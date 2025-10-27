package com.qbros.chatsense.service.analysis

import com.qbros.chatsense.domain.CommentDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class PreProcessService {


    private val urlPattern = Regex("https?://\\S+\\s?")
    private val emojiPattern = Regex("[\\p{So}\\p{Cn}]+")
    private val spacePattern = Regex("\\s+")

    // Thresholds
    private val maxLength = 2000 // skip very long comments

    val logger = KotlinLogging.logger {}

    fun process(commentEntries: List<CommentDto>): List<CommentDto> {
        return commentEntries.asSequence()
            .filter { filter(it) }
            .map { cleanup(it) }
            .filter { it.text.isNotBlank() }
            .toList()
            .also { logger.info { "✅ Preprocess completed" } }
    }

    private fun filter(it: CommentDto) =
        (it.text.isNotBlank() && it.text.length <= maxLength)

    private fun cleanup(commentDto: CommentDto): CommentDto {
        val cleanedText = commentDto.text
            .replace(urlPattern, "")   // remove URLs
            .replace(emojiPattern, "") // remove emojis
            .replace(spacePattern, " ") // normalize spaces
            .trim()
            .lowercase()

        return commentDto.copy(text = cleanedText)
    }
}