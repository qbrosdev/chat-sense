package com.qbros.chatsense.service.analysis.analyzers

import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import org.junit.jupiter.api.Test

class LinguisticAnalyzerTest {

    private val linguisticAnalyzer = LinguisticAnalyzer()

    @Test
    fun testo() {
        val actual = linguisticAnalyzer.process(CommentAnalysis(text = "apple, applE, APPLE"))
        assert(actual.words.size == 3)
    }
}