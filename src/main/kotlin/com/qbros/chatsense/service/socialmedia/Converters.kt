package com.qbros.chatsense.service.socialmedia

import com.google.api.services.youtube.model.Comment
import com.google.api.services.youtube.model.CommentSnippet
import com.google.api.services.youtube.model.CommentThread
import com.qbros.chatsense.domain.CommentDto


fun CommentThread.toCommentDto(): CommentDto {
    val top: Comment = this.snippet.topLevelComment
    val snippet: CommentSnippet = top.snippet
    return CommentDto(
        id = top.id,
        author = snippet.authorDisplayName,
        text = snippet.textDisplay,
        likeCount = snippet.likeCount.toInt(),
        replyCount = this.snippet.totalReplyCount.toInt(),
        contentId = snippet.videoId,
    )
}