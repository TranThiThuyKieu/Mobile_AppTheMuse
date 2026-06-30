package com.example.appthemuse.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "downloaded_chapters",
    foreignKeys = [
        ForeignKey(
            entity = DownloadedBookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
data class DownloadedChapterEntity(
    @PrimaryKey
    val id: String,
    val bookId: String,
    val title: String,
    val content: String,
    val chapterNumber: Int,
    val viewCount: Long,
    val createdAt: Long? = null
)
