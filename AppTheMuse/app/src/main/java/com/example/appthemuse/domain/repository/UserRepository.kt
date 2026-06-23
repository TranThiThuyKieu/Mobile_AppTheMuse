package com.example.appthemuse.domain.repository

import com.example.appthemuse.domain.model.User

interface UserRepository {
    suspend fun getUserProfile(userId: String): Result<User>
}
