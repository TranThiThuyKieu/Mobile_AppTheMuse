package com.example.appthemuse.domain.model

import java.util.Date

data class FavoriteModel(
    val id: Int = 0,
    val userId: String = "",
    val bookId: Int = 0,
    val createdAt: Date = Date()
)

data class HistoryModel(
    val id: Int = 0,
    val userId: String = "",
    val bookId: Int = 0,
    val readAt: Date = Date()
)

data class ReadingProgressModel(
    val id: Int = 0,
    val userId: String = "",
    val bookId: Int = 0,
    val chapterId: Int = 0,
    val scrollPosition: Int = 0
)