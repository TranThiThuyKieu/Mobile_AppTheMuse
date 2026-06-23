package com.example.appthemuse.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()

    // --- 1. CÁC HÀM VỀ USER (GIỮ NGUYÊN CỦA BẠN) ---
    suspend fun getUserDocument(userId: String): DocumentSnapshot {
        var doc = firestore.collection("users").document(userId).get().await()
        if (!doc.exists()) {
            doc = firestore.collection("người dùng").document(userId).get().await()
        }
        return doc
    }

    suspend fun saveUserDocument(userId: String, userData: Map<String, Any>) {
        try {
            firestore.collection("users").document(userId).set(userData).await()
        } catch (e: Exception) {
            firestore.collection("người dùng").document(userId).set(userData).await()
        }
    }

    suspend fun updateFavoriteGenres(userId: String, genres: List<String>) {
        try {
            firestore.collection("users").document(userId).update("favorite_genres", genres).await()
        } catch (e: Exception) {
            firestore.collection("người dùng").document(userId).update("favorite_genres", genres).await()
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
}