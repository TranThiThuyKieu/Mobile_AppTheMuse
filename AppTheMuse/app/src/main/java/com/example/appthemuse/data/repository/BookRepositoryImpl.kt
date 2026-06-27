package com.example.appthemuse.data.repository

import android.util.Log
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.repository.BookRepository
import com.google.firebase.firestore.DocumentSnapshot
import android.net.Uri
import com.google.firebase.Timestamp

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
        val categoryId = doc.get("category_id")?.toString() ?: ""
        return Book(
            id = doc.id,
            title = doc.getString("title") ?: "",
            cover_url = doc.getString("cover_url") ?: "",
            author_name = authorDoc?.getString("username") ?: "Ẩn danh",
            chapter_count = chapterCount,
            rating = rating,
            view_count = doc.getLong("view_count") ?: 0L,
            status = doc.getString("status") ?: "",
            category_id = categoryId
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
    override suspend fun getBooksByAuthor(authorId: String): List<Book> {
        val documents = firestoreService.getBooksByAuthorRaw(authorId)
        return documents.map { mapDocumentToBook(it) }
    }

    override suspend fun createBook(book: Book, imageBase64: String?): String {
        var finalCoverUrl = book.cover_url
        if (!imageBase64.isNullOrEmpty() && !imageBase64.startsWith("http")) {
            finalCoverUrl = firestoreService.uploadBookCoverToImgBB(imageBase64)
        }

        val bookData = hashMapOf<String, Any>(
            "title" to book.title,
            "slug" to book.slug,
            "author_id" to book.author_id,
            "author_name" to book.author_name,
            "chapter_count" to book.chapter_count,
            "rating" to book.rating,
            "category_id" to book.category_id,
            "genres" to listOf(book.category_id),
            "cover_url" to finalCoverUrl,
            "description" to book.description,
            "is_premium" to book.is_premium,
            "view_count" to book.view_count,
            "status" to book.status,
            "created_at" to (book.created_at ?: Timestamp.now())
        )
        return firestoreService.createBookRaw(bookData)
    }

    override suspend fun getBookById(bookId: String): Book? {
        val doc = firestoreService.getBookByDocumentId(bookId)
        return if (doc != null && doc.exists()) {
            mapDocumentToBook(doc)
        } else {
            null
        }
    }

    override suspend fun getChapters(bookId: String): List<com.example.appthemuse.domain.model.Chapter> {
        val documents = firestoreService.getChaptersRaw(bookId)
        return documents.map { doc ->
            com.example.appthemuse.domain.model.Chapter(
                id = doc.id,
                book_id = doc.getLong("book_id")?.toInt() ?: 0,
                title = doc.getString("title") ?: "",
                content = doc.getString("content") ?: "",
                chapter_number = doc.getLong("chapter_number")?.toInt() ?: 0,
                view_count = doc.getLong("view_count") ?: 0L,
                created_at = doc.getTimestamp("created_at"),
                status = doc.getString("status") ?: "đã đăng"
            )
        }.sortedBy { it.chapter_number } // Sort client-side, tránh cần Composite Index trên Firestore
    }

    override suspend fun getVoteCount(bookId: String): Int {
        return firestoreService.getVoteCount(bookId)
    }

    override suspend fun getCommentCount(bookId: String): Int {
        return firestoreService.getCommentCount(bookId)
    }

    override suspend fun createChapter(bookId: String, title: String, content: String): String {
        val chapterData = mapOf<String, Any>(
            "title" to title,
            "content" to content
        )
        return firestoreService.createChapterRaw(bookId, chapterData)
    }
}