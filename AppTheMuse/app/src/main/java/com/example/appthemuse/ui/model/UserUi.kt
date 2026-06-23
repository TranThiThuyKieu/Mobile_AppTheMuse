package com.example.appthemuse.ui.model

data class UserUi(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val birthday: String = "",
    val gender: String = "",
    val readCount: Int = 0,
    val favoriteCount: Int = 0,
    val downloadedCount: Int = 0,
    val isBlocked: Boolean = false,
    val favoriteGenres: List<String> = emptyList()
)