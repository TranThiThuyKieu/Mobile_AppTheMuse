package com.example.appthemuse.data.repository

import android.util.Log
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.repository.BookRepository
import com.google.firebase.firestore.DocumentSnapshot

class BookRepositoryImpl(
    private val firestoreService: FirestoreService
) : BookRepository {

    override suspend fun getTrendingBooks(limit: Long): List<Book> {
        // Giả sử service trả về List<DocumentSnapshot> thô từ Firestore
        val documents = firestoreService.getTrendingBooksRaw(limit)
        return documents.map { mapDocumentToBook(it) }
    }

    override suspend fun getRecentBooks(limit: Long): List<Book> {
        val documents = firestoreService.getRecentBooksRaw(limit)
        return documents.map { mapDocumentToBook(it) }
    }

    override suspend fun getRecommendedBooks(favoriteGenres: List<String>, limit: Long): List<Book> {
        val documents = firestoreService.getRecommendedBooksRaw(favoriteGenres, limit)
        return documents.map { mapDocumentToBook(it) }
    }

    override suspend fun getCategories(): List<Category> {
        val documents = firestoreService.getCategoriesListRaw()
        return documents.map { doc ->
            Category(
                id = doc.id,
                name = doc.getString("name") ?: doc.getString("tên_thể_loại") ?: "Chưa phân loại",
                totalBooks = doc.getLong("total_books")?.toInt() ?: doc.getLong("số_lượng_sách")?.toInt() ?: 0
            )
        }
    }

    override suspend fun getNewReleaseBooks(limit: Long): List<Book> {
        val documents = firestoreService.getNewReleaseBooksRaw(limit)
        return documents.map { mapDocumentToBook(it) }
    }

    override suspend fun getAllBooks(limit: Long): List<Book> {
        val documents = firestoreService.getAllBooksRaw(limit)
        return documents.map { mapDocumentToBook(it) }
    }

    // 👉 Hàm tiện ích nội bộ tách biệt hoàn toàn cấu trúc Firebase Database và Domain Model
    private suspend fun mapDocumentToBook(doc: DocumentSnapshot): Book {
        Log.d("BOOK_ID",doc.id)
        println(doc.id)
        val authorId = doc.getString("author_id") ?: ""
        val authorDoc = firestoreService.getUserById(authorId)
        val chapterCount = firestoreService.getChapterCount(doc.id)
        val rating = firestoreService.getAverageRating(doc.id)
        return Book(
            id = doc.id,
            title = doc.getString("title") ?: "",
            cover_url = doc.getString("cover_url") ?: "",
            author_name = authorDoc?.getString("username") ?: "Ẩn danh",
            chapter_count = chapterCount,
            rating = rating,
            view_count = doc.getLong("view_count") ?: 0L,
            status = doc.getString("status") ?: ""
        )
    }
    // Hàm lưu lịch sử tìm kiếm
    override suspend fun saveSearchHistory(userId: String, keyword: String) {
        firestoreService.addSearchHistory(userId, keyword)
    }
    // Hàm lấy lịch sử tìm kiếm
    override suspend fun getSearchHistory(userId: String): List<String> {
        return firestoreService.getSearchHistory(userId)
    }
}