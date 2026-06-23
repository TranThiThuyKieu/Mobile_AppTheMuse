// File: app/src/main/java/com/example/appthemuse/data/repository/BookRepositoryImpl.kt
package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.repository.BookRepository

class BookRepositoryImpl(
    private val firestoreService: FirestoreService
) : BookRepository {

    // Giả sử FirestoreService giờ trả về List<Book> (Domain)
    override suspend fun getTrendingBooks(limit: Long): List<Book> {
        return firestoreService.getTrendingBooks(limit)
    }

    override suspend fun getRecentBooks(limit: Long): List<Book> {
        return firestoreService.getRecentBooks(limit)
    }

    override suspend fun getRecommendedBooks(favoriteGenres: List<String>, limit: Long): List<Book> {
        return firestoreService.getRecommendedBooks(favoriteGenres, limit)
    }

    override suspend fun getCategories(): List<Category> {
        return firestoreService.getCategoriesList()
    }

    override suspend fun getNewReleaseBooks(limit: Long): List<Book> {
        return firestoreService.getNewReleaseBooks(limit)
    }

    override suspend fun getAllBooks(limit: Long): List<Book> {
        return firestoreService.getAllBooks(limit)
    }
}