package com.qbros.chatsense.service.analysis

import com.qbros.chatsense.domain.CommentDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PreProcessServiceTest {

    private val preProcessService = PreProcessService()

    @Test
    fun `should clean up comments by removing urls, emojis, spaces and lowercasing`() {
        // given
        val rawCommentEntries = listOf(
            CommentDto(text = "Check this out! https://example.com 😂🔥"),
            CommentDto(text = "  GREAT tutorial!!   ")
        )

        // when
        val cleaned = preProcessService.process(rawCommentEntries)

        // then
        assertEquals(2, cleaned.size)
        assertEquals("check this out!", cleaned[0].text)
        assertEquals("great tutorial!!", cleaned[1].text)
    }


}