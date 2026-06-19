package com.example.appthemuse.data.remote.dto

import com.example.appthemuse.domain.model.ReviewModel
import java.util.Date

data class ReviewDto(
        val id: String = "",
        val user_id: String = "",
        val book_id: Int = 0,
        val rating: Int = 5,
        val comment: String? = null,
        val is_hidden: Boolean = false,
        val created_at: Date = Date()
) {
fun toDomain(): ReviewModel {
    val numericId = id.hashCode() and 0x7FFFFFFF
    return ReviewModel(
            id = numericId,
            userId = user_id,
            bookId = book_id,
            rating = rating,
            comment = comment,
            isHidden = is_hidden,
            createdAt = created_at
    )
}
}