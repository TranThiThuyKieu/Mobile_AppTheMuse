package com.example.appthemuse.data.remote.dto

import com.example.appthemuse.domain.model.CategoryModel

data class CategoryDto(
    val id: String = "",
    val name: String = "",
    val slug: String = ""
) {
    fun toDomain(): CategoryModel {
        // Chuyển String ID của Firestore thành Int an toàn cho Domain Model
        val numericId = id.hashCode() and 0x7FFFFFFF
        return CategoryModel(
            id = numericId,
            name = name,
            slug = slug
        )
    }
}