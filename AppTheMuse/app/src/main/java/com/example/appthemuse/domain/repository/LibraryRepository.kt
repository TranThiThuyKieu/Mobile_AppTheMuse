package com.example.appthemuse.domain.repository

import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.ui.model.HistoryUi

interface LibraryRepository {
    suspend fun getFavoriteBooks(userId: String): List<Book>
    suspend fun getHistoryBooks(
        userId: String
    ): List<HistoryUi>
}