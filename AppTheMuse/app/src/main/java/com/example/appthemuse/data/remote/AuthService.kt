package com.example.appthemuse.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.GoogleAuthProvider

class AuthService {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // 1. Đăng nhập
    suspend fun loginWithEmail(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user
    }

    // 2. Đăng ký và tạo User trên Firestore
    suspend fun registerWithEmail(email: String, password: String, username: String): FirebaseUser? {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user

        if (user != null) {
            val userData = hashMapOf(
                "id" to user.uid,
                "username" to username,
                "email" to email,
                "role" to "user",
                "is_blocked" to false,
                "favorite_genres" to emptyList<String>(),
                "created_at" to com.google.firebase.Timestamp.now()
            )
            try {
                firestore.collection("người dùng").document(user.uid).set(userData).await()
            } catch (e: Exception) {
                firestore.collection("users").document(user.uid).set(userData).await()
            }
        }
        return user
    }

    // Hàm đăng nhập bằng Google Token
    suspend fun loginWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val user = result.user

        if (user != null) {
            var userDoc = firestore.collection("người dùng").document(user.uid).get().await()
            if (!userDoc.exists()) {
                userDoc = firestore.collection("users").document(user.uid).get().await()
            }
            if (!userDoc.exists()) {
                val userData = hashMapOf(
                    "id" to user.uid,
                    "username" to (user.displayName ?: "Người dùng Google"),
                    "email" to (user.email ?: ""),
                    "role" to "user",
                    "is_blocked" to false,
                    "favorite_genres" to emptyList<String>(),
                    "created_at" to com.google.firebase.Timestamp.now()
                )
                try {
                    firestore.collection("người dùng").document(user.uid).set(userData).await()
                } catch (e: Exception) {
                    firestore.collection("users").document(user.uid).set(userData).await()
                }
            }
        }
        return user
    }

    // 3. Kiểm tra xem đã chọn thể loại chưa
    suspend fun hasSelectedGenres(userId: String): Boolean {
        var document = firestore.collection("người dùng").document(userId).get().await()
        if (!document.exists()) {
            document = firestore.collection("users").document(userId).get().await()
        }
        val genres = (document.get("favorite_genres") ?: document.get("thể_loại_yêu_thích")) as? List<*>
        return !genres.isNullOrEmpty()
    }

    // 4. Cập nhật thể loại yêu thích
    suspend fun updateFavoriteGenres(userId: String, genres: List<String>) {
        try {
            firestore.collection("người dùng").document(userId)
                .update("favorite_genres", genres)
                .await()
        } catch (e: Exception) {
            firestore.collection("users").document(userId)
                .update("favorite_genres", genres)
                .await()
        }
    }
}