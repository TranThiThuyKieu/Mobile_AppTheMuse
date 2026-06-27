package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.model.Chapter
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
                imageUrl = doc.getString("imageUrl") ?: "",
                totalBooks = (doc.get("totalBooks") as? Number)?.toInt() ?: 0
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
        val chapterCount = (doc.get("chapter_count") as? Number)?.toInt()
            ?: firestoreService.getChapterCount(docId)
        val rating = (doc.get("rating") as? Number)?.toDouble() 
            ?: firestoreService.getAverageRating(docId)
        
        // Fix: Read category_id as Number and convert to String to avoid crash
        val categoryId = doc.get("category_id")?.toString() ?: ""
        
        return Book(
            id = docId,
            title = doc.getString("title") ?: "",
            cover_url = doc.getString("cover_url") ?: "",
            author_name = doc.getString("author_name") ?: "Ẩn danh",
            chapter_count = chapterCount,
            rating = rating,
            view_count = (doc.get("view_count") as? Number)?.toLong() ?: 0L,
            status = doc.getString("status") ?: "",
            category_id = categoryId,
            description = doc.getString("description") ?: ""
        )
    }

    override suspend fun getBookById(bookId: String): Book? {
        val doc = firestoreService.getBookByDocumentId(bookId)
        return if (doc != null && doc.exists()) mapDocumentToBook(doc) else null
    }

    override suspend fun getChapters(bookId: String): List<Chapter> {
        return firestoreService.getChaptersRaw(bookId).map { doc ->
            Chapter(
                id = doc.id,
                book_id = bookId,
                title = doc.getString("title") ?: "",
                content = doc.getString("content") ?: "",
                chapter_number = (doc.get("chapter_number") as? Number)?.toInt() ?: 0,
                created_at = doc.getTimestamp("created_at")
            )
        }.sortedBy { it.chapter_number }
    }

    override suspend fun createChapter(bookId: String, title: String, content: String): String {
        val chapterData = mapOf("title" to title, "content" to content)
        return firestoreService.createChapterRaw(bookId, chapterData)
    }

    override suspend fun incrementViewCount(bookId: String) {
        firestoreService.incrementViewCount(bookId)
    }

    override suspend fun updateReadingProgress(userId: String, bookId: String, chapterNumber: Int, scrollPosition: Int) {
        firestoreService.updateReadingProgress(userId, bookId, chapterNumber, scrollPosition)
    }

    override suspend fun getReadingProgress(userId: String, bookId: String): Pair<Int, Int>? {
        val doc = firestoreService.getReadingProgress(userId, bookId) ?: return null
        return Pair(
            (doc.get("chapter_number") as? Number)?.toInt() ?: 1,
            (doc.get("scroll_position") as? Number)?.toInt() ?: 0
        )
    }

    override suspend fun addBookmark(userId: String, bookId: String, chapterNumber: Int) {
        firestoreService.addBookmark(userId, bookId, chapterNumber)
    }

    override suspend fun removeBookmark(userId: String, bookId: String, chapterNumber: Int) {
        firestoreService.removeBookmark(userId, bookId, chapterNumber)
    }

    override suspend fun isBookmarked(userId: String, bookId: String, chapterNumber: Int): Boolean {
        return firestoreService.isBookmarked(userId, bookId, chapterNumber)
    }

    override suspend fun getSearchHistory(userId: String): List<String> = firestoreService.getSearchHistory(userId)

    override suspend fun saveSearchHistory(userId: String, keyword: String) = firestoreService.addSearchHistory(userId, keyword)

    override suspend fun createBook(book: Book, coverBase64: String?): String {
        var finalCoverUrl = book.cover_url
        if (!coverBase64.isNullOrEmpty()) {
            finalCoverUrl = firestoreService.uploadBookCoverToImgBB(coverBase64)
        }
        
        // Fix: Save category_id as Int if it's a number string
        val categoryIdValue: Any = book.category_id.toIntOrNull() ?: book.category_id

        val bookData = mapOf(
            "title" to book.title,
            "author_id" to book.author_id,
            "author_name" to book.author_name,
            "description" to book.description,
            "category_id" to categoryIdValue,
            "cover_url" to finalCoverUrl,
            "status" to book.status,
            "chapter_count" to 0,
            "view_count" to 0L,
            "rating" to 0.0,
            "created_at" to Timestamp.now()
        )
        return firestoreService.createBookRaw(bookData)
    }

    override suspend fun getBooksByAuthor(authorId: String): List<Book> = coroutineScope {
        val docs = firestoreService.getBooksByAuthorRaw(authorId)
        docs.map { async { mapDocumentToBook(it) } }.awaitAll()
    }

    override suspend fun getVoteCount(bookId: String): Int = firestoreService.getVoteCount(bookId)

    override suspend fun getCommentCount(bookId: String): Int = firestoreService.getCommentCount(bookId)

    override suspend fun uploadBookCover(base64Image: String): String = firestoreService.uploadBookCoverToImgBB(base64Image)
}
