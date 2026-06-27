package com.example.appthemuse.domain.repository

import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.model.Chapter

interface BookRepository {
    // Sách và danh sách
    suspend fun getAllBooks(limit: Long = 20): List<Book>
    suspend fun getNewReleaseBooks(limit: Long = 20): List<Book>
    suspend fun getTrendingBooks(limit: Long = 20): List<Book>
    suspend fun getRecentBooks(limit: Long = 20): List<Book>
    suspend fun getRecommendedBooks(favoriteGenres: List<String>, limit: Long = 20): List<Book>
    suspend fun getCategories(): List<Category>
    suspend fun getBookById(bookId: String): Book?
    
    // Chương
    suspend fun getChapters(bookId: String): List<Chapter>
    suspend fun createChapter(bookId: String, title: String, content: String): String
    
    // Tiến độ và Bookmark
    suspend fun getReadingProgress(userId: String, bookId: String): Pair<Int, Int>?
    suspend fun updateReadingProgress(userId: String, bookId: String, chapterNumber: Int, scrollPosition: Int)
    suspend fun incrementViewCount(bookId: String)
    suspend fun addBookmark(userId: String, bookId: String, chapterNumber: Int)
    suspend fun removeBookmark(userId: String, bookId: String, chapterNumber: Int)
    suspend fun isBookmarked(userId: String, bookId: String, chapterNumber: Int): Boolean

    // Lịch sử tìm kiếm
    suspend fun getSearchHistory(userId: String): List<String>
    suspend fun saveSearchHistory(userId: String, keyword: String)

    // Creator / Studio
    suspend fun createBook(book: Book, coverBase64: String?): String
    suspend fun getBooksByAuthor(authorId: String): List<Book>
    suspend fun getVoteCount(bookId: String): Int
    suspend fun getCommentCount(bookId: String): Int
    suspend fun uploadBookCover(base64Image: String): String
}
