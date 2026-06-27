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
            val number = try { favorite.getLong("book_id")?.toInt() } catch (e: Exception) { null }
            val bookIdStr = try { favorite.getString("book_id") } catch (e: Exception) { null }
            val finalId = if (number != null) "book$number" else (bookIdStr ?: continue)
            
            val bookDoc = firestoreService.getBookByDocumentId(finalId) ?: continue
            books.add(mapDocumentToBook(bookDoc))
        }
        return books
    }

    override suspend fun getHistoryBooks(userId: String): List<HistoryUi> {
        val historyDocs = firestoreService.getHistoryDocuments(userId)
        val result = mutableListOf<HistoryUi>()

        // Sắp xếp giảm dần theo thời gian đọc (mới nhất lên đầu)
        val sortedDocs = historyDocs.sortedByDescending { it.getTimestamp("read_at")?.seconds ?: 0 }
        
        // Lọc để chỉ giữ lại document đầu tiên (mới nhất) cho mỗi book_id
        val uniqueDocs = sortedDocs.distinctBy {
            val number = try { it.getLong("book_id")?.toInt() } catch (e: Exception) { null }
            val bookIdStr = try { it.getString("book_id") } catch (e: Exception) { null }
            if (number != null) "book$number" else bookIdStr
        }

        for (history in uniqueDocs) {
            val number = try { history.getLong("book_id")?.toInt() } catch (e: Exception) { null }
            val bookIdStr = try { history.getString("book_id") } catch (e: Exception) { null }
            val finalId = if (number != null) "book$number" else (bookIdStr ?: continue)
            
            val bookDoc = firestoreService.getBookByDocumentId(finalId) ?: continue
            
            val progressDoc = firestoreService.getReadingProgress(userId, finalId)
            val currentChapter = progressDoc?.getLong("chapter_number")?.toInt() ?: 1
            val totalChapters = bookDoc.getLong("chapter_count")?.toInt() ?: 0

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
        return result
    }

    private fun mapDocumentToBook(doc: DocumentSnapshot): Book {
        val categoryId = doc.get("category_id")?.toString() ?: ""
        return Book(
            id = doc.id,
            title = doc.getString("title") ?: "",
            cover_url = doc.getString("cover_url") ?: "",
            author_name = doc.getString("author_name") ?: "Ẩn danh",
            chapter_count = doc.getLong("chapter_count")?.toInt() ?: 0,
            rating = (doc.get("rating") as? Number)?.toDouble() ?: 0.0,
            view_count = doc.getLong("view_count") ?: 0L,
            status = doc.getString("status") ?: "",
            category_id = categoryId,
            description = doc.getString("description") ?: doc.getString("mô_tả") ?: ""
        )
    }
}
