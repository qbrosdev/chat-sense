package com.qbros.chatsense.service.analysis.analyzers

import ai.djl.modality.Classifications
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.ProgressBar
import com.qbros.chatsense.domain.Sentiment
import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component


@ConditionalOnProperty("analyzer.sentiment.config.enabled", havingValue = "true", matchIfMissing = false)
@Component
class SentimentAnalyzer : Analyzer {

    private val logger = KotlinLogging.logger {}
    private val scope = CoroutineScope(Dispatchers.Default)

    private val model: ZooModel<String, Classifications> by lazy {
        val criteria = Criteria.builder()
            .setTypes(String::class.java, Classifications::class.java)
            .optEngine("PyTorch")
            .optModelUrls("djl://ai.djl.huggingface.pytorch/j-hartmann/emotion-english-distilroberta-base")
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
                val result: Classifications = predictor.predict(analysis.text)
                val best: Classifications.Classification = result.best()
                Sentiment.fromLabel(best.className)
            }
            .run { analysis.copy(sentiment = this) }
}