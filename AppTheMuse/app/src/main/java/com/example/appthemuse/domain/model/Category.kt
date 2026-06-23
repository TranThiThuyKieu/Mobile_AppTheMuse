package com.example.appthemuse.domain.model

data class Category(
    val id: String = "",       //Thêm = "" để Firebase tự map toObject được
    val name: String = "",     //Thêm = "" cho an toàn
    val imageUrl: String = "",
    val totalBooks: Int = 0
)