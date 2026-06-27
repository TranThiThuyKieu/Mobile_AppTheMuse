package com.example.appthemuse.ui.model

import com.google.firebase.Timestamp

data class BookUi(
    val id: String,
    val title: String,
    val cover_url: String,
    val author_name: String,
    val chapter_count: Int,
    val rating: Double,
    val view_count: Long,
    val status: String,
    val category_id: String,
    val description: String,
    val progressPercent: Int = 0,
    val lastReadAt: Timestamp? = null,
    val hasUpdate: Boolean = false
)
