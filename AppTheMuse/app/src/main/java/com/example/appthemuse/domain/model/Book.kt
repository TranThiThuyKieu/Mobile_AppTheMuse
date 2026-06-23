package com.example.appthemuse.domain.model

import com.google.firebase.Timestamp

data class Book(
    val id: String = "",            // Vẫn phải đổi sang String vì Firestore không dùng số Long
    val title: String = "",
    val slug: String = "",
    val author_id: String = "",
    val author_name: String = "",
    val chapter_count: Int = 0,
    val rating: Double = 0.0,
    val category_id: String = "",   // Đổi sang String để ăn khớp với bảng Thể loại trên Firestore
    val cover_url: String = "",
    val description: String = "",
    val is_premium: Boolean = false,
    val view_count: Long = 0L,
    val status: String = "",
    val created_at: Timestamp? = null
)