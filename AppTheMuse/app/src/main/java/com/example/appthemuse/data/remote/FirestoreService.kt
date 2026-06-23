package com.example.appthemuse.data.remote

import android.util.Log
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getCategoriesList(): List<Category> {
        return try {
            var snapshot = firestore.collection("Thể loại").get().await()
            if (snapshot.isEmpty) {
                snapshot = firestore.collection("categories").get().await()
            }
            snapshot.documents.map { document ->
                Category(
                    id = document.id,
                    name = document.getString("tên") ?: document.getString("name") ?: "Chưa đặt tên",
                    imageUrl = document.getString("ảnh_bìa") ?: document.getString("imageUrl") ?: "",
                    totalBooks = ((document.get("tổng_số_sách") ?: document.get("totalBooks") ?: 0L) as? Long)?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getCategoriesList: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getTrendingBooks(limit: Long = 5): List<Book> {
        return try {
            var snapshot = firestore.collection("sách").orderBy("số lượt xem", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            if (snapshot.isEmpty) {
                snapshot = firestore.collection("books").orderBy("view_count", Query.Direction.DESCENDING)
                    .limit(limit).get().await()
            }
            snapshot.documents.map { toBook(it) }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getTrendingBooks: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getRecentBooks(limit: Long = 5): List<Book> {
        return try {
            var snapshot = firestore.collection("sách").orderBy("created_at", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            if (snapshot.isEmpty) {
                snapshot = firestore.collection("books").orderBy("created_at", Query.Direction.DESCENDING)
                    .limit(limit).get().await()
            }
            snapshot.documents.map { toBook(it) }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getRecentBooks: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getRecommendedBooks(favoriteGenres: List<String>, limit: Long = 5): List<Book> {
        return try {
            var snapshot = firestore.collection("sách").orderBy("số lượt xem", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            if (snapshot.isEmpty) {
                snapshot = firestore.collection("books").orderBy("view_count", Query.Direction.DESCENDING)
                    .limit(limit).get().await()
            }
            snapshot.documents.map { toBook(it) }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getRecommendedBooks: ${e.message}", e)
            emptyList()
        }
    }

    private suspend fun toBook(doc: DocumentSnapshot): Book {
        val title = doc.getString("tiêu đề") ?: doc.getString("title") ?: ""
        val slug = doc.getString("sên") ?: doc.getString("slug") ?: ""
        val authorId = doc.getString("author_id") ?: doc.getString("tác_giả_id") ?: ""
        val categoryId = (doc.get("category_id") ?: doc.get("thể_loại_id"))?.toString() ?: ""
        val coverUrl = doc.getString("URL bìa") ?: doc.getString("cover_url") ?: ""
        val description = doc.getString("Sự miêu tả") ?: doc.getString("description") ?: ""
        val isPremium = doc.getBoolean("là cao cấp") ?: doc.getBoolean("is_premium") ?: false
        val viewCount = doc.getLong("số lượt xem") ?: doc.getLong("view_count") ?: 0L
        val status = doc.getString("trạng thái") ?: doc.getString("status") ?: ""
        val createdAt = doc.getTimestamp("created_at")

        // Fetch author name from "người dùng" / "users"
        var authorName = ""
        if (authorId.isNotEmpty()) {
            var userDoc = firestore.collection("người dùng").document(authorId).get().await()
            if (!userDoc.exists()) {
                userDoc = firestore.collection("users").document(authorId).get().await()
            }
            authorName = userDoc.getString("username") ?: userDoc.getString("tên_người_dùng") ?: ""
        }

        // Fetch chapter count from "chương" / "chapters"
        var chapterCount = 0
        var chapterSnapshot = firestore.collection("chương").whereEqualTo("book_id", doc.id).get().await()
        if (chapterSnapshot.isEmpty) {
            chapterSnapshot = firestore.collection("chapters").whereEqualTo("book_id", doc.id).get().await()
        }
        chapterCount = chapterSnapshot.size()

        // Fetch rating from "đánh giá" / "reviews"
        var reviewSnapshot = firestore.collection("đánh giá").whereEqualTo("book_id", doc.id).get().await()
        if (reviewSnapshot.isEmpty) {
            reviewSnapshot = firestore.collection("reviews").whereEqualTo("book_id", doc.id).get().await()
        }
        val rating = if (reviewSnapshot.isEmpty) 0.0 else reviewSnapshot.documents.map { 
            (it.getLong("rating") ?: it.getLong("đánh_giá") ?: 0L).toDouble() 
        }.average()

        return Book(
            id = doc.id,
            title = title,
            slug = slug,
            author_id = authorId,
            author_name = authorName,
            chapter_count = chapterCount,
            rating = rating,
            category_id = categoryId,
            cover_url = coverUrl,
            description = description,
            is_premium = isPremium,
            view_count = viewCount,
            status = status,
            created_at = createdAt
        )
    }

    suspend fun getNewReleaseBooks(limit: Long = 5): List<Book> {
        return try {
            var snapshot = firestore.collection("sách").orderBy("created_at", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            if (snapshot.isEmpty) {
                snapshot = firestore.collection("books").orderBy("created_at", Query.Direction.DESCENDING)
                    .limit(limit).get().await()
            }
            snapshot.documents.map { toBook(it) }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getNewReleaseBooks: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getAllBooks(limit: Long = 50): List<Book> {
        return try {
            var snapshot = firestore.collection("sách").limit(limit).get().await()
            if (snapshot.isEmpty) {
                snapshot = firestore.collection("books").limit(limit).get().await()
            }
            snapshot.documents.map { toBook(it) }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getAllBooks: ${e.message}", e)
            emptyList()
        }
    }
}