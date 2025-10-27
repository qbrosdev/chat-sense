package com.qbros.chatsense.service.analysis.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LLMConfig(private val ollamaChatModel: OllamaChatModel) {

    @Bean
    fun ollamaChatClient(): ChatClient {
        return ChatClient.builder(ollamaChatModel).build()
    }

    @Bean("analysisClient")
    fun analysisClient(): ChatClient =
        ChatClient.builder(ollamaChatModel)
            .defaultOptions(
                ChatOptions.builder()
                    //.model("deepseek-r1:7b")
                    .model("llama3.1:8b")
                    //.model("mistral:7b")
                    .temperature(0.1)  // stricter, more deterministic
                    .frequencyPenalty(0.2)
                    .build()
            )
            .build()
}