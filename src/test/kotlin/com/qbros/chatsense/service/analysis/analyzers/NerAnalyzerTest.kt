package com.qbros.chatsense.service.analysis.analyzers

import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertTrue


class NerAnalyzerTest {

    private val nerAnalyzer = NerAnalyzer()
    private val logger = KotlinLogging.logger {}

    companion object {
        @JvmStatic
        fun nerTestCases() = listOf(
            Triple(
                """
                Elon Musk met Tim Cook at Apple's headquarters in Cupertino, California, after flying from SpaceX’s base in Texas.
                """.trimIndent(),
                listOf("Elon Musk", "Tim Cook", "Apple", "Cupertino", "California", "SpaceX", "Texas"),
                3
            ),
            Triple(
                """
                Dr. Jean-Paul Sartre visited the newly-opened Microsoft Research lab in New York City, 
                while Elon Musk and Tim Cook attended a SpaceX-AI summit in Cupertino, California, 
                after flying from SpaceX’s Texas headquarters.
                """.trimIndent(),
                listOf(
                    "Jean-Paul Sartre", "Microsoft Research", "New York City",
                    "Elon Musk", "Tim Cook", "SpaceX-AI", "Cupertino",
                    "California", "SpaceX", "Texas"
                ),
                8
            )
        )
    }

    @ParameterizedTest
    @MethodSource("nerTestCases")
    fun testNerCases(input: Triple<String, List<String>, Int>) {
        val (text, expectedEntities, minMatches) = input

        val result = nerAnalyzer.process(CommentAnalysis(text))

        assertDetectedEntities(
            expected = expectedEntities,
            actual = result.entities.map { it.word },
            minMatches = minMatches
        )
    }

    private fun assertDetectedEntities(
        expected: List<String>,
        actual: List<String>,
        minMatches: Int = expected.size
    ) {
        val normalizedExpected = expected.map { it.lowercase().trim() }
        val normalizedActual = actual.map { it.lowercase().trim() }

        val found = normalizedExpected.filter { normalizedActual.contains(it) }
        val missing = normalizedExpected - found.toSet()
        val unexpected = normalizedActual.filter { !normalizedExpected.contains(it) }

        val message = buildString {
            appendLine("NER Evaluation Result:")
            appendLine("Actual entities: ${actual.joinToString()}")
            appendLine("Expected entities: ${expected.joinToString()}")
            if (missing.isNotEmpty()) appendLine("❌ Missing: ${missing.joinToString()}")
            if (unexpected.isNotEmpty()) appendLine("⚠️ Unexpected: ${unexpected.joinToString()}")
        }

        logger.info { message }

        assertTrue(found.size >= minMatches, "expected number of entities not found")
    }

}