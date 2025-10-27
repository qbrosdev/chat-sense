package com.qbros.chatsense.integration

import com.github.dockerjava.api.model.Bind
import com.qbros.chatsense.TextFixture.analysisSummary
import com.qbros.chatsense.service.analysis.llm.ingetration.LLMAnalysisService
import com.qbros.chatsense.service.analysis.model.ClaimClassification
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.test.assertTrue

@ActiveProfiles("integration")
@Testcontainers
@SpringBootTest
class LLMAnalysisServiceIntegrationTest {

    @Autowired
    lateinit var LLMAnalysisService: LLMAnalysisService

    val logger = KotlinLogging.logger{}

    companion object {
        @Container
        @JvmStatic
        val ollamaContainer = GenericContainer<Nothing>(DockerImageName.parse("ollama/ollama:0.12.1"))
            .apply {
                withExposedPorts(11434)
                withCommand("serve")
                withReuse(true)
                waitingFor(Wait.forHttp("/").forStatusCode(200))
                withCreateContainerCmdModifier { cmd ->
                    cmd.hostConfig!!.withBinds(Bind.parse("llm_data:/root/.ollama/models"))
                }
            }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.ai.ollama.base-url") {
                "http://${ollamaContainer.host}:${ollamaContainer.getMappedPort(11434)}"
            }
        }

        @JvmStatic
        @BeforeAll
        fun pullModel() {
            println("INIT/////")
            val result = ollamaContainer.execInContainer("ollama", "pull", "llama3.1:8b")
            println(result.stdout)
            println(result.stderr)
        }
    }

    @Test
    fun factCheckingTest() = runBlocking {

        withTimeout(240_000) {
            val actual = LLMAnalysisService.detectFalseClaims(
                analysisSummary(
                    "linux was the first os",
                    "sun is bigger than the earth"
                )
            ).toList()

            logger.info { actual }
            assert(actual.isNotEmpty())
            assertTrue(actual.size == 2)

            actual.forEachIndexed { index, report ->
                assertTrue(report.claims.isNotEmpty(), "Report $index should have claims")
                report.claims.forEach { claim ->
                    assertTrue(
                        claim.classification == ClaimClassification.FALSE ||
                                claim.classification == ClaimClassification.MISLEADING,
                        "Unexpected classification: ${claim.classification}"
                    )
                    assertTrue(claim.explanation.isNotBlank(), "Explanation missing for claim: ${claim.claim}")
                }
            }
        }
    }

    @Test
    fun summarizeTopCommentsTest() = runBlocking {

        withTimeout(240_000) {
            val actual = LLMAnalysisService.summarizeTopComments(
                analysisSummary(
                    """
                    This is pretty normal through out the entite IT industry. 
                    I'm a Linux Systems Administrator but my role is entirely cloud based with a bit of DevOps in the mix the mix. 
                    The traditional Sysadmin role has changed a lot since the rise of DevOps culture adoption in organizations. 
                    Many existing sysadmim roles have been rebranded as DevOps Engineer, Site Reliability Engineer and Cloud Engineer 
                    hense why these titles dominate over Sysadmin title over the years. 
                    They are just evolved Sysadmin roles. 
                    Network Administrator title isn't used much either thats been replaced with 
                    Network Engineer and Cloud Network Engineer that handles both engineering and daily 
                    network maintenance operations.
                """
                )
            )

            println(actual)
        }

    }
}