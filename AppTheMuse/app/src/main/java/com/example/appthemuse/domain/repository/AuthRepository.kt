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
    suspend fun sendEmailVerification()
    suspend fun isEmailVerified(): Boolean
    suspend fun deleteUnverifiedAccount(userId: String)
}