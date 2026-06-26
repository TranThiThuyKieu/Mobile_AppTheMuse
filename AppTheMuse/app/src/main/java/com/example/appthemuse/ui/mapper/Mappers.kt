package com.example.appthemuse.ui.mapper

import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.model.User
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.model.CategoryUi
import com.example.appthemuse.ui.model.UserUi

fun Book.toBookUi(): BookUi {
    return BookUi(
        id = this.id,
        title = this.title,
        cover_url = this.cover_url,
        author_name = this.author_name,
        chapter_count = this.chapter_count,
        rating = this.rating,
        view_count = this.view_count,
        status = this.status ,
        category_id = this.category_id,
        progressPercent = progressPercent,
        lastReadAt = lastReadAt
    )
}

fun Category.toCategoryUi(): CategoryUi {
    return CategoryUi(
        id = this.id,
        name = this.name,
        totalBooks = this.totalBooks
    )
}

fun User.toUserUi(): UserUi {
    return UserUi(
        id = this.id,
        username = this.username,
        email = this.email,
        role = this.role,
        isBlocked = this.isBlocked,
        favoriteGenres = this.favoriteGenres
    )
}
