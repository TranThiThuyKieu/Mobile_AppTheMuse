package com.example.appthemuse.data.remote.dto

import com.example.appthemuse.domain.model.BookModel
import java.util.Date

data class BookDto(
    val id: String = "", // Document ID từ Firestore nhận dạng String
    val title: String = "",
    val slug: String = "",
    val author_id: String = "",
    val category_id: Int = 0,
    val cover_url: String? = null,
    val description: String? = null,
    val status: String = "pending",
    val is_premium: Boolean = false,
    val view_count: Int = 0,
    val created_at: Date = Date()
) {
    fun toDomain(): BookModel {
        val numericId = id.hashCode() and 0x7FFFFFFF
        return BookModel(
            id = numericId,
            title = title,
            slug = slug,
            authorId = author_id,
            categoryId = category_id,
            coverUrl = cover_url,
            description = description,
            status = status,
            isPremium = is_premium,
            viewCount = view_count,
            createdAt = created_at
        )
    }
}