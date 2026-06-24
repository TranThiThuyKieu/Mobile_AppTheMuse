package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.repository.LibraryRepository
import com.google.firebase.firestore.DocumentSnapshot

class LibraryRepositoryImpl(
    private val firestoreService: FirestoreService
) : LibraryRepository {
    // Lấy danh sách truyện yêu thích của người dùng
    override suspend fun getFavoriteBooks(userId: String): List<Book> {
        // Danh sách favorite của user
        val favoriteDocs = firestoreService.getFavoriteDocuments(userId)
        val books = mutableListOf<Book>()
        for (favorite in favoriteDocs) {
            // Lấy book_id từ favorite
            val number = favorite.getLong("book_id")?.toInt() ?: continue
            val bookDoc = firestoreService.getBookByDocumentId("book$number") ?: continue
            books.add(mapDocumentToBook(bookDoc))
        }
        return books
    }
    override suspend fun getHistoryBooks(
        userId: String
    ): List<Book> {

        val historyDocs =
            firestoreService.getHistoryDocuments(userId)

        val books = mutableListOf<Book>()

        for (history in historyDocs) {

            val number =
                history.getLong("book_id")
                    ?.toInt()
                    ?: continue

            val bookDoc =
                firestoreService.getBookByDocumentId(
                    "book$number"
                ) ?: continue

            books.add(
                mapDocumentToBook(bookDoc)
            )

        }

        return books
    }
    private suspend fun mapDocumentToBook(doc: DocumentSnapshot): Book {
        val authorId = doc.getString("author_id") ?: ""
        val author = firestoreService.getUserById(authorId)
        val chapterCount = firestoreService.getChapterCount(doc.id)
        val rating = firestoreService.getAverageRating(doc.id)
        return Book(
            id = doc.id,
            title = doc.getString("title") ?: "",
            cover_url = doc.getString("cover_url") ?: "",
            author_name = author?.getString("username") ?: "Ẩn danh",
            chapter_count = chapterCount,
            rating = rating,
            view_count = doc.getLong("view_count") ?: 0,
            status = doc.getString("status") ?: "",
            category_id = doc.getLong("category_id")?.toString() ?: ""
        )
    }
}