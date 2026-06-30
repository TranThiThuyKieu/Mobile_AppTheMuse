package com.example.appthemuse.ui.mapper

import com.example.appthemuse.domain.model.AdminBook
import com.example.appthemuse.domain.model.AdminBookStats
import com.example.appthemuse.domain.model.AdminChapter
import com.example.appthemuse.domain.model.AdminReview
import com.example.appthemuse.ui.model.AdminBookStatsUi
import com.example.appthemuse.ui.model.AdminBookUi
import com.example.appthemuse.ui.model.AdminChapterUi
import com.example.appthemuse.ui.model.AdminReviewUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))

fun AdminBook.toUi(): AdminBookUi {
    return AdminBookUi(
        id = id,
        title = title.ifBlank { "Chua co ten" },
        authorId = authorId,
        categoryId = categoryId,
        coverUrl = coverUrl,
        description = description,
        statusValue = status.value,
        statusLabel = status.label,
        isPremium = isPremium,
        viewCountText = viewCount.toString(),
        chapterCountText = chapterCount.toString(),
        reviewCountText = reviewCount.toString(),
        ratingText = "%.1f".format(averageRating),
        createdAtText = createdAt.formatDate()
    )
}

fun AdminChapter.toUi(): AdminChapterUi {
    return AdminChapterUi(
        id = id,
        title = title.ifBlank { "Chuong $chapterNumber" },
        chapterNumber = chapterNumber,
        chapterNumberText = "Chuong $chapterNumber",
        stateText = if (isDraft) "Ban nhap" else "Da dang",
        createdAtText = createdAt.formatDate()
    )
}

fun AdminReview.toUi(): AdminReviewUi {
    return AdminReviewUi(
        id = id,
        userId = userId,
        ratingText = "$rating/5",
        comment = comment.ifBlank { "Khong co noi dung" },
        isHidden = isHidden,
        statusText = if (isHidden) "Da an" else "Dang hien thi",
        createdAtText = createdAt.formatDate()
    )
}

fun AdminBookStats.toUi(): AdminBookStatsUi {
    return AdminBookStatsUi(
        total = totalBooks.toString(),
        pending = pendingBooks.toString(),
        ongoing = ongoingBooks.toString(),
        completed = completedBooks.toString(),
        hidden = hiddenBooks.toString()
    )
}

private fun Date?.formatDate(): String {
    return this?.let(dateFormatter::format) ?: "--"
}
