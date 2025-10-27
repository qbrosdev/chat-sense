package com.qbros.chatsense.service.analysis.model

import com.qbros.chatsense.domain.Sentiment
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow


data class SentenceAnalysis(val keywords: List<String>)

data class CommentsSummary(
    val summary: String,
    val key_points: List<String>
)

data class FalseClaimsReport(
    val comment: String = "NA",
    val claims: List<Claim>
)

data class Claim(
    val claim: String,
    val classification: ClaimClassification,
    val explanation: String,
    val confidence: Float
)

enum class ClaimClassification {
    MISLEADING,
    FALSE,
    UNCLEAR,
}

data class Entity(
    val word: String,
    val type: String,   // e.g. "PER", "ORG", "LOC"
    val score: Float,     // model confidence
)

data class CommentAnalysis(
    val text: String,
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    val words: List<String> = emptyList(),
    val entities: List<Entity> = emptyList(),
    val sentiment: Sentiment = Sentiment.UNKNOWN,
    val hateSpeach: String = "",
)

data class AnalysisSummary(
    val sentimentOverview: Map<Sentiment, Int>,
    val topComments: List<CommentAnalysis>,
    val topKeywords: Map<String, Int>,
    val topEntityNames: Map<String, Int>
)

data class CommentsInsights(
    val analysis: AnalysisSummary,
    val commentSummary: Deferred<CommentsSummary>,
    val falseClaimReports: Flow<FalseClaimsReport>,
)
