package com.example.appthemuse.domain.repository

import com.example.appthemuse.domain.model.AdminBook
import com.example.appthemuse.domain.model.AdminBookDetail
import com.example.appthemuse.domain.model.AdminBookStats
import com.example.appthemuse.domain.model.AdminReview
import com.example.appthemuse.domain.model.BookStatus

interface AdminBookRepository {
    suspend fun getBooks(status: BookStatus? = null, keyword: String = ""): List<AdminBook>
    suspend fun getBookStats(): AdminBookStats
    suspend fun updateBookStatus(bookId: String, status: BookStatus)
    suspend fun getBookDetail(bookId: String): AdminBookDetail
    suspend fun getReviews(bookId: String, includeHidden: Boolean = true): List<AdminReview>
    suspend fun setReviewHidden(bookId: String, reviewId: String, hidden: Boolean)
}
