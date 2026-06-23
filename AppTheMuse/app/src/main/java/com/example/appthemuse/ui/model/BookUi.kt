package com.example.appthemuse.ui.model

data class BookUi(
    val id: String,
    val title: String,
    val cover_url: String,
    val author_name: String,
    val chapter_count: Int,
    val rating: Double,
    val view_count: Long,
    val status: String
)