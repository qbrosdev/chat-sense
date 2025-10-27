package com.qbros.chatsense.service.analysis.analyzers


import com.qbros.chatsense.TextFixture
import com.qbros.chatsense.domain.CommentDto
import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.system.measureTimeMillis


@SpringBootTest
class AnalyzerServiceIntegrationTest {

    @Autowired
    lateinit var analyzerService: AnalyzerService

    val logger = KotlinLogging.logger {}

    @Test
    fun `stress test2`() {
        val input = MutableList(10) {
            CommentDto(text = TextFixture.generateSentence())
        }

        val result: List<CommentAnalysis>

        val measureMillisTime = measureTimeMillis {
            result = analyzerService.analyze(input)
        }

        logger.info {
            """Got the result in: ${measureMillisTime} 
            $result"""
                .trimMargin()
        }
    }

    @Test
    fun `stress test`() {

        val input = listOf(
            CommentDto(
                text = """im fed up with the corruption from lobbying, to expenses to covering up the 
                grooming shit and of them ignoring the democratic will of the people,70+ years of voting and polling 
                for lower immigration and getting the opposite well its gone to far they have allowed in millions legal 
                and using the illegals to ignore it and gas light the people , im fed up of dei based hiring which 
                is 99% of the time based on ethnicity aka racism towards the white native population of the uk, 
                im fed up non english aka those who have adopted british are allowed to dilute our democratic voice 
                and im fed up seeing non natives in positions of power, i have no issue on skilled immigration 
                to a degree but it wasn't supposed to be permanent it was supposed to be till we train our own immigration 
                has been abused the meaning of british has been abused and corrupted to the point id like to see it dropped, 
                and considering all the devolved crap it should be""".trimIndent()
            )
        )

        val result = analyzerService.analyze(input)

        logger.info { result }
    }

}