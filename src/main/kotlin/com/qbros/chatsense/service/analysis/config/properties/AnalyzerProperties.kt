package com.qbros.chatsense.service.analysis.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "analyzer")
class AnalyzerProperties {

    var sentiment: AnalyzerToggle = AnalyzerToggle()
    var ner: AnalyzerToggle = AnalyzerToggle()
    var linguistic: AnalyzerToggle = AnalyzerToggle()

    class AnalyzerToggle {
        var enabled: Boolean = true
    }
}