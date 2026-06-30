package com.example.appthemuse.data.remote

import com.example.appthemuse.domain.model.AdminBook
import com.example.appthemuse.domain.model.AdminBookDetail
import com.example.appthemuse.domain.model.AdminBookStats
import com.example.appthemuse.domain.model.AdminChapter
import com.example.appthemuse.domain.model.AdminReview
import com.example.appthemuse.domain.model.BookStatus
import com.example.appthemuse.utils.constants.FirebaseConstants
import com.example.appthemuse.utils.constants.FirebaseConstants.BookFields
import com.example.appthemuse.utils.constants.FirebaseConstants.ChapterFields
import com.example.appthemuse.utils.constants.FirebaseConstants.ReviewFields
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.Date

class AdminBookRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getBooks(status: BookStatus? = null): List<AdminBook> = coroutineScope {
        var query: Query = firestore.collection(FirebaseConstants.BOOKS)
            .orderBy(BookFields.CREATED_AT, Query.Direction.DESCENDING)

        if (status != null) {
            query = query.whereEqualTo(BookFields.STATUS, status.value)
        }

        val documents = query.get().await().documents
        documents.map { document ->
            async { document.toAdminBook() }
        }.awaitAll()
    }

    suspend fun getBookStats(): AdminBookStats {
        val books = getBooks()
        return AdminBookStats(
            totalBooks = books.size,
            pendingBooks = books.count { it.status == BookStatus.Pending },
            ongoingBooks = books.count { it.status == BookStatus.Ongoing },
            completedBooks = books.count { it.status == BookStatus.Completed },
            hiddenBooks = books.count { it.status == BookStatus.Hidden }
        )
    }

    suspend fun updateBookStatus(bookId: String, status: BookStatus) {
        firestore.collection(FirebaseConstants.BOOKS)
            .document(bookId)
            .update(BookFields.STATUS, status.value)
            .await()
    }

    suspend fun getBookDetail(bookId: String): AdminBookDetail {
        val bookDocument = firestore.collection(FirebaseConstants.BOOKS)
            .document(bookId)
            .get()
            .await()

        val bookNumId = bookId.removePrefix("book").toIntOrNull() ?: 0
        val chapters = firestore.collection(FirebaseConstants.CHAPTERS)
            .whereEqualTo("book_id", bookNumId)
            .get()
            .await()
            .documents
            .map { it.toAdminChapter(bookId) }
            .sortedBy { it.chapterNumber }

        return AdminBookDetail(
            book = bookDocument.toAdminBook(),
            chapters = chapters
        )
    }

    suspend fun getReviews(bookId: String, includeHidden: Boolean): List<AdminReview> = coroutineScope {
        val numericPart = bookId.replace(Regex("[^0-9]"), "").toIntOrNull()
        val possibleIds = listOfNotNull(
            bookId,
            bookId.toLongOrNull(),
            bookId.toIntOrNull(),
            numericPart,
            numericPart?.toLong()
        ).distinct()

        val query = firestore.collection(FirebaseConstants.REVIEWS)
            .whereIn(ReviewFields.BOOK_ID, possibleIds)

        val documents = query.get().await().documents

        documents.map { doc ->
            async {
                val userId = doc.getString(ReviewFields.USER_ID).orEmpty()
                val userName = try {
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    userDoc.getString("username") ?: userDoc.getString("name") ?: "Người dùng"
                } catch (e: Exception) {
                    "Người dùng"
                }
                doc.toAdminReview(bookId, userName)
            }
        }.awaitAll()
            .filter { includeHidden || !it.isHidden }
            .sortedByDescending { it.createdAt }
    }

    suspend fun setReviewHidden(bookId: String, reviewId: String, hidden: Boolean) {
        firestore.collection(FirebaseConstants.REVIEWS)
            .document(reviewId)
            .update(ReviewFields.IS_HIDDEN, hidden)
            .await()
    }

    private suspend fun DocumentSnapshot.toAdminBook(): AdminBook = coroutineScope {
        val bookId = id
        val reviewsDeferred = async { getReviews(bookId, includeHidden = true) }
        val chaptersDeferred = async {
            val bookNumId = bookId.removePrefix("book").toIntOrNull() ?: 0
            firestore.collection(FirebaseConstants.CHAPTERS)
                .whereEqualTo("book_id", bookNumId)
                .get()
                .await()
                .size()
        }

        val reviews = reviewsDeferred.await()
        val chapterCount = chaptersDeferred.await()
        val visibleReviews = reviews.filter { !it.isHidden }

        AdminBook(
            id = bookId,
            title = getString(BookFields.TITLE).orEmpty(),
            authorId = getString(BookFields.AUTHOR_ID).orEmpty(),
            categoryId = get(BookFields.CATEGORY_ID)?.toString().orEmpty(),
            coverUrl = getString(BookFields.COVER_URL).orEmpty(),
            description = getString(BookFields.DESCRIPTION).orEmpty(),
            status = BookStatus.fromValue(getString(BookFields.STATUS)),
            isPremium = getBoolean(BookFields.IS_PREMIUM) ?: false,
            viewCount = getLong(BookFields.VIEW_COUNT)?.toInt() ?: 0,
            chapterCount = chapterCount,
            reviewCount = visibleReviews.size,
            averageRating = visibleReviews.map { it.rating }.average().takeIf { !it.isNaN() } ?: 0.0,
            createdAt = getDateCompat(BookFields.CREATED_AT)
        )
    }

    private fun DocumentSnapshot.toAdminChapter(bookId: String): AdminChapter {
        return AdminChapter(
            id = id,
            bookId = bookId,
            chapterNumber = getLong(ChapterFields.CHAPTER_NUMBER)?.toInt() ?: 0,
            title = getString(ChapterFields.TITLE).orEmpty(),
            isDraft = getBoolean(ChapterFields.IS_DRAFT) ?: false,
            createdAt = getDateCompat(ChapterFields.CREATED_AT)
        )
    }

    private fun DocumentSnapshot.toAdminReview(bookId: String, userName: String = ""): AdminReview {
        return AdminReview(
            id = id,
            bookId = bookId,
            userId = getString(ReviewFields.USER_ID).orEmpty(),
            userName = userName,
            rating = getLong(ReviewFields.RATING)?.toInt() ?: 0,
            comment = getString(ReviewFields.COMMENT).orEmpty(),
            isHidden = getBoolean(ReviewFields.IS_HIDDEN) ?: false,
            createdAt = getDateCompat(ReviewFields.CREATED_AT)
        )
    }

    private fun DocumentSnapshot.getDateCompat(field: String): Date? {
        return when (val value = get(field)) {
            is Timestamp -> value.toDate()
            is Date -> value
            else -> null
        }
    }
}
