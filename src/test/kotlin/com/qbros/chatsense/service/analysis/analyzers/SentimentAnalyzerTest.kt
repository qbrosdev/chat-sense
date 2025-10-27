package com.qbros.chatsense.service.analysis.analyzers

import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test

class SentimentAnalyzerTest{

    private val sentimentAnalyzer: SentimentAnalyzer = SentimentAnalyzer()
    private val logger = KotlinLogging.logger {}

    @Test
    fun `dfdf`(){

        val result = sentimentAnalyzer.process(CommentAnalysis(""))
        logger.info { result.sentiment }
    }
}