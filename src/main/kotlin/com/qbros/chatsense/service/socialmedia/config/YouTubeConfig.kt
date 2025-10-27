package com.qbros.chatsense.service.socialmedia.config

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class YouTubeConfig {

    companion object {
        const val API_KEY = "AIzaSyBS82pzxo0inBX1pwB1PIVn4XQnqOiaJ-k"
        private const val APPLICATION_NAME = "debater-app"
        private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    }

    val logger = KotlinLogging.logger {}

    @Bean
    @Throws(Exception::class)
    fun youtubeService(): YouTube {

        return YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null)
            .setApplicationName(APPLICATION_NAME)
            .build()
            .also { logger.info { "YouTube service initialized successfully" } }
    }

}
