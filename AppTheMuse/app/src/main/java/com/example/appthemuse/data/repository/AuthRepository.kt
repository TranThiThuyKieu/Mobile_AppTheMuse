package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.AuthService
import com.google.firebase.auth.FirebaseUser

class AuthRepository(private val authService: AuthService) {

    // Đăng nhập
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val user = authService.loginWithEmail(email, password)
            if (user != null) Result.success(user)
            else Result.failure(Exception("Đăng nhập thất bại. Tài khoản không tồn tại."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Đăng ký
    suspend fun register(email: String, password: String, username: String): Result<FirebaseUser> {
        return try {
            val user = authService.registerWithEmail(email, password, username)
            if (user != null) Result.success(user)
            else Result.failure(Exception("Đăng ký thất bại."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kiểm tra thể loại yêu thích
    suspend fun checkUserGenresSelected(userId: String): Result<Boolean> {
        return try {
            val hasSelected = authService.hasSelectedGenres(userId)
            Result.success(hasSelected)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cập nhật thể loại yêu thích
    suspend fun saveFavoriteGenres(userId: String, genres: List<String>): Result<Unit> {
        return try {
            authService.updateFavoriteGenres(userId, genres)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}