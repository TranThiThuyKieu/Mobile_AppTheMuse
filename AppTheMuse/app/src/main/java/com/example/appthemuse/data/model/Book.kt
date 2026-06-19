package com.example.appthemuse.data.model

import com.google.firebase.Timestamp

data class Book(
    val id: Long = 0,
    val title: String = "",
    val slug: String = "",
    val author_id: String = "",
    val category_id: Long = 0L,
    val cover_url: String = "",
    val description: String = "",
    val is_premium: Boolean = false,
    val view_count: Long = 0L,
    val status: String = "",
    val created_at: Timestamp? = null
)