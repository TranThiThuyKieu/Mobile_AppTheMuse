package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.User
import com.example.appthemuse.domain.repository.UserRepository

class UserRepositoryImpl(
    private val firestoreService: FirestoreService // 👉 Đã sửa: Dùng FirestoreService thay vì AuthService
) : UserRepository {

    override suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val doc = firestoreService.getUserDocument(userId)
            if (doc.exists()) {
                val genresRaw = (doc.get("favorite_genres") ?: doc.get("thể_loại_yêu_thích")) as? List<*>
                val user = User(
                    id = userId,
                    username = doc.getString("username") ?: doc.getString("tên_người_dùng") ?: "Người dùng",
                    email = doc.getString("email") ?: "",
                    role = doc.getString("role") ?: "user",
                    isBlocked = doc.getBoolean("is_blocked") ?: doc.getBoolean("bị_khóa") ?: false,
                    favoriteGenres = genresRaw?.map { it.toString() } ?: emptyList()
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Không tìm thấy thông tin người dùng"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}