package com.qbros.chatsense.controller

import com.qbros.chatsense.service.analysis.Coordinator
import com.qbros.chatsense.service.analysis.model.CommentsInsights
import com.qbros.chatsense.service.analysis.model.FalseClaimsReport
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/insights")
class ChatSenseApi(private val coordinator: Coordinator) {

    private val logger = KotlinLogging.logger {}

    @GetMapping(produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun getInsights(@RequestParam contentId: String, @RequestParam limit: Int): Flow<InsightEvent> = flow {

        val insights = coordinator.getInsights(contentId, limit)

        // Emit the fast summary immediately
        emit(toSummary(insights)).also { logger.info { "Published summary" } }

        // Emit slow summary once ready
        emit(summarizeTopComments(insights))

        // Stream false claim reports as they arrive
        emitAll(flow1(insights))
    }

    private suspend fun summarizeTopComments(insights: CommentsInsights) =
        InsightEvent.SummarizeTopComments(insights.commentSummary.await().summary)

    private fun flow1(insights: CommentsInsights) =
        insights.falseClaimReports.map { InsightEvent.FalseClaim(it) }

    private fun toSummary(insights: CommentsInsights) = InsightEvent.Summary(
        sentimentOverview = insights.analysis.sentimentOverview,
        keywords = insights.analysis.topKeywords,
        entities = insights.analysis.topEntityNames
    )

}

sealed class InsightEvent {
    data class Summary(
        val sentimentOverview: Any,
        val keywords: Map<String, Int>,
        val entities: Map<String, Int>
    ) : InsightEvent()

    data class SummarizeTopComments(val summary: String) : InsightEvent()
    data class FalseClaim(val report: FalseClaimsReport) : InsightEvent()
}