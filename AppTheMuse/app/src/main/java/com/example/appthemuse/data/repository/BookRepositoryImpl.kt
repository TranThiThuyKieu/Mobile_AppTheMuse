package com.example.appthemuse.data.repository

import android.util.Log
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.model.Chapter
import com.example.appthemuse.domain.model.Review
import com.example.appthemuse.domain.repository.BookRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.Timestamp
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class BookRepositoryImpl(
    private val firestoreService: FirestoreService
) : BookRepository {

    override suspend fun getTrendingBooks(limit: Long): List<Book> = coroutineScope {
        val documents = firestoreService.getTrendingBooksRaw(limit)
        documents.map { async { mapDocumentToBook(it) } }.awaitAll()
    }

    override suspend fun getRecentBooks(limit: Long): List<Book> = coroutineScope {
        val documents = firestoreService.getRecentBooksRaw(limit)
        documents.map { async { mapDocumentToBook(it) } }.awaitAll()
    }

    override suspend fun getRecommendedBooks(favoriteGenres: List<String>, limit: Long): List<Book> = coroutineScope {
        val documents = firestoreService.getRecommendedBooksRaw(favoriteGenres, limit)
        documents.map { async { mapDocumentToBook(it) } }.awaitAll()
    }

    override suspend fun getCategories(): List<Category> {
        return firestoreService.getCategoriesListRaw().map { doc ->
            Category(
                id = doc.get("id")?.toString() ?: doc.id,
                name = doc.getString("name") ?: doc.getString("tên_thể_loại") ?: "Chưa phân loại",
                totalBooks = doc.getLong("total_books")?.toInt() ?: doc.getLong("số_lượng_sách")?.toInt() ?: 0
            )
        }
    }

    override suspend fun getNewReleaseBooks(limit: Long): List<Book> = coroutineScope {
        val documents = firestoreService.getNewReleaseBooksRaw(limit)
        documents.map { async { mapDocumentToBook(it) } }.awaitAll()
    }

    override suspend fun getAllBooks(limit: Long): List<Book> = coroutineScope {
        val documents = firestoreService.getAllBooksRaw(limit)
        documents.map { async { mapDocumentToBook(it) } }.awaitAll()
    }

    private suspend fun mapDocumentToBook(doc: DocumentSnapshot): Book {
        val docId = doc.id
        val chapterCount = doc.get("chapter_count")?.toString()?.toIntOrNull() ?: firestoreService.getChapterCount(docId)
        val rating = doc.get("rating")?.toString()?.toDoubleOrNull() ?: firestoreService.getAverageRating(docId)
        
        return Book(
            id = docId,
            title = doc.getString("title") ?: doc.getString("tên_sách") ?: "",
            slug = doc.getString("slug") ?: "",
            author_id = doc.getString("author_id") ?: "",
            author_name = doc.getString("author_name") ?: doc.getString("tác_giả") ?: "Ẩn danh",
            chapter_count = chapterCount,
            rating = rating,
            category_id = doc.get("category_id")?.toString() ?: "",
            cover_url = doc.getString("cover_url") ?: doc.getString("ảnh_bìa") ?: "",
            description = doc.getString("description") ?: doc.getString("mô_tả") ?: "",
            is_premium = doc.getBoolean("is_premium") ?: false,
            view_count = doc.get("view_count")?.toString()?.toLongOrNull() ?: 0L,
            status = doc.getString("status") ?: "",
            created_at = doc.getTimestamp("created_at")
        )
    }

    override suspend fun saveSearchHistory(userId: String, keyword: String) {
        firestoreService.addSearchHistory(userId, keyword)
    }

    override suspend fun getSearchHistory(userId: String): List<String> {
        return firestoreService.getSearchHistory(userId)
    }

    override suspend fun getBooksByAuthor(authorId: String): List<Book> = coroutineScope {
        val documents = firestoreService.getBooksByAuthorRaw(authorId)
        documents.map { async { mapDocumentToBook(it) } }.awaitAll()
    }

    override suspend fun createBook(book: Book, imageBase64: String?): String {
        var finalCoverUrl = book.cover_url
        if (!imageBase64.isNullOrEmpty() && !imageBase64.startsWith("http")) {
            finalCoverUrl = firestoreService.uploadBookCoverToImgBB(imageBase64)
        }
        val categoryIdLong = book.category_id.toLongOrNull() ?: 0L
        val bookData = hashMapOf<String, Any>(
            "title" to book.title, "slug" to book.slug, "author_id" to book.author_id,
            "author_name" to book.author_name, "chapter_count" to book.chapter_count,
            "rating" to book.rating, "category_id" to categoryIdLong, "genres" to listOf(categoryIdLong),
            "cover_url" to finalCoverUrl, "description" to book.description, "is_premium" to book.is_premium,
            "view_count" to book.view_count, "status" to book.status, "created_at" to (book.created_at ?: Timestamp.now())
        )
        return firestoreService.createBookRaw(bookData)
    }

    override suspend fun getBookById(bookId: String): Book? {
        val doc = firestoreService.getBookByDocumentId(bookId)
        return if (doc != null && doc.exists()) mapDocumentToBook(doc) else null
    }

    override suspend fun getChapters(bookId: String): List<Chapter> {
        return firestoreService.getChaptersRaw(bookId).map { doc ->
            Chapter(
                id = doc.id,
                book_id = doc.get("book_id")?.toString()?.toIntOrNull() ?: 0,
                title = doc.getString("title") ?: "",
                content = doc.getString("content") ?: "",
                chapter_number = doc.get("chapter_number")?.toString()?.toIntOrNull() ?: 0,
                view_count = doc.get("view_count")?.toString()?.toLongOrNull() ?: 0L,
                created_at = doc.getTimestamp("created_at"),
                status = doc.getString("status") ?: "đã đăng"
            )
        }.sortedBy { it.chapter_number }
    }

    override suspend fun getVoteCount(bookId: String): Int = firestoreService.getVoteCount(bookId)
    override suspend fun getCommentCount(bookId: String): Int = firestoreService.getCommentCount(bookId)

    override suspend fun createChapter(bookId: String, title: String, content: String): String {
        return firestoreService.createChapterRaw(bookId, mapOf("title" to title, "content" to content))
    }

    override suspend fun incrementViewCount(bookId: String) {
        try { firestoreService.incrementViewCount(bookId) } catch (e: Exception) { }
    }

    override suspend fun updateReadingProgress(userId: String, bookId: String, chapterNumber: Int, scrollPosition: Int) {
        try { firestoreService.updateReadingProgress(userId, bookId, chapterNumber, scrollPosition) } catch (e: Exception) { }
    }

    override suspend fun getReadingProgress(userId: String, bookId: String): Pair<Int, Int>? {
        return try {
            val doc = firestoreService.getReadingProgress(userId, bookId)
            if (doc != null) {
                val chapterNumber = doc.get("chapter_number")?.toString()?.toIntOrNull() ?: 1
                val scrollPosition = doc.get("scroll_position")?.toString()?.toIntOrNull() ?: 0
                Pair(chapterNumber, scrollPosition)
            } else null
        } catch (e: Exception) { null }
    }

    override suspend fun toggleFavorite(userId: String, bookId: String) = firestoreService.toggleFavorite(userId, bookId)

    override suspend fun isBookFavorite(userId: String, bookId: String): Boolean {
        return firestoreService.getFavoriteDocuments(userId).any { doc ->
            val idRaw = doc.get("book_id")
            val idStr = when (idRaw) {
                is Number -> "book${idRaw.toInt()}"
                is String -> if (idRaw.startsWith("book")) idRaw else "book$idRaw"
                else -> ""
            }
            idStr == bookId
        }
    }

    override suspend fun getReviews(bookId: String): List<Review> = coroutineScope {
        val documents = firestoreService.getReviewsRaw(bookId)
        documents.map { doc ->
            async {
                val userId = doc.getString("user_id") ?: ""
                val userDoc = firestoreService.getUserById(userId)
                Review(
                    id = doc.id,
                    book_id = doc.get("book_id")?.toString() ?: "",
                    user_id = userId,
                    rating = doc.get("rating")?.toString()?.toIntOrNull() ?: 0,
                    comment = doc.getString("comment") ?: "",
                    created_at = doc.getTimestamp("created_at"),
                    is_hidden = doc.getBoolean("is_hidden") ?: false,
                    user_name = userDoc?.getString("name") ?: userDoc?.getString("tên_người_dùng") ?: "Người dùng",
                    user_avatar = userDoc?.getString("avatar_url") ?: userDoc?.getString("ảnh_đại_diện") ?: ""
                )
            }
        }.awaitAll()
    }

    override suspend fun addReview(bookId: String, userId: String, rating: Int, comment: String) {
        val bookNumId = bookId.removePrefix("book").toIntOrNull()
        val reviewData = hashMapOf(
            "book_id" to (bookNumId ?: bookId),
            "user_id" to userId,
            "rating" to rating,
            "comment" to comment,
            "created_at" to Timestamp.now(),
            "is_hidden" to false
        )
        firestoreService.addReview(reviewData)
    }
}
