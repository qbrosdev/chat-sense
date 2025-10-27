package com.qbros.chatsense.service.analysis.llm.ingetration

import com.qbros.chatsense.service.analysis.config.properties.LLMAnalyzerProperties
import com.qbros.chatsense.service.analysis.model.AnalysisSummary
import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import com.qbros.chatsense.service.analysis.model.CommentsSummary
import com.qbros.chatsense.service.analysis.model.FalseClaimsReport
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
    private val properties: LLMAnalyzerProperties,
    private val analysisClient: ChatClient,
) {

    private val falseClaimsPrompt: String = properties.falseClaims.prompt
    private val summarizePrompt: String = properties.summarize.prompt
    private val semaphore: Semaphore = Semaphore(3)
    private val logger = KotlinLogging.logger {}

    suspend fun summarizeTopComments(analysisSummary: AnalysisSummary): Deferred<CommentsSummary> = coroutineScope {
        async(Dispatchers.IO) {
            val popularComments = analysisSummary.topComments
                .map { PopularComment(it.text, it.likeCount) }
            summarizeTopComment(popularComments)
        }
    }

    fun detectFalseClaims(analysisSummary: AnalysisSummary): Flow<FalseClaimsReport> = channelFlow {

        analysisSummary.topComments
            .map {
                launch(Dispatchers.IO) {
                    semaphore.withPermit {
                        try {
                            val report = analyzeComment(it)
                            val filteredReport = report.copy(
                                claims = report.claims.filter { claim -> claim.confidence >= 0.7 }
                            )

                            if (filteredReport.claims.isNotEmpty()) {
                                send(filteredReport).also { logger.debug { "Sent the report $it" } }
                            }
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to analyze comment: ${it.text.take(50)}" }
                            send(FalseClaimsReport(comment = it.text, claims = emptyList()))
                        }
                    }
                }
            }.joinAll()
            .let {
                logger.info { "All comments processed, closing channel" }
                close()
            }

        awaitClose {
            logger.info { " Channel closed for detectFalseClaims()" }
        }
    }

    private fun summarizeTopComment(popularComments: List<PopularComment>): CommentsSummary =
        analysisClient.prompt()
            .user(summarizePrompt.replace("{comments}", popularComments.joinToString("\n") {
                "- Likes: ${it.likeCount} | Comment: ${it.text}"
            }))
            .call()
            .also { logger.debug { "LLM reply: ${it.content()}" } }
            .responseEntity(CommentsSummary::class.java).entity ?: CommentsSummary("NA", emptyList())
            .also { logger.debug { "Converted response: $it" } }

    private fun analyzeComment(comment: CommentAnalysis): FalseClaimsReport = analysisClient.prompt()
        .user(falseClaimsPrompt.replace("{comment}", comment.text))
        .call()
        .also { logger.debug { "LLM reply: ${it.content()}" } }
        .responseEntity(FalseClaimsReport::class.java).entity ?: FalseClaimsReport(comment = comment.text, claims = emptyList())
        .also { logger.debug { "Converted response: $it" } }
}
data class PopularComment(val text: String, val likeCount: Int)
