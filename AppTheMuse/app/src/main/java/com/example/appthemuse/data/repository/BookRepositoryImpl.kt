package com.example.appthemuse.data.repository

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
    private fun mapDocumentToBook(doc: DocumentSnapshot): Book {
        return Book(
            id = doc.id,
            title = doc.getString("title") ?: doc.getString("tên_sách") ?: "Không có tiêu đề",
            cover_url = doc.getString("cover_url") ?: doc.getString("ảnh_bìa") ?: "",
            author_name = doc.getString("author_name") ?: doc.getString("tác_giả") ?: "Ẩn danh",

            // 1. Giữ nguyên .toInt() vì chapter_count trong Book là Int
            chapter_count = doc.getLong("chapter_count")?.toInt() ?: doc.getLong("số_chương")?.toInt() ?: 0,

            // 2. 👉 SỬA TẠI ĐÂY: Xóa .toFloat() đi vì rating trong Book là Double
            rating = doc.getDouble("rating") ?: doc.getDouble("đánh_giá") ?: 0.0,

            // 3. 👉 SỬA TẠI ĐÂY: Xóa .toInt() đi vì view_count trong Book là Long
            view_count = doc.getLong("view_count") ?: doc.getLong("lượt_xem") ?: 0L,

            status = doc.getString("status") ?: doc.getString("trạng_thái") ?: "Đang tiến hành"
        )
    }
}