package com.example.appthemuse.data.remote

import android.util.Log
import com.example.appthemuse.data.remote.dto.BookDto
import com.example.appthemuse.data.remote.dto.CategoryDto
import com.example.appthemuse.data.remote.dto.ChapterDto
import com.example.appthemuse.data.remote.dto.ReviewDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getCategoriesList(): List<CategoryDto> {
        return try {
            val snapshot = firestore.collection("categories").get().await()
            snapshot.documents.map { document ->
                CategoryDto(
                    id = document.id,
                    name = document.getString("name") ?: "Chưa đặt tên",
                    slug = document.getString("slug") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getCategoriesList: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getTrendingBooks(limit: Long = 5): List<BookDto> {
        return try {
            val snapshot = firestore.collection("books")
                .orderBy("view_count", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            snapshot.documents.map { it.toObject(BookDto::class.java)?.copy(id = it.id) ?: BookDto() }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getTrendingBooks: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getRecentBooks(limit: Long = 5): List<BookDto> {
        return try {
            val snapshot = firestore.collection("books")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            snapshot.documents.map { it.toObject(BookDto::class.java)?.copy(id = it.id) ?: BookDto() }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getRecentBooks: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getRecommendedBooks(favoriteGenres: List<String>, limit: Long = 5): List<BookDto> {
        return try {
            if (favoriteGenres.isEmpty()) {
                return getTrendingBooks(limit)
            }
            val snapshot = firestore.collection("books")
                .whereIn("slug", favoriteGenres) // So khớp danh mục
                .limit(limit).get().await()
            snapshot.documents.map { it.toObject(BookDto::class.java)?.copy(id = it.id) ?: BookDto() }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getRecommendedBooks: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getNewReleaseBooks(limit: Long = 5): List<BookDto> {
        return try {
            val snapshot = firestore.collection("books")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            snapshot.documents.map { it.toObject(BookDto::class.java)?.copy(id = it.id) ?: BookDto() }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getNewReleaseBooks: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getAllBooks(limit: Long = 50): List<BookDto> {
        return try {
            val snapshot = firestore.collection("books").limit(limit).get().await()
            snapshot.documents.map { it.toObject(BookDto::class.java)?.copy(id = it.id) ?: BookDto() }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getAllBooks: ${e.message}", e)
            emptyList()
        }
    }

    // 🌟 TRUY VẤN SUB-COLLECTION CHAPTERS (Nằm sâu trong lòng một cuốn sách cụ thể)
    suspend fun getChaptersOfBook(bookDocumentId: String): List<ChapterDto> {
        return try {
            val snapshot = firestore.collection("books").document(bookDocumentId)
                .collection("chapters")
                .orderBy("chapter_number", Query.Direction.ASCENDING)
                .get().await()
            snapshot.documents.map { it.toObject(ChapterDto::class.java)?.copy(id = it.id) ?: ChapterDto() }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getChaptersOfBook: ${e.message}", e)
            emptyList()
        }
    }

    // 🌟 TRUY VẤN SUB-COLLECTION REVIEWS (Chỉ kéo bình luận của chính cuốn sách đang xem)
    suspend fun getReviewsOfBook(bookDocumentId: String): List<ReviewDto> {
        return try {
            val snapshot = firestore.collection("books").document(bookDocumentId)
                .collection("reviews")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.map { it.toObject(ReviewDto::class.java)?.copy(id = it.id) ?: ReviewDto() }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getReviewsOfBook: ${e.message}", e)
            emptyList()
        }
    }
}