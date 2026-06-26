package com.example.appthemuse.domain.repository

import com.example.appthemuse.domain.model.Book
interface DownloadRepository {
    // Lấy danh sách sách đã tải xuống
    suspend fun getDownloadedBooks():
            List<Book>
    // Lưu một cuốn sách xuống bộ nhớ cục bộ
    suspend fun saveBook(book: Book)
}