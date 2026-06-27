package com.example.appthemuse.data.local.dao

import androidx.room.*
import com.example.appthemuse.data.local.entity.DownloadedBookEntity
import com.example.appthemuse.data.local.entity.DownloadedChapterEntity

@Dao
interface DownloadedBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: DownloadedBookEntity): Long

    @Query("SELECT * FROM downloaded_books")
    suspend fun getAllBooks(): List<DownloadedBookEntity>

    @Query("SELECT * FROM downloaded_books WHERE id = :bookId")
    suspend fun getBookById(bookId: String): DownloadedBookEntity?

    @Delete
    suspend fun deleteBook(book: DownloadedBookEntity)

    // Chapters
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<DownloadedChapterEntity>)

    @Query("SELECT * FROM downloaded_chapters WHERE bookId = :bookId ORDER BY chapterNumber ASC")
    suspend fun getChaptersByBookId(bookId: String): List<DownloadedChapterEntity>

    @Query("SELECT * FROM downloaded_chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: String): DownloadedChapterEntity?
}
