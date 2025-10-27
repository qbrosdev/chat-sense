package com.qbros.chatsense.service.analysis

import com.qbros.chatsense.domain.Sentiment
import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import com.qbros.chatsense.service.analysis.model.Entity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SummaryGeneratorServiceTest {

    private val summaryService = SummaryGeneratorService()

    @Test
    fun `generateSummary should compute correct summary`() {
        // Arrange: create sample CommentAnalysis objects
        val comment1 = CommentAnalysis(
            text = "Kotlin is great",
            words = listOf("Kotlin", "is", "great"),
            entities = listOf(Entity("Kotlin", "Language", 0.9f)),
            sentiment = Sentiment.JOY,
            likeCount = 5,
            replyCount = 2
        )

        val comment2 = CommentAnalysis(
            text = "I love Kotlin",
            words = listOf("I", "love", "Kotlin"),
            entities = listOf(Entity("Kotlin", "Language", 0.8f)),
            sentiment = Sentiment.JOY,
            likeCount = 3,
            replyCount = 1
        )

        val comment3 = CommentAnalysis(
            text = "Java is old",
            words = listOf("Java", "is", "old"),
            entities = listOf(Entity("Java", "Language", 0.85f)),
            sentiment = Sentiment.NATURAL,
            likeCount = 1,
            replyCount = 0
        )

        val comment4 = CommentAnalysis(
            text = "I dislike bugs",
            words = listOf("I", "dislike", "bugs"),
            entities = emptyList(),
            sentiment = Sentiment.DISGUST,
            likeCount = 0,
            replyCount = 0
        )

        val comments = listOf(comment1, comment2, comment3, comment4)

        // Act
        val summary = summaryService.generateSummary(comments)

        // Assert
        // Sentiment overview
        assertEquals(8, summary.sentimentOverview[Sentiment.JOY])
        assertEquals(1, summary.sentimentOverview[Sentiment.NATURAL])
        assertEquals(null, summary.sentimentOverview[Sentiment.ANGER])

        // Top keywords
        assert(summary.topKeywords.containsKey("kotlin"))
        assert(summary.topKeywords.containsKey("is"))
        assert(summary.topKeywords.containsKey("i"))

        // Top comments (sorted by likeCount + replyCount)
        assertEquals(comment1, summary.topComments[0])
        assertEquals(comment2, summary.topComments[1])
        assertEquals(comment3, summary.topComments[2])

        // Top entity names
        assertEquals(2, summary.topEntityNames["Kotlin"]) // appears in 2 comments
        assertEquals(1, summary.topEntityNames["Java"])   // appears in 1 comment
    }
}