// domain/repository/BookRepository.kt
package com.example.appthemuse.domain.repository

import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category

interface BookRepository {
    suspend fun getTrendingBooks(limit: Long = 5): List<Book>
    suspend fun getRecentBooks(limit: Long = 5): List<Book>
    suspend fun getRecommendedBooks(favoriteGenres: List<String>, limit: Long = 5): List<Book>
    suspend fun getCategories(): List<Category>
    suspend fun getNewReleaseBooks(limit: Long = 5): List<Book>
    suspend fun getAllBooks(limit: Long = 50): List<Book>
    suspend fun saveSearchHistory(userId: String, keyword: String)
    suspend fun getSearchHistory(userId: String): List<String>
}