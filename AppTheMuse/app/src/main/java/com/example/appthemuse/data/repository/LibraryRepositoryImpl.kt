package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.repository.LibraryRepository
import com.example.appthemuse.ui.mapper.toBookUi
import com.example.appthemuse.ui.model.HistoryUi
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class LibraryRepositoryImpl(
    private val firestoreService: FirestoreService
) : LibraryRepository {

    override suspend fun getFavoriteBooks(userId: String): List<Book> = coroutineScope {
        val favoriteDocs = firestoreService.getFavoriteDocuments(userId)
        favoriteDocs.map { favorite ->
            async {
                val bookIdRaw = favorite.get("book_id")
                val finalId = when (bookIdRaw) {
                    is Number -> "book${bookIdRaw.toInt()}"
                    is String -> if (bookIdRaw.startsWith("book")) bookIdRaw else "book$bookIdRaw"
                    else -> return@async null
                }

                val bookDoc = firestoreService.getBookByDocumentId(finalId) ?: return@async null
                mapDocumentToBook(bookDoc)
            }
        }.awaitAll().filterNotNull()
    }

    override suspend fun getHistoryBooks(userId: String): List<HistoryUi> = coroutineScope {
        val historyDocs = firestoreService.getHistoryDocuments(userId)
        historyDocs.map { history ->
            async {
                val bookIdRaw = history.get("book_id")
                val finalId = when (bookIdRaw) {
                    is Number -> "book${bookIdRaw.toInt()}"
                    is String -> if (bookIdRaw.startsWith("book")) bookIdRaw else "book$bookIdRaw"
                    else -> return@async null
                }

                val bookDoc = firestoreService.getBookByDocumentId(finalId) ?: return@async null

                val progressDoc = firestoreService.getReadingProgress(userId, finalId)
                val currentChapter = progressDoc?.getLong("chapter_number")?.toInt() ?: 1
                
                val book = mapDocumentToBook(bookDoc)
                val totalChapters = book.chapter_count

                val percent = if (totalChapters > 0) {
                    ((currentChapter.toFloat() / totalChapters.toFloat()) * 100).toInt().coerceIn(0, 100)
                } else 0

                HistoryUi(
                    book = book.toBookUi().copy(progressPercent = percent),
                    progressPercent = percent,
                    lastReadAt = history.getTimestamp("read_at")
                )
            }
        }.awaitAll().filterNotNull().sortedByDescending { it.lastReadAt }
    }

    private suspend fun mapDocumentToBook(doc: DocumentSnapshot): Book {
        val docId = doc.id
        
        val chapterCount = (doc.get("chapter_count") as? Number)?.toInt()
            ?: firestoreService.getChapterCount(docId)
            
        val rating = (doc.get("rating") as? Number)?.toDouble() 
            ?: firestoreService.getAverageRating(docId)

        val categoryId = doc.get("category_id")?.toString() ?: ""
        
        return Book(
            id = docId,
            title = doc.getString("title") ?: doc.getString("tên_sách") ?: "",
            cover_url = doc.getString("cover_url") ?: doc.getString("ảnh_bìa") ?: "",
            author_name = doc.getString("author_name") ?: doc.getString("tác_giả") ?: "Ẩn danh",
            chapter_count = chapterCount,
            rating = rating,
            view_count = (doc.get("view_count") as? Number)?.toLong() ?: 0L,
            status = doc.getString("status") ?: "",
            category_id = categoryId,
            description = doc.getString("description") ?: doc.getString("mô_tả") ?: ""
        )
    }
}
