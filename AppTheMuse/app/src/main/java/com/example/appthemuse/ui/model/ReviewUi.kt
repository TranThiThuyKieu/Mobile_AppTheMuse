package com.example.appthemuse.ui.model

import com.google.firebase.Timestamp

data class ReviewUi(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val rating: Int,
    val comment: String,
    val createdAt: Timestamp?
)
