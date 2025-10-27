package com.qbros.chatsense.service.analysis.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ai-analyzer")
data class LLMAnalyzerProperties(
    val falseClaims: Analyzer,
    val summarize:Analyzer
)

data class Analyzer(val prompt: String)
