package com.qbros.chatsense.service.analysis

import com.qbros.chatsense.service.analysis.model.AnalysisSummary
import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class SummaryGeneratorService {

    private val logger = KotlinLogging.logger {}

    fun generateSummary(commentsAnalysis: List<CommentAnalysis>): AnalysisSummary {

        val wordCounts = commentsAnalysis
            .flatMap { it.words }
            .groupingBy { it.lowercase() }
            .eachCount()

        val topKeywords = wordCounts.entries
            .sortedBy { it.value }
            .take(10)
            .associate { it.toPair() }

        val sentimentOverview = commentsAnalysis
            .groupingBy { it.sentiment }
            .fold(initialValue = 0) { acc, word -> acc + word.likeCount }


        val topComments = commentsAnalysis
            .sortedByDescending { it.likeCount + it.replyCount }
            .take(3)

        val topEntityNames =  commentsAnalysis
            .flatMap { it.entities }
            .groupingBy { it.word }
            .eachCount()
            .entries
            .sortedBy { it.value }
            .take(5)
            .associate { it.toPair() }

        return AnalysisSummary(
            sentimentOverview = sentimentOverview,
            topComments = topComments,
            topKeywords = topKeywords,
            topEntityNames = topEntityNames
        )
            .also { logger.info { "✅ Summary Generation completed" } }

    }
}
