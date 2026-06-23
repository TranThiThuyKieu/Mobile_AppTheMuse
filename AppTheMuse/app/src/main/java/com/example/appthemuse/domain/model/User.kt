package com.example.appthemuse.domain.model

data class User(
    val id: String = "",        //Thêm = ""
    val username: String = "",  //Thêm = ""
    val email: String = "",     //Thêm = ""
    val role: String = "user",
    val isBlocked: Boolean = false,
    val favoriteGenres: List<String> = emptyList()
)