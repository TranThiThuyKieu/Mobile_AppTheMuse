package com.example.appthemuse.domain.repository

import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.model.Chapter
import com.example.appthemuse.domain.model.Review

interface BookRepository {
    suspend fun getTrendingBooks(limit: Long = 5): List<Book>
    suspend fun getRecentBooks(limit: Long = 5): List<Book>
    suspend fun getRecommendedBooks(favoriteGenres: List<String>, limit: Long = 5): List<Book>
    suspend fun getCategories(): List<Category>
    suspend fun getNewReleaseBooks(limit: Long = 5): List<Book>
    suspend fun getAllBooks(limit: Long = 50): List<Book>
    suspend fun saveSearchHistory(userId: String, keyword: String)
    suspend fun getSearchHistory(userId: String): List<String>
    suspend fun getBooksByAuthor(authorId: String): List<Book>
    suspend fun createBook(book: Book, imageUriStr: String?): String
    suspend fun getBookById(bookId: String): Book?
    suspend fun getChapters(bookId: String): List<Chapter>
    suspend fun getVoteCount(bookId: String): Int
    suspend fun getCommentCount(bookId: String): Int
    suspend fun createChapter(bookId: String, title: String, content: String): String
    
    // Progress and View Count
    suspend fun incrementViewCount(bookId: String)
    suspend fun updateReadingProgress(userId: String, bookId: String, chapterNumber: Int, scrollPosition: Int)
    suspend fun getReadingProgress(userId: String, bookId: String): Pair<Int, Int>? // chapterNumber, scrollPosition
    
    // Favorites
    suspend fun toggleFavorite(userId: String, bookId: String)
    suspend fun isBookFavorite(userId: String, bookId: String): Boolean

    // Reviews
    suspend fun getReviews(bookId: String): List<Review>
    suspend fun addReview(bookId: String, userId: String, rating: Int, comment: String)
}