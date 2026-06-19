package com.example.appthemuse.data.remote.dto

import com.example.appthemuse.domain.model.ChapterModel
import java.util.Date

data class ChapterDto(
    val id: String = "",
    val book_id: Int = 0,
    val chapter_number: Int = 0,
    val title: String = "",
    val content: String? = null,
    val audio_url: String? = null,
    val is_draft: Boolean = false,
    val created_at: Date = Date()
) {
    fun toDomain(): ChapterModel {
        val numericId = id.hashCode() and 0x7FFFFFFF
        return ChapterModel(
            id = numericId,
            bookId = book_id,
            chapterNumber = chapter_number,
            title = title,
            content = content,
            audioUrl = audio_url,
            isDraft = is_draft,
            createdAt = created_at
        )
    }
}