package com.qbros.chatsense.service.socialmedia

import com.qbros.chatsense.domain.CommentDto
import com.qbros.chatsense.domain.ContentProviderType

interface SocialMediaService {

    val providerType: ContentProviderType
    fun getTopComments(contentId: String, limit: Int): List<CommentDto>

}