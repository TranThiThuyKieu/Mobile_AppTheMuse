package com.example.appthemuse.ui.model

data class UserUi(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val isBlocked: Boolean,
    val favoriteGenres: List<String>
)
