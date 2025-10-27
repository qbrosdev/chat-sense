package com.qbros.chatsense.service.socialmedia.model


enum class CommentOrder(val value: String) {
    RELEVANCE("relevance"),
    TIME("time")
}

enum class CommentParts(val value: String) {
    SNIPPET("snippet"),
    REPLIES("replies"),
}

enum class CommentTextFormat(val value: String) {
    HTML("html"),
    PLAIN_TEXT("plainText"),
}