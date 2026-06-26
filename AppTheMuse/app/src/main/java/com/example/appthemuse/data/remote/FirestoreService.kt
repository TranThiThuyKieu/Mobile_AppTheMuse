package com.example.appthemuse.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()
    private val userCache = mutableMapOf<String, DocumentSnapshot?>()
    private val chapterCache = mutableMapOf<String, Int>()
    private val ratingCache = mutableMapOf<String, Double>()
    // --- 1. CÁC HÀM VỀ USER (GIỮ NGUYÊN CỦA BẠN) ---
    suspend fun getUserDocument(userId: String): DocumentSnapshot {
        var doc = firestore.collection("users").document(userId).get().await()
        return doc
    }

    suspend fun saveUserDocument(userId: String, userData: Map<String, Any>) {
        try {
            firestore.collection("users").document(userId).set(userData).await()
        } catch (e: Exception) {
        }
    }

    suspend fun updateFavoriteGenres(userId: String, genres: List<String>) {
        try {
            firestore.collection("users").document(userId).update("favorite_genres", genres).await()
        } catch (e: Exception) {
        }
    }

    // --- 🎯 2. BỔ SUNG CÁC HÀM LẤY DỮ LIỆU SÁCH VÀ THỂ LOẠI RAW TẠI ĐÂY ---

    // Lấy tất cả sách
    suspend fun getAllBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("books")
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        }
    }

    // Lấy sách mới phát hành (Sắp xếp theo thời gian created_at giảm dần)
    suspend fun getNewReleaseBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("books")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .orderBy("ngày_tạo", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        }
    }

    // Lấy sách đang thịnh hành (Sắp xếp theo lượt xem giảm dần)
    suspend fun getTrendingBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("books")
                .orderBy("view_count", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .orderBy("lượt_xem", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        }
    }

    // Lấy sách đọc gần đây (Tạm thời lấy danh sách tổng quát, bạn có thể custom theo bảng lịch sử sau)
    suspend fun getRecentBooksRaw(limit: Long): List<DocumentSnapshot> {
        return getAllBooksRaw(limit)
    }

    // Gợi ý sách dựa trên danh sách thể loại yêu thích của người dùng
    suspend fun getRecommendedBooksRaw(favoriteGenres: List<String>, limit: Long): List<DocumentSnapshot> {
        return try {
            if (favoriteGenres.isEmpty()) return getAllBooksRaw(limit)

            val snapshot = firestore.collection("books")
                .whereArrayContainsAny("genres", favoriteGenres)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .whereArrayContainsAny("thể_loại", favoriteGenres)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        }
    }

    // Lấy danh sách danh mục / thể loại sách hiện có trên hệ thống
    suspend fun getCategoriesListRaw(): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("categories").get().await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("thể_loại_sách").get().await()
            snapshot.documents
        }
    }
    // Lấy người dùng qua Id người dùng
    suspend fun getUserById(userId: String): DocumentSnapshot? {
        userCache[userId]?.let {
            return it
        }
        val user = firestore.collection("users").document(userId).get().await()
        userCache[userId] = user
        return user
    }
    // Đếm số chương của một quyển sách
    suspend fun getChapterCount(bookId: String): Int {
        chapterCache[bookId]?.let {
            return it
        }
        val count = firestore.collection("chapters").whereEqualTo("book_id", bookId.removePrefix("book").toInt()).get().await().size()
        chapterCache[bookId] = count
        return count
    }
    // Tính Rating
    suspend fun getAverageRating(bookId: String): Double {
        ratingCache[bookId]?.let {
            return it
        }
        val snapshot = firestore.collection("reviews").whereEqualTo("book_id", bookId.removePrefix("book").toInt()).get().await()
        val rating = if (snapshot.isEmpty) {
                0.0
            } else {
                snapshot.documents.map {
                    it.getLong("rating")?.toDouble() ?: 0.0
                }.average()
            }
        ratingCache[bookId] = rating
        return rating
    }
    // Thêm lịch sử tìm kiếm
    suspend fun addSearchHistory(userId: String, keyword: String) {
        if (keyword.isBlank()) return
        firestore.collection("users").document(userId)
            .collection("search_history").document(keyword)
            .set(mapOf("keyword" to keyword,
                    "timestamp" to com.google.firebase.Timestamp.now()))
    }
    // Lấy lịch sử tìm kiếm
    suspend fun getSearchHistory(userId: String): List<String> {
        return firestore.collection("users").document(userId)
            .collection("search_history").orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(7).get()
            .await().documents.mapNotNull { it.getString("keyword") }
    }
    // Lấy tất cả document favorite của user
    suspend fun getFavoriteDocuments(userId: String): List<DocumentSnapshot> {
        return firestore.collection("favorites").whereEqualTo("user_id", userId)
            .get().await().documents
    }

    // Lấy 1 quyển sách theo document id (book1, book2,...)
    suspend fun getBookByDocumentId(bookId: String): DocumentSnapshot? {
        return firestore.collection("books").document(bookId).get().await()
    }
    // lấy lịch sử đọc sách
    suspend fun getHistoryDocuments(userId: String): List<DocumentSnapshot> {
        return firestore.collection("history").whereEqualTo("user_id", userId)
            .get().await().documents
    }
    suspend fun getReadingProgress(
        userId: String,
        bookId: Int
    ): DocumentSnapshot? {

        return firestore
            .collection("reading_progress")
            .whereEqualTo("user_id", userId)
            .whereEqualTo("book_id", bookId)
            .get()
            .await()
            .documents
            .firstOrNull()
    }
}