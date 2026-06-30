package com.example.appthemuse.utils.constants

object FirebaseConstants {
    const val USERS = "users"
    const val BOOKS = "books"
    const val CHAPTERS = "chapters"
    const val REVIEWS = "reviews"

    object BookFields {
        const val TITLE = "title"
        const val AUTHOR_ID = "author_id"
        const val CATEGORY_ID = "category_id"
        const val COVER_URL = "cover_url"
        const val DESCRIPTION = "description"
        const val STATUS = "status"
        const val IS_PREMIUM = "is_premium"
        const val VIEW_COUNT = "view_count"
        const val CREATED_AT = "created_at"
    }

    object ChapterFields {
        const val CHAPTER_NUMBER = "chapter_number"
        const val TITLE = "title"
        const val CONTENT = "content"
        const val AUDIO_URL = "audio_url"
        const val IS_DRAFT = "is_draft"
        const val CREATED_AT = "created_at"
    }

    object ReviewFields {
        const val BOOK_ID = "book_id"
        const val USER_ID = "user_id"
        const val RATING = "rating"
        const val COMMENT = "comment"
        const val IS_HIDDEN = "is_hidden"
        const val CREATED_AT = "created_at"
    }
}
