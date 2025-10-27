package com.qbros.chatsense.domain


enum class ContentProviderType {
    YOUTUBE,
}

enum class Sentiment(val label: String) {
    // Emotion-based labels (for Hugging Face models)
    JOY("joy"),           //😀
    ANGER("anger"),       //🤬
    DISGUST("disgust"),   //🤢
    FEAR("fear"),         //😨
    NATURAL("neutral"),   //😐
    SADNESS("sadness"),   //😭
    SURPRISE("surprise"), //😲
    UNKNOWN("unknown");

    companion object {
        fun fromLabel(label: String): Sentiment =
            entries
                .find { it.label.equals(label, ignoreCase = true) } ?: UNKNOWN
    }
}

data class CommentDto(
    val id: String? = null,
    val author: String? = null,
    val text: String,
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    val contentId: String? = null,
)



