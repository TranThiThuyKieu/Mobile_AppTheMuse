package com.example.appthemuse.domain.model

import java.util.Date

data class ReviewModel(
    val id: Int = 0,
    val userId: String = "",
    val bookId: Int = 0,
    val rating: Int = 5,
    val comment: String? = null,
    val isHidden: Boolean = false,
    val createdAt: Date = Date()
)