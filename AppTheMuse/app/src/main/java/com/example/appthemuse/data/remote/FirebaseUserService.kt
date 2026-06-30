// File: app/src/main/java/com/example/appthemuse/data/remote/FirebaseUserService.kt
package com.example.appthemuse.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseUserService {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    fun getCurrentUserUid(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    suspend fun fetchUserName(uid: String): String {
        return try {
            var userDoc = firestore.collection("users").document(uid).get().await()


            if (userDoc.exists()) {
                userDoc.getString("username")
                    ?: userDoc.getString("tên_người_dùng")
                    ?: "Người dùng"
            } else {
                "Người dùng"
            }
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error fetchUserName for uid $uid: ${e.message}", e)
            "Lỗi tải dữ liệu"
        }
    }
    suspend fun fetchFullUserProfile(uid: String): Map<String, Any>? {
        return try {
            var userDoc = firestore.collection("users").document(uid).get().await()
            userDoc.data
        } catch (e: Exception) {
            Log.e("FirebaseUserService", "Error fetchFullUserProfile: ${e.message}", e)
            null
        }
    }

    // Lưu thông tin thay đổi lên Firestore
    suspend fun updateUserProfile(uid: String, data: Map<String, Any>): Boolean {
        return try {
            firestore.collection("users").document(uid).update(data).await()
            true
        } catch (e: Exception) {
            // Nếu update báo lỗi do document chưa có thì dùng set(merge)
            try {
                firestore.collection("users").document(uid).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
                true
            } catch (ex: Exception) {
                Log.e("FirebaseUserService", "Error updateUserProfile: ${ex.message}", ex)
                false
            }
        }
    }

    suspend fun countFavoriteBooks(userId: String): Int {
        return try {
            firestore.collection("favorites")
                .whereEqualTo("user_id", userId)
                .get().await().size()
        } catch (e: Exception) { 0 }
    }

    suspend fun countReadBooks(userId: String): Int {
        return try {
            // Đếm số sách duy nhất người dùng đã đọc từ bảng history
            firestore.collection("history")
                .whereEqualTo("user_id", userId)
                .get().await()
                .documents
                .mapNotNull { it.getString("book_id") }
                .toSet()
                .size
        } catch (e: Exception) { 0 }
    }
}