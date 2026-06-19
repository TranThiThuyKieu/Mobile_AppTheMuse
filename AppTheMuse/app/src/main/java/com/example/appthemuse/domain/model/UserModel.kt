package com.example.appthemuse.domain.model

import java.util.Date

data class UserModel(
    val id: String = "",          // UID từ Firebase Auth
    val username: String = "",
    val email: String = "",
    val role: String = "user",
    val isBlocked: Boolean = false,
    val favoriteGenres: List<String> = emptyList(),
    val createdAt: Date = Date()
)