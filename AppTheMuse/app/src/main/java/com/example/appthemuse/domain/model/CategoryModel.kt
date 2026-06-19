package com.example.appthemuse.domain.model

data class CategoryModel(
    val id: String,     // ID của tài liệu trên Firestore
    val name: String,   // Tên thể loại (Ví dụ: Tiểu thuyết, Trinh thám...)
    val imageUrl: String = "" // Sẵn sàng cho việc hiển thị hình ảnh thể loại sau này
)