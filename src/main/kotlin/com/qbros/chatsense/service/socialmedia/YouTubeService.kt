package com.qbros.chatsense.service.socialmedia

import com.google.api.services.youtube.YouTube
import com.qbros.chatsense.domain.CommentDto
import com.qbros.chatsense.domain.ContentProviderType
import com.qbros.chatsense.service.socialmedia.config.YouTubeConfig.Companion.API_KEY
import com.qbros.chatsense.service.socialmedia.model.CommentOrder
import com.qbros.chatsense.service.socialmedia.model.CommentParts
import com.qbros.chatsense.service.socialmedia.model.CommentTextFormat
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.net.UnknownHostException

@Service
class YouTubeService(private val youtubeClient: YouTube) : SocialMediaService {

    override val providerType = ContentProviderType.YOUTUBE
    private val logger = KotlinLogging.logger{}

    override fun getTopComments(contentId: String, limit: Int): List<CommentDto> {
        return try {
            val request = youtubeClient.commentThreads()
                .list(listOf(CommentParts.SNIPPET.value))
                .setVideoId(contentId)
                .setTextFormat(CommentTextFormat.PLAIN_TEXT.value)
                .setOrder(CommentOrder.RELEVANCE.value)
                .setMaxResults(limit.toLong())
                .setKey(API_KEY)

            request.execute().items.map { it.toCommentDto() }
        } catch (e: UnknownHostException) {
            logger.error(e) { "Cannot reach YouTube API. Check network/DNS." }
            emptyList()
        }
    }

}
