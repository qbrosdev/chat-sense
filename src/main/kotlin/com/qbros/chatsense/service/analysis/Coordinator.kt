package com.qbros.chatsense.service.analysis

import com.qbros.chatsense.service.analysis.analyzers.AnalyzerService
import com.qbros.chatsense.service.analysis.llm.ingetration.LLMAnalysisService
import com.qbros.chatsense.service.analysis.model.AnalysisSummary
import com.qbros.chatsense.service.analysis.model.CommentsInsights
import com.qbros.chatsense.service.socialmedia.SocialMediaService
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class Coordinator(
    private val youtubeService: SocialMediaService,
    private val preProcessService: PreProcessService,
    private val analyzerService: AnalyzerService,
    private val summaryGeneratorService: SummaryGeneratorService,
    private val llmAnalysisService: LLMAnalysisService
) {

    suspend fun getInsights(contentId: String, limit: Int = 10): CommentsInsights = coroutineScope  {

        val summaryReport = getSummaryReport(contentId, limit)

        val summarizeTopComments = llmAnalysisService.summarizeTopComments(summaryReport)

        val falseClaims = llmAnalysisService.detectFalseClaims(summaryReport)

        CommentsInsights(
            analysis = summaryReport,
            commentSummary = summarizeTopComments,
            falseClaimReports = falseClaims
        )
    }

    private fun getSummaryReport(contentId: String, limit: Int = 10): AnalysisSummary {

        return youtubeService.getTopComments(contentId, limit)
            .let { preProcessService.process(it) }
            .let { analyzerService.analyze(it) }
            .let { summaryGeneratorService.generateSummary(it) }
    }
}