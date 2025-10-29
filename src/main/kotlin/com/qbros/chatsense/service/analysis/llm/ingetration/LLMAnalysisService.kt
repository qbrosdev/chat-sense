package com.qbros.chatsense.service.analysis.llm.ingetration

import com.qbros.chatsense.service.analysis.config.properties.LLMAnalysisProperties
import com.qbros.chatsense.service.analysis.model.AnalysisSummary
import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import com.qbros.chatsense.service.analysis.model.CommentsSummary
import com.qbros.chatsense.service.analysis.model.FalseClaimsReport
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
class LLMAnalysisService(
    private val properties: LLMAnalysisProperties,
    private val analysisClient: ChatClient,
) {

    private val falseClaimsPrompt: String = properties.falseClaims.prompt
    private val summarizePrompt: String = properties.summarize.prompt
    private val semaphore: Semaphore = Semaphore(3)
    private val logger = KotlinLogging.logger {}

    suspend fun summarizeTopComments(analysisSummary: AnalysisSummary): Deferred<CommentsSummary> = coroutineScope {

        if (!properties.summarize.config.enabled) {
            return@coroutineScope CompletableDeferred(CommentsSummary())
        }

        async(Dispatchers.IO) {
            analysisSummary.topComments
                .map { PopularComment(it.text, it.likeCount) }
                .let(::summarizeTopComments)
        }
    }

    fun detectFalseClaims(analysisSummary: AnalysisSummary): Flow<FalseClaimsReport> = channelFlow {

        if (!properties.falseClaims.config.enabled) {
            send(FalseClaimsReport())
            return@channelFlow
        }

        analysisSummary.topComments
            .map {
                launch(Dispatchers.IO) { processFalseClaims(it) }
            }.joinAll()
            .let {
                logger.info { "All comments processed, closing channel" }
                close()
            }

        awaitClose {
            logger.info { " Channel closed for detectFalseClaims()" }
        }
    }

    private fun summarizeTopComments(popularComments: List<PopularComment>): CommentsSummary =
        analysisClient.prompt()
            .user(summarizePrompt.replace("{comments}", popularComments.joinToString("\n") {
                "- Likes: ${it.likeCount} | Comment: ${it.text}"
            }))
            .call()
            .also { logger.debug { "LLM reply: ${it.content()}" } }
            .responseEntity(CommentsSummary::class.java).entity ?: CommentsSummary()

    private suspend fun ProducerScope<FalseClaimsReport>.processFalseClaims(comment: CommentAnalysis) {
        semaphore.withPermit {
            try {
                analyzeComment(comment)
                    .run { copy(claims = this.claims.filter { it.confidence >= 0.7 }) }
                    .takeIf { it.claims.isNotEmpty() }
                    ?.also {
                        send(it)
                        logger.debug { "Sent the report $it" }
                    }
            } catch (e: Exception) {
                logger.error(e) { "Failed to analyze comment: ${comment.text.take(50)}" }
                send(FalseClaimsReport(comment = comment.text, claims = emptyList()))
            }
        }
    }

    private fun analyzeComment(comment: CommentAnalysis): FalseClaimsReport = analysisClient.prompt()
        .user(falseClaimsPrompt.replace("{comment}", comment.text))
        .call()
        .also { logger.debug { "LLM reply: ${it.content()}" } }
        .responseEntity(FalseClaimsReport::class.java).entity ?: FalseClaimsReport(comment = comment.text)
}

data class PopularComment(val text: String, val likeCount: Int)
