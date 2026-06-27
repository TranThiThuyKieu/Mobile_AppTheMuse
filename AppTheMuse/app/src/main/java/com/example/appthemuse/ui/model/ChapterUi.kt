package com.example.appthemuse.ui.model

import com.google.firebase.Timestamp

data class ChapterUi(
    val id: String,
    val book_id: String,
    val title: String,
    val content: String,
    val chapter_number: Int,
    val view_count: Long,
    val created_at: Timestamp?,
    val status: String
)
