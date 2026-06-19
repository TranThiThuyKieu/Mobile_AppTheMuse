package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.AuthService
import com.example.appthemuse.domain.model.UserModel
import com.google.firebase.auth.FirebaseUser

class AuthRepository(private val authService: AuthService) {

    suspend fun login(email: String, password: String): Result<UserModel> {
        return try {
            val firebaseUser = authService.loginWithEmail(email, password)
            if (firebaseUser != null) {
                Result.success(
                    UserModel(
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

    suspend fun register(email: String, password: String, username: String): Result<UserModel> {
        return try {
            val firebaseUser = authService.registerWithEmail(email, password, username)
            if (firebaseUser != null) {
                Result.success(UserModel(id = firebaseUser.uid, username = username, email = email))
            } else {
                Result.failure(Exception("Đăng ký thất bại."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkUserGenresSelected(userId: String): Result<Boolean> {
        return try {
            val hasSelected = authService.hasSelectedGenres(userId)
            Result.success(hasSelected)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveFavoriteGenres(userId: String, genres: List<String>): Result<Unit> {
        return try {
            authService.updateFavoriteGenres(userId, genres)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<UserModel> {
        return try {
            val firebaseUser = authService.loginWithGoogle(idToken)
            if (firebaseUser != null) {
                Result.success(
                    UserModel(
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