package com.example.appthemuse.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.appthemuse.data.model.Book
import com.google.firebase.firestore.Query
import com.example.appthemuse.data.model.BookUi
import com.google.firebase.firestore.DocumentSnapshot
class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()

    // Lấy danh sách tên thể loại từ Collection "categories"
    suspend fun getCategoriesList(): List<String> {
        return try {
            val snapshot = firestore.collection("categories").get().await()
            snapshot.documents.map { document ->
                document.getString("name") ?: document.id
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    // Lấy danh sách truyện có lượt xem cao nhất
    suspend fun getTrendingBooks(limit: Long = 5): List<BookUi> {
        val snapshot = firestore.collection("books").orderBy("view_count", Query.Direction.DESCENDING)
                .limit(limit).get().await()
        return snapshot.documents.map {
            toBookUi(it)
        }
    }
    // Lấy danh sách truyện mới cập nhật gần đây
    suspend fun getRecentBooks(limit: Long = 5): List<BookUi> {
        return try {
            val snapshot = firestore.collection("books").orderBy("created_at", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            snapshot.documents.map {
                toBookUi(it)
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getRecentBooks: ${e.message}", e)
            emptyList()
        }
    }
    // Lấy danh sách truyện đề xuất cho người dùng
    suspend fun getRecommendedBooks(favoriteGenres: List<String>, limit: Long = 5): List<BookUi> {
        return try {
            val snapshot = firestore.collection("books").orderBy("view_count", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            snapshot.documents.map {
                toBookUi(it)
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getRecommendedBooks: ${e.message}", e)
            emptyList()
        }
    }
    // Chuyển đổi dữ liệu Firestore thành đối tượng BookUi đồng thời lấy thêm tên tác giả, số chương và đánh giá trung bình
    private suspend fun toBookUi(doc: DocumentSnapshot): BookUi {
        val book = doc.toObject(Book::class.java)!!
        // author
        val userDoc = firestore.collection("users").document(book.author_id).get().await()
        val authorName = userDoc.getString("username") ?: ""
        // chapter count
        val chapterSnapshot = firestore.collection("chapters").whereEqualTo("book_id", book.id).get().await()
        val chapterCount = chapterSnapshot.size()
        // rating
        val reviewSnapshot = firestore.collection("reviews").whereEqualTo("book_id", book.id).get().await()
        val rating =
            if (reviewSnapshot.isEmpty)
                0.0
            else
                reviewSnapshot.documents.map {
                        (it.getLong("rating") ?: 0).toDouble()
                    }.average()
        return BookUi(
            id = doc.id,
            title = book.title,
            cover_url = book.cover_url,
            author_name = authorName,
            chapter_count = chapterCount,
            rating = rating,
            view_count = book.view_count,
            status = book.status)
    }
    // Danh sách sách mới phát hành
    suspend fun getNewReleaseBooks(limit: Long = 5): List<BookUi> {
        return try {
            val snapshot = firestore.collection("books").orderBy("created_at", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            snapshot.documents.map {
                toBookUi(it)
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getNewReleaseBooks: ${e.message}", e)
            emptyList()
        }
    }
}