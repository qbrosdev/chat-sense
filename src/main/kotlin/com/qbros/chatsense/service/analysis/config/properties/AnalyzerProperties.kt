package com.qbros.chatsense.service.analysis.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "analyzer")
data class AnalyzerProperties(val sentiment: Config = Config(),
                              val ner: Config = Config(),
                              val linguistic: Config = Config())
@ConfigurationProperties(prefix = "llm-analysis")
data class LLMAnalysisProperties(
    val falseClaims: LLMAnalyzer,
    val summarize: LLMAnalyzer
)

data class LLMAnalyzer(
    val prompt: String,         // required in YAML
    val config: Config = Config()
)

data class Config(
    val enabled: Boolean = true,
    val confidenceThreshold: Double = 0.9
)