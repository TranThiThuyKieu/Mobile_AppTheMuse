package com.example.appthemuse.ui.mapper

import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.model.Chapter
import com.example.appthemuse.domain.model.User
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.model.CategoryUi
import com.example.appthemuse.ui.model.ChapterUi
import com.example.appthemuse.ui.model.UserUi
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Locale

fun Book.toBookUi(): BookUi {
    return BookUi(
        id = this.id,
        title = this.title,
        cover_url = this.cover_url,
        author_name = this.author_name,
        chapter_count = this.chapter_count,
        rating = this.rating,
        view_count = this.view_count,
        status = this.status,
        category_id = this.category_id,
        description = this.description,
        progressPercent = this.progressPercent,
        lastReadAt = this.lastReadAt
    )
}

fun Chapter.toChapterUi(): ChapterUi {
    return ChapterUi(
        id = this.id,
        book_id = this.book_id,
        title = this.title,
        content = this.content,
        chapter_number = this.chapter_number,
        view_count = this.view_count,
        created_at = this.created_at,
        status = this.status
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

// Hàm dùng chung cho toàn bộ ứng dụng
fun formatViewCount(count: Long): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1000 -> String.format(Locale.US, "%.1fK", count / 1000.0)
        else -> count.toString()
    }
}

fun mapDocumentToUser(
    userId: String,
    doc: DocumentSnapshot,
    backupEmail: String
): User {
    val genresRaw = (doc.get("favorite_genres")) as? List<*>
    return User(
        id = userId,
        username = doc.getString("username") ?: "Người dùng",
        email = doc.getString("email") ?: backupEmail,
        role = doc.getString("role") ?: "user",
        isBlocked = doc.getBoolean("is_blocked") ?: false,
        favoriteGenres = genresRaw?.map { it.toString() } ?: emptyList()
    )
}
