package com.qbros.chatsense.service.analysis.analyzers

import ai.djl.modality.nlp.translator.NamedEntity
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.ProgressBar
import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import com.qbros.chatsense.service.analysis.model.Entity
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnProperty("analyzer.ner.config.enabled", havingValue = "true", matchIfMissing = false)
@Component
class NerAnalyzer : Analyzer {

    private val logger = KotlinLogging.logger {}
    private val scope = CoroutineScope(Dispatchers.Default)

    private val model: ZooModel<String, Array<NamedEntity>> by lazy {
        val criteria = Criteria.builder()
            .setTypes(String::class.java, Array<NamedEntity>::class.java)
            .optEngine("PyTorch")
            .optModelUrls("djl://ai.djl.huggingface.pytorch/dslim/bert-base-NER-uncased")
            .optProgress(ProgressBar())
            .build()

        criteria.loadModel()
    }

    @PostConstruct
    protected fun preloadModel() {
        scope.launch {
            logger.info { "Initializing the analyzer" }
            model
            logger.info { "Completed initializing the analyzer" }
        }
    }

    @PreDestroy
    protected fun close() {
        model.close()
        scope.cancel()
    }

    override fun process(analysis: CommentAnalysis): CommentAnalysis =
        model.newPredictor()
            .use { predictor ->

                val entities = (predictor.predict(analysis.text) ?: emptyArray())
                    .asList()
                    .onEach { logger.debug { "item is: $it" } }

                mergeEntities(entities)
                    .filter { it.score > 0.7f }
                    .map { Entity(word = it.word, type = it.entity, it.score) }
                    .toMutableList()

            }
            .run { analysis.copy(entities = this) }

    private fun mergeEntities(entities: List<NamedEntity>): List<NamedEntity> {
        if (entities.isEmpty()) return emptyList()

        val sorted = entities.sortedBy { it.start }
        val merged = mutableListOf<NamedEntity>()

        var currentEntity = sorted.first()

        for (it in sorted.drop(1)) {

            if ((currentEntity.extractType() != it.extractType()) || (it.start > currentEntity.end + 1)) {
                // different type or non-adjacent → finish previous entity
                merged.addCleaned(currentEntity)
                currentEntity = it
            } else {

                val word = when (it.start == currentEntity.end) {
                    true -> currentEntity.word + it.word  // same type and adjacent → merge
                    false -> "${currentEntity.word} ${it.word}" // → Add a space before appending so multi-word entities
                }
                currentEntity = NamedEntity(
                    it.entity,
                    //todo: improve the average score implementation
                    (currentEntity.score + it.score) / 2,
                    currentEntity.index,
                    word,
                    currentEntity.start,
                    it.end
                )
            }
        }
        merged.addCleaned(currentEntity)

        return merged
    }

    private fun MutableList<NamedEntity>.addCleaned(currentEntity: NamedEntity) {
        val cleaned = NamedEntity(
            currentEntity.entity,
            currentEntity.score,
            currentEntity.index,
            currentEntity.word.replace("#", ""),
            currentEntity.start,
            currentEntity.end
        )
        this.add(cleaned)
    }

    private fun NamedEntity.extractType(): String = this.entity.substring(2)

}
