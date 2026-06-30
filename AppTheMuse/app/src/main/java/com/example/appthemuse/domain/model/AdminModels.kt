package com.example.appthemuse.domain.model

import java.util.Date

enum class BookStatus(val value: String, val label: String) {
    Pending("pending", "Cho duyet"),
    Ongoing("ongoing", "Dang cap nhat"),
    Completed("completed", "Da hoan thanh"),
    Hidden("hidden", "Da an");

    companion object {
        fun fromValue(value: String?): BookStatus {
            return values().firstOrNull { it.value == value } ?: Pending
        }
    }
}

data class AdminBook(
    val id: String,
    val title: String,
    val authorId: String,
    val categoryId: String,
    val coverUrl: String,
    val description: String,
    val status: BookStatus,
    val isPremium: Boolean,
    val viewCount: Int,
    val chapterCount: Int,
    val reviewCount: Int,
    val averageRating: Double,
    val createdAt: Date?
)

data class AdminChapter(
    val id: String,
    val bookId: String,
    val chapterNumber: Int,
    val title: String,
    val isDraft: Boolean,
    val createdAt: Date?
)

data class AdminReview(
    val id: String,
    val bookId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val isHidden: Boolean,
    val createdAt: Date?
)

data class AdminBookDetail(
    val book: AdminBook,
    val chapters: List<AdminChapter>
)

data class AdminBookStats(
    val totalBooks: Int,
    val pendingBooks: Int,
    val ongoingBooks: Int,
    val completedBooks: Int,
    val hiddenBooks: Int
)
