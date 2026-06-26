package com.example.appthemuse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_books")
data class DownloadedBookEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val coverUrl: String,
    val authorName: String,
    val chapterCount: Int,
    val rating: Double,
    val viewCount: Long,
    val status: String,
    val categoryId: String
)