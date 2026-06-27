package com.example.appthemuse.data.repository

import com.example.appthemuse.data.local.dao.DownloadedBookDao
import com.example.appthemuse.data.local.entity.DownloadedBookEntity
import com.example.appthemuse.data.local.entity.DownloadedChapterEntity
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Chapter
import com.example.appthemuse.domain.repository.DownloadRepository
import com.google.firebase.Timestamp

class DownloadedRepositoryImpl(
    private val dao: DownloadedBookDao
) : DownloadRepository {
    
    override suspend fun getDownloadedBooks(): List<Book> {
        return dao.getAllBooks().map {
            Book(
                id = it.id,
                title = it.title,
                cover_url = it.coverUrl,
                author_name = it.authorName,
                chapter_count = it.chapterCount,
                rating = it.rating,
                view_count = it.viewCount,
                status = it.status,
                category_id = it.categoryId,
                description = it.description
            )
        }
    }

    override suspend fun saveBook(book: Book) {
        dao.insertBook(
            DownloadedBookEntity(
                id = book.id,
                title = book.title,
                coverUrl = book.cover_url,
                authorName = book.author_name,
                chapterCount = book.chapter_count,
                rating = book.rating,
                viewCount = book.view_count,
                status = book.status,
                categoryId = book.category_id,
                description = book.description
            )
        )
    }

    override suspend fun getBookById(bookId: String): Book? {
        return dao.getBookById(bookId)?.let {
            Book(
                id = it.id,
                title = it.title,
                cover_url = it.coverUrl,
                author_name = it.authorName,
                chapter_count = it.chapterCount,
                rating = it.rating,
                view_count = it.viewCount,
                status = it.status,
                category_id = it.categoryId,
                description = it.description
            )
        }
    }

    override suspend fun deleteBook(bookId: String) {
        val entity = dao.getBookById(bookId)
        if (entity != null) {
            dao.deleteBook(entity)
        }
    }

    override suspend fun saveChapters(bookId: String, chapters: List<Chapter>) {
        val entities = chapters.map {
            DownloadedChapterEntity(
                id = "${bookId}_chapter${it.chapter_number}",
                bookId = bookId,
                title = it.title,
                content = it.content,
                chapterNumber = it.chapter_number,
                viewCount = it.view_count,
                createdAt = it.created_at?.seconds?.times(1000)
            )
        }
        dao.insertChapters(entities)
    }

    override suspend fun getChapters(bookId: String): List<Chapter> {
        return dao.getChaptersByBookId(bookId).map {
            Chapter(
                id = it.id,
                book_id = it.bookId.removePrefix("book").toIntOrNull() ?: 0,
                title = it.title,
                content = it.content,
                chapter_number = it.chapterNumber,
                view_count = it.viewCount,
                created_at = it.createdAt?.let { millis -> Timestamp(millis / 1000, 0) }
            )
        }
    }

    override suspend fun getChapter(chapterId: String): Chapter? {
        return dao.getChapterById(chapterId)?.let {
            Chapter(
                id = it.id,
                book_id = it.bookId.removePrefix("book").toIntOrNull() ?: 0,
                title = it.title,
                content = it.content,
                chapter_number = it.chapterNumber,
                view_count = it.viewCount,
                created_at = it.createdAt?.let { millis -> Timestamp(millis / 1000, 0) }
            )
        }
    }
}
