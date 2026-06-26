package com.example.appthemuse.domain.repository

import com.example.appthemuse.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, username: String): Result<User>
    suspend fun loginWithGoogle(idToken: String): Result<User>

    suspend fun checkUserGenresSelected(userId: String): Result<Boolean>
    suspend fun saveFavoriteGenres(userId: String, genres: List<String>): Result<Unit>

    suspend fun getCurrentUserId(): String?

    // ✅ VERIFY FLOW
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun isEmailVerified(): Boolean
    suspend fun deleteUnverifiedAccount(userId: String)
    suspend fun updateUserBlockStatus(userId: String, isBlocked: Boolean): Result<Unit>

    // ✅ NEW SECURITY FEATURES
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun checkPasswordResetLimit(email: String): Result<Boolean>
    suspend fun checkResendVerificationLimit(email: String): Result<Boolean>
    suspend fun isAccountLocked(email: String): Result<Boolean>
}