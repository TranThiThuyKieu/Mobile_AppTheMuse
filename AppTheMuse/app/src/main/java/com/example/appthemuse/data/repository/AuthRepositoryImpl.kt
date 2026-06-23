package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.AuthService
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.User
import com.example.appthemuse.domain.repository.AuthRepository
import com.google.firebase.Timestamp

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val firestoreService: FirestoreService // Inject thêm FirestoreService vào đây
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authService.loginWithEmail(email, password)
            if (firebaseUser != null) {
                // Nhờ FirestoreService lấy doc thô về và ép sang Domain Model tại đây
                val doc = firestoreService.getUserDocument(firebaseUser.uid)
                Result.success(mapDocumentToUser(firebaseUser.uid, doc, firebaseUser.email ?: email))
            } else {
                Result.failure(Exception("Đăng nhập thất bại."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, username: String): Result<User> {
        return try {
            val firebaseUser = authService.registerWithEmail(email, password)
            if (firebaseUser != null) {
                val userData = mapOf(
                    "id" to firebaseUser.uid,
                    "username" to username,
                    "email" to email,
                    "role" to "user",
                    "is_blocked" to false,
                    "favorite_genres" to emptyList<String>(),
                    "created_at" to Timestamp.now()
                )
                firestoreService.saveUserDocument(firebaseUser.uid, userData)
                Result.success(User(id = firebaseUser.uid, username = username, email = email))
            } else {
                Result.failure(Exception("Đăng ký thất bại."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkUserGenresSelected(userId: String): Result<Boolean> {
        return try {
            val doc = firestoreService.getUserDocument(userId)
            val genresRaw = (doc.get("favorite_genres") ?: doc.get("thể_loại_yêu_thích")) as? List<*>
            Result.success(!genresRaw.isNullOrEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveFavoriteGenres(userId: String, genres: List<String>): Result<Unit> {
        return try {
            firestoreService.updateFavoriteGenres(userId, genres)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val firebaseUser = authService.loginWithGoogle(idToken)
            if (firebaseUser != null) {
                var doc = firestoreService.getUserDocument(firebaseUser.uid)
                if (!doc.exists()) {
                    val userData = mapOf(
                        "id" to firebaseUser.uid,
                        "username" to (firebaseUser.displayName ?: "Người dùng Google"),
                        "email" to (firebaseUser.email ?: ""),
                        "role" to "user",
                        "is_blocked" to false,
                        "favorite_genres" to emptyList<String>(),
                        "created_at" to Timestamp.now()
                    )
                    firestoreService.saveUserDocument(firebaseUser.uid, userData)
                    doc = firestoreService.getUserDocument(firebaseUser.uid)
                }
                Result.success(mapDocumentToUser(firebaseUser.uid, doc, firebaseUser.email ?: ""))
            } else {
                Result.failure(Exception("Đăng nhập Google thất bại."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm tiện ích nội bộ để chuyển Document của Firebase thành Model của Domain
    private fun mapDocumentToUser(userId: String, doc: com.google.firebase.firestore.DocumentSnapshot, backupEmail: String): User {
        val genresRaw = (doc.get("favorite_genres") ?: doc.get("thể_loại_yêu_thích")) as? List<*>
        return User(
            id = userId,
            username = doc.getString("username") ?: doc.getString("tên_người_dùng") ?: "Người dùng",
            email = doc.getString("email") ?: backupEmail,
            role = doc.getString("role") ?: "user",
            isBlocked = doc.getBoolean("is_blocked") ?: doc.getBoolean("bị_khóa") ?: false,
            favoriteGenres = genresRaw?.map { it.toString() } ?: emptyList()
        )
    }
}