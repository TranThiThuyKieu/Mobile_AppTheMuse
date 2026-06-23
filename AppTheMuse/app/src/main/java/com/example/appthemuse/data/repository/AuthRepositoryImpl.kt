package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.AuthService
import com.example.appthemuse.domain.model.User
import com.example.appthemuse.domain.repository.AuthRepository

// 👉 SỬA LỖI 1: Thêm ": AuthRepository" để kế thừa từ Interface của tầng Domain
class AuthRepositoryImpl(
    private val authService: AuthService
) : AuthRepository {

    // 👉 SỬA LỖI 3: Thêm từ khóa "override" trước tất cả các hàm nghiệp vụ
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authService.loginWithEmail(email, password)
            if (firebaseUser != null) {
                Result.success(
                    User(
                        id = firebaseUser.uid,
                        username = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: ""
                    )
                )
            } else {
                Result.failure(Exception("Đăng nhập thất bại. Tài khoản không tồn tại."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, username: String): Result<User> {
        return try {
            val firebaseUser = authService.registerWithEmail(email, password, username)
            if (firebaseUser != null) {
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
            val hasSelected = authService.hasSelectedGenres(userId)
            Result.success(hasSelected)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveFavoriteGenres(userId: String, genres: List<String>): Result<Unit> {
        return try {
            // 👉 SỬA LỖI 2: Xóa bỏ dòng gọi nhầm "authRepository.updateFavoriteGenres(...)" gây crash/lỗi compile
            authService.updateFavoriteGenres(userId, genres)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val firebaseUser = authService.loginWithGoogle(idToken)
            if (firebaseUser != null) {
                Result.success(
                    User(
                        id = firebaseUser.uid,
                        username = firebaseUser.displayName ?: "Người dùng Google",
                        email = firebaseUser.email ?: ""
                    )
                )
            } else {
                Result.failure(Exception("Đăng nhập Google thất bại."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}