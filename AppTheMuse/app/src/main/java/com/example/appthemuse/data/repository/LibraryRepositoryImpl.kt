package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.repository.LibraryRepository
import com.example.appthemuse.ui.mapper.toBookUi
import com.example.appthemuse.ui.model.HistoryUi
import com.google.firebase.firestore.DocumentSnapshot

class LibraryRepositoryImpl(
    private val firestoreService: FirestoreService
) : LibraryRepository {

    override suspend fun getFavoriteBooks(userId: String): List<Book> {
        val favoriteDocs = firestoreService.getFavoriteDocuments(userId)
        val books = mutableListOf<Book>()
        for (favorite in favoriteDocs) {
            val bookId = favorite.getString("book_id") ?: continue
            val bookDoc = firestoreService.getBookByDocumentId(bookId) ?: continue
            books.add(mapDocumentToBook(bookDoc))
        }
        return books
    }

    override suspend fun getHistoryBooks(userId: String): List<HistoryUi> {
        val historyDocs = firestoreService.getHistoryDocuments(userId)
        val result = mutableListOf<HistoryUi>()

        for (history in historyDocs) {
            val bookId = history.getString("book_id") ?: continue
            val bookDoc = firestoreService.getBookByDocumentId(bookId) ?: continue
            
            val progressDoc = firestoreService.getReadingProgress(userId, bookId)
            val currentChapter = progressDoc?.getLong("chapter_number")?.toInt() ?: 1
            val totalChapters = bookDoc.getLong("chapter_count")?.toInt() ?: 1

            val percent = if (totalChapters > 0) {
                ((currentChapter.toFloat() / totalChapters.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else 0

            result.add(
                HistoryUi(
                    book = mapDocumentToBook(bookDoc).toBookUi().copy(progressPercent = percent),
                    progressPercent = percent,
                    lastReadAt = history.getTimestamp("read_at")
                )
            )
        }
        return result.sortedByDescending { it.lastReadAt }
    }

    override suspend fun isFavorite(userId: String, bookId: String): Boolean {
        return firestoreService.isFavorite(userId, bookId)
    }

    override suspend fun addFavorite(userId: String, bookId: String) {
        firestoreService.addFavorite(userId, bookId)
    }

    override suspend fun removeFavorite(userId: String, bookId: String) {
        firestoreService.removeFavorite(userId, bookId)
    }

    private fun mapDocumentToBook(doc: DocumentSnapshot): Book {
        return Book(
            id = doc.id,
            title = doc.getString("title") ?: "",
            cover_url = doc.getString("cover_url") ?: "",
            author_name = doc.getString("author_name") ?: "Ẩn danh",
            chapter_count = doc.getLong("chapter_count")?.toInt() ?: 0,
            rating = (doc.get("rating") as? Number)?.toDouble() ?: 0.0,
            view_count = doc.getLong("view_count") ?: 0L,
            status = doc.getString("status") ?: "",
            category_id = doc.get("category_id")?.toString() ?: "",
            description = doc.getString("description") ?: ""
        )
    }
}
