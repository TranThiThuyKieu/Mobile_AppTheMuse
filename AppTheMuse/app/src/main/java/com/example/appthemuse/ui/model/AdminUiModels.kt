package com.example.appthemuse.ui.model

data class AdminBookUi(
    val id: String,
    val title: String,
    val authorId: String,
    val categoryId: String,
    val coverUrl: String,
    val description: String,
    val statusValue: String,
    val statusLabel: String,
    val isPremium: Boolean,
    val viewCountText: String,
    val chapterCountText: String,
    val reviewCountText: String,
    val ratingText: String,
    val createdAtText: String
)

data class AdminChapterUi(
    val id: String,
    val title: String,
    val chapterNumberText: String,
    val stateText: String,
    val createdAtText: String
)

data class AdminReviewUi(
    val id: String,
    val userId: String,
    val ratingText: String,
    val comment: String,
    val isHidden: Boolean,
    val statusText: String,
    val createdAtText: String
)

data class AdminBookStatsUi(
    val total: String,
    val pending: String,
    val ongoing: String,
    val completed: String,
    val hidden: String
)
