package com.example.appthemuse.domain.model

import java.util.Date

data class ChapterModel(
    val id: Int = 0,
    val bookId: Int = 0,
    val chapterNumber: Int = 0,
    val title: String = "",
    val content: String? = null,
    val audioUrl: String? = null,
    val isDraft: Boolean = false,
    val createdAt: Date = Date()
)