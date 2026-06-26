package com.example.appthemuse.domain.model

import com.google.firebase.Timestamp

data class Chapter(
    val id: String = "",
    val book_id: Int = 0,
    val title: String = "",
    val content: String = "",
    val chapter_number: Int = 0,
    val view_count: Long = 0L,
    val created_at: Timestamp? = null,
    val status: String = "đã đăng"
)
