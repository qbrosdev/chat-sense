package com.qbros.chatsense.service.analysis.analyzers

import ai.djl.engine.Engine
import ai.djl.repository.zoo.ModelZoo
import com.qbros.chatsense.domain.CommentDto
import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import com.qbros.chatsense.utils.measure
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

interface Analyzer {
    /**
     * Determines whether this analyzer should process the given analysis.
     * Default: always true.
     */
    fun supports(analysis: CommentAnalysis): Boolean = true
    fun process(analysis: CommentAnalysis): CommentAnalysis
}

@Service
class AnalyzerService(private val analyzers: List<Analyzer>) {

    private val logger = KotlinLogging.logger {}

    @PostConstruct
    private fun init() {
        listAllSupportedModels()
    }

    fun analyze(comments: List<CommentDto>): List<CommentAnalysis> = runBlocking {

        val commentsAnalysis = comments
            .map { comment ->
                async(Dispatchers.Default) {
                    analyzeCommentSequentially(comment)
                }
            }.awaitAll()
            .toList()

        commentsAnalysis
            .also { logger.debug { "✅ Analyzer completed" } }
    }

    private fun analyzeCommentSequentially(comment: CommentDto): CommentAnalysis {
        var commentAnalysis = CommentAnalysis(comment.text, comment.likeCount, comment.replyCount)

        val totalTime = measureTimeMillis {
            analyzers.forEach { analyzer ->
                val (updatedResult, time) = measure { analyzer.process(commentAnalysis) }
                commentAnalysis = updatedResult
                logger.debug { "[${analyzer::class.simpleName}] took $time ms" }
            }
        }

        logger.debug { "Processing '${comment.text}' took $totalTime ms" }

        return commentAnalysis
    }

    private fun listAllSupportedModels() {

        logger.info { "Available engines: ${Engine.getAllEngines().joinToString()}" }

        logger.debug { "=== Listing All Supported Models By DJL ===" }

        try {
            // Get all model zoos
            ModelZoo.listModelZoo().forEach { zooName ->

                logger.debug { "\n Model Zoo: $zooName" }

                try {
                    val zoo = ModelZoo.getModelZoo(zooName.groupId)
                    // Get all model loaders in this zoo
                    zoo.modelLoaders
                        .sortedBy { it.groupId }
                        .forEach { loader ->
                            logger.debug {
                                """
                            ArtifactId: ${loader.artifactId}
                            Name: ${loader.groupId ?: "N/A"}
                            Supported Apps: ${loader.application}
                            ------------------------------------
                        """.trimIndent()
                            }
                        }

                } catch (e: Exception) {
                    logger.warn { " Error accessing zoo: ${e.message}" }
                }
            }

        } catch (e: Exception) {
            logger.warn { "Error listing models: ${e.message}" }
            e.printStackTrace()
        }
    }
}