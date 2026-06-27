package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.AdminBookRemoteDataSource
import com.example.appthemuse.domain.model.AdminBook
import com.example.appthemuse.domain.model.AdminBookDetail
import com.example.appthemuse.domain.model.AdminBookStats
import com.example.appthemuse.domain.model.AdminReview
import com.example.appthemuse.domain.model.BookStatus
import com.example.appthemuse.domain.repository.AdminBookRepository

class AdminBookRepositoryImpl(
    private val remoteDataSource: AdminBookRemoteDataSource = AdminBookRemoteDataSource()
) : AdminBookRepository {
    override suspend fun getBooks(status: BookStatus?, keyword: String): List<AdminBook> {
        val books = remoteDataSource.getBooks(status)
        val normalizedKeyword = keyword.trim().lowercase()
        if (normalizedKeyword.isBlank()) return books

        return books.filter { book ->
            book.title.lowercase().contains(normalizedKeyword) ||
                book.authorId.lowercase().contains(normalizedKeyword)
        }
    }

    override suspend fun getBookStats(): AdminBookStats {
        return remoteDataSource.getBookStats()
    }

    override suspend fun updateBookStatus(bookId: String, status: BookStatus) {
        remoteDataSource.updateBookStatus(bookId, status)
    }

    override suspend fun getBookDetail(bookId: String): AdminBookDetail {
        return remoteDataSource.getBookDetail(bookId)
    }

    override suspend fun getReviews(bookId: String, includeHidden: Boolean): List<AdminReview> {
        return remoteDataSource.getReviews(bookId, includeHidden)
    }

    override suspend fun setReviewHidden(bookId: String, reviewId: String, hidden: Boolean) {
        remoteDataSource.setReviewHidden(bookId, reviewId, hidden)
    }
}
