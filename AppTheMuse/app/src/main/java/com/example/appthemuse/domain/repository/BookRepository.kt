package com.example.appthemuse.domain.repository

import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.model.Chapter
import com.example.appthemuse.domain.model.Review

interface BookRepository {
    suspend fun getTrendingBooks(limit: Long = 50): List<Book>
    suspend fun updateBookStatus(bookId: String, status: String)
    suspend fun hideBook(bookId: String, currentStatus: String)
    suspend fun unhideBook(bookId: String, previousStatus: String)
    suspend fun getRecentBooks(limit: Long = 50): List<Book>
    suspend fun getRecommendedBooks(favoriteGenres: List<String>, limit: Long = 50): List<Book>
    suspend fun getCategories(): List<Category>
    suspend fun getNewReleaseBooks(limit: Long = 50): List<Book>
    suspend fun getAllBooks(limit: Long = 1000): List<Book>
    suspend fun saveSearchHistory(userId: String, keyword: String)
    suspend fun getSearchHistory(userId: String): List<String>
    suspend fun getBooksByAuthor(authorId: String): List<Book>
    suspend fun createBook(book: Book, imageBase64: String? = null): String
    suspend fun getBookById(bookId: String): Book?
    suspend fun getChapters(bookId: String): List<Chapter>
    suspend fun getVoteCount(bookId: String): Int
    suspend fun getCommentCount(bookId: String): Int
    suspend fun createChapter(bookId: String, title: String, content: String): String
    suspend fun incrementViewCount(bookId: String)
    suspend fun updateReadingProgress(userId: String, bookId: String, chapterNumber: Int, scrollPosition: Int)
    suspend fun getReadingProgress(userId: String, bookId: String): Pair<Int, Int>?
    suspend fun toggleFavorite(userId: String, bookId: String)
    suspend fun isBookFavorite(userId: String, bookId: String): Boolean
    suspend fun getReviews(bookId: String): List<Review>
    suspend fun addReview(bookId: String, userId: String, rating: Int, comment: String)
}