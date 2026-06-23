package com.example.appthemuse.ui.mapper

import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.model.CategoryUi

fun Book.toBookUi(): BookUi {
    return BookUi(
        id = this.id,
        title = this.title,
        cover_url = this.cover_url,
        author_name = this.author_name,
        chapter_count = this.chapter_count,
        rating = this.rating,
        view_count = this.view_count,
        status = this.status
    )
}

fun Category.toCategoryUi(): CategoryUi {
    return CategoryUi(
        id = this.id.toIntOrNull() ?: this.id.hashCode(),
        name = this.name,
        totalBooks = this.totalBooks
    )
}
