package com.example.appthemuse.domain.model

import java.util.Date

data class BookModel(
    val id: Int = 0,
    val title: String = "",
    val slug: String = "",
    val authorId: String = "",
    val categoryId: Int = 0,
    val coverUrl: String? = null,
    val description: String? = null,
    val status: String = "pending",
    val isPremium: Boolean = false,
    val viewCount: Int = 0,
    val createdAt: Date = Date()
)