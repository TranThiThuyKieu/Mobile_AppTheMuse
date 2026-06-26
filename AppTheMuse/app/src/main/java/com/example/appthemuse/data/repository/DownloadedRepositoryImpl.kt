package com.example.appthemuse.data.repository

import com.example.appthemuse.data.local.dao.DownloadedBookDao
import com.example.appthemuse.data.local.entity.DownloadedBookEntity
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.repository.DownloadRepository

class DownloadRepositoryImpl(
    private val dao: DownloadedBookDao
) : DownloadRepository {
    // Lấy danh sách sách đã được tải về trong database
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
                category_id = it.categoryId
            )
        }
    }
    // Lưu sách đã tải về vào Database
    override suspend fun saveBook(book: Book) {

        dao.insert(
            DownloadedBookEntity(
                id = book.id,
                title = book.title,
                coverUrl = book.cover_url,
                authorName = book.author_name,
                chapterCount = book.chapter_count,
                rating = book.rating,
                viewCount = book.view_count,
                status = book.status,
                categoryId = book.category_id
            )
        )
    }
}