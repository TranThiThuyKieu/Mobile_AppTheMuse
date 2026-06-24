package com.example.appthemuse.domain.repository

import com.example.appthemuse.domain.model.Book

interface LibraryRepository {
    suspend fun getFavoriteBooks(userId: String): List<Book>
    suspend fun getHistoryBooks(
        userId: String
    ): List<Book>
}