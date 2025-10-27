package com.qbros.chatsense.integration

import com.qbros.chatsense.service.analysis.Coordinator
import com.qbros.chatsense.service.analysis.model.CommentsInsights
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.system.measureTimeMillis

@ActiveProfiles("integration")
@SpringBootTest
class CoordinatorIntegrationTest {

    @Autowired
    private lateinit var coordinator: Coordinator

    private val logger = KotlinLogging.logger {}

    @Test
    fun test1() = runBlocking  {
        val result: CommentsInsights

        val measureTimeMillis = measureTimeMillis {
            result = coordinator.getInsights("W6TqyEQnFsA", 10)
        }

        logger.info {
            """Got the result in: ${measureTimeMillis} $result""".trimMargin()
        }

        assertNotNull(result.analysis)
        assertNotNull(result.commentSummary)
    }

}