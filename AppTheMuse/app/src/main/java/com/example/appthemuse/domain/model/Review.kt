package com.example.appthemuse.domain.model

import com.google.firebase.Timestamp

data class Review(
    val id: String = "",
    val book_id: String = "",
    val user_id: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val created_at: Timestamp? = null,
    val is_hidden: Boolean = false,
    val user_name: String = "",
    val user_avatar: String = ""
)
