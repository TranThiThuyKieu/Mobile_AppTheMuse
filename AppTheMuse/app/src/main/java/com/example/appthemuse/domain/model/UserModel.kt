package com.example.appthemuse.domain.model

data class UserModel(
    val id: String,
    val username: String,
    val email: String,
    val role: String = "user",
    val isBlocked: Boolean = false,
    val favoriteGenres: List<String> = emptyList()
)