package com.example.appthemuse.domain.repository

import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Chapter

interface DownloadRepository {
    suspend fun getDownloadedBooks(): List<Book>
    suspend fun saveBook(book: Book)
    suspend fun getBookById(bookId: String): Book?
    suspend fun deleteBook(bookId: String)
    
    suspend fun saveChapters(bookId: String, chapters: List<Chapter>)
    suspend fun getChapters(bookId: String): List<Chapter>
    suspend fun getChapter(chapterId: String): Chapter?
}
