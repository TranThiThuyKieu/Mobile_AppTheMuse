package com.example.appthemuse.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.firestore.AggregateSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()
    private val userCache = mutableMapOf<String, DocumentSnapshot?>()
    private val chapterCache = mutableMapOf<String, Int>()
    private val ratingCache = mutableMapOf<String, Double>()

    suspend fun getUserDocument(userId: String): DocumentSnapshot {
        return firestore.collection("users").document(userId).get().await()
    }

    suspend fun saveUserDocument(userId: String, userData: Map<String, Any>) {
        try {
            firestore.collection("users").document(userId).set(userData).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Error saving user: ${e.message}")
        }
    }

    suspend fun updateFavoriteGenres(userId: String, genres: List<String>) {
        try {
            firestore.collection("users").document(userId).update("favorite_genres", genres).await()
        } catch (e: Exception) {
        }
    }

    suspend fun updateUserBlockStatus(userId: String, isBlocked: Boolean) {
        try {
            firestore.collection("users").document(userId).update("is_blocked", isBlocked).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Error updating block status: ${e.message}")
        }
    }

    // ✅ Security Logs Methods
    suspend fun getSecurityLog(email: String): DocumentSnapshot {
        return firestore.collection("security_logs").document(email).get().await()
    }

    suspend fun updateSecurityLog(email: String, data: Map<String, Any>) {
        firestore.collection("security_logs").document(email).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    suspend fun getAllBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("books")
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        }
    }

    suspend fun getNewReleaseBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("books")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .orderBy("ngày_tạo", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        }
    }

    suspend fun getTrendingBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("books")
                .orderBy("view_count", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .orderBy("lượt_xem", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        }
    }

    suspend fun getRecentBooksRaw(limit: Long): List<DocumentSnapshot> {
        return getAllBooksRaw(limit)
    }

    suspend fun getRecommendedBooksRaw(favoriteGenres: List<String>, limit: Long): List<DocumentSnapshot> {
        return try {
            if (favoriteGenres.isEmpty()) return getAllBooksRaw(limit)

            val snapshot = firestore.collection("books")
                .whereArrayContainsAny("genres", favoriteGenres)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .whereArrayContainsAny("thể_loại", favoriteGenres)
                .limit(limit)
                .get()
                .await()
            snapshot.documents
        }
    }

    suspend fun getCategoriesListRaw(): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("categories").get().await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("thể_loại_sách").get().await()
            snapshot.documents
        }
    }

    suspend fun getUserById(userId: String): DocumentSnapshot? {
        userCache[userId]?.let {
            return it
        }
        val user = firestore.collection("users").document(userId).get().await()
        userCache[userId] = user
        return user
    }

    suspend fun getChapterCount(bookId: String): Int {
        chapterCache[bookId]?.let {
            return it
        }
        val count = firestore.collection("chapters").whereEqualTo("book_id", bookId.removePrefix("book").toInt()).get().await().size()
        chapterCache[bookId] = count
        return count
    }

    suspend fun getAverageRating(bookId: String): Double {
        ratingCache[bookId]?.let {
            return it
        }
        val snapshot = firestore.collection("reviews").whereEqualTo("book_id", bookId.removePrefix("book").toInt()).get().await()
        val rating = if (snapshot.isEmpty) {
                0.0
            } else {
                snapshot.documents.map {
                    it.getLong("rating")?.toDouble() ?: 0.0
                }.average()
            }
        ratingCache[bookId] = rating
        return rating
    }

    suspend fun getVoteCount(bookId: String): Int {
        return try {
            val snapshot = firestore.collection("favorites")
                .whereEqualTo("book_id", bookId.removePrefix("book").toInt())
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getCommentCount(bookId: String): Int {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("book_id", bookId.removePrefix("book").toInt())
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getChaptersRaw(bookId: String): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("chapters")
                .whereEqualTo("book_id", bookId.removePrefix("book").toIntOrNull() ?: return emptyList())
                .get()
                .await()
            snapshot.documents
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "getChaptersRaw error: ${e.message}")
            emptyList()
        }
    }

    suspend fun createChapterRaw(bookId: String, chapterData: Map<String, Any>): String {
        val bookNumId = bookId.removePrefix("book").toIntOrNull() ?: return ""
        val existingCount = firestore.collection("chapters")
            .whereEqualTo("book_id", bookNumId)
            .get().await().size()
        val nextChapterNumber = existingCount + 1
        val documentId = "${bookId}_chapter$nextChapterNumber"
        val dataWithNumber = chapterData.toMutableMap()
        dataWithNumber["chapter_number"] = nextChapterNumber
        dataWithNumber["book_id"] = bookNumId
        dataWithNumber["created_at"] = com.google.firebase.Timestamp.now()
        dataWithNumber["view_count"] = 0L
        dataWithNumber["status"] = "đã đăng"
        firestore.collection("chapters").document(documentId).set(dataWithNumber).await()
        firestore.collection("books").document(bookId)
            .update("chapter_count", nextChapterNumber).await()
        chapterCache.remove(bookId)
        return documentId
    }

    suspend fun addSearchHistory(userId: String, keyword: String) {
        if (keyword.isBlank()) return
        firestore.collection("users").document(userId)
            .collection("search_history").document(keyword)
            .set(mapOf("keyword" to keyword,
                    "timestamp" to com.google.firebase.Timestamp.now()))
    }

    suspend fun getSearchHistory(userId: String): List<String> {
        return firestore.collection("users").document(userId)
            .collection("search_history").orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(7).get()
            .await().documents.mapNotNull { it.getString("keyword") }
    }

    suspend fun getFavoriteDocuments(userId: String): List<DocumentSnapshot> {
        return firestore.collection("favorites").whereEqualTo("user_id", userId)
            .get().await().documents
    }

    suspend fun getBookByDocumentId(bookId: String): DocumentSnapshot? {
        return firestore.collection("books").document(bookId).get().await()
    }

    suspend fun getHistoryDocuments(userId: String): List<DocumentSnapshot> {
        return firestore.collection("history").whereEqualTo("user_id", userId)
            .get().await().documents
    }

    suspend fun getReadingProgress(
        userId: String,
        bookId: Int
    ): DocumentSnapshot? {

        return firestore
            .collection("reading_progress")
            .whereEqualTo("user_id", userId)
            .whereEqualTo("book_id", bookId)
            .get()
            .await()
            .documents
            .firstOrNull()
    }
    // Lấy sách dựa theo author_id (Dành cho Góc tác giả)
    suspend fun getBooksByAuthorRaw(authorId: String): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("books")
                .whereEqualTo("author_id", authorId)
                .get()
                .await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .whereEqualTo("tác_giả_id", authorId)
                .get()
                .await()
            snapshot.documents
        }
    }

    suspend fun uploadBookCoverToImgBB(base64Image: String): String = withContext(Dispatchers.IO) {
        val apiKey = "a91c56ed41e002d0d9caf4919a1ee092"
        val urlEncodedImage = java.net.URLEncoder.encode(base64Image, "UTF-8")
        val url = java.net.URL("https://api.imgbb.com/1/upload")
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        val postData = "key=$apiKey&image=$urlEncodedImage"
        connection.outputStream.write(postData.toByteArray(Charsets.UTF_8))
        val responseCode = connection.responseCode
        if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonObject = org.json.JSONObject(response)
            val dataObject = jsonObject.getJSONObject("data")
            return@withContext dataObject.getString("url")
        } else {
            val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
            throw Exception("Lỗi ImgBB: $responseCode - $errorResponse")
        }
    }

    suspend fun createBookRaw(bookData: Map<String, Any>): String {
        val counterRef = firestore.collection("metadata").document("book_counter")
        val counterSnapshot = counterRef.get().await()
        var fallbackCount = 0L
        if (!counterSnapshot.exists()) {
            val countQuery = firestore.collection("books").count()
            val aggregateSnapshot = countQuery.get(AggregateSource.SERVER).await()
            fallbackCount = aggregateSnapshot.count
        }
        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val currentCount = if (snapshot.exists()) {
                snapshot.getLong("count") ?: 0L
            } else {
                fallbackCount
            }
            val nextNumber = currentCount + 1
            val newId = "book$nextNumber"
            val documentRef = firestore.collection("books").document(newId)
            val dataWithId = bookData.toMutableMap()
            dataWithId["id"] = nextNumber
            if (snapshot.exists()) {
                transaction.update(counterRef, "count", nextNumber)
            } else {
                transaction.set(counterRef, mapOf("count" to nextNumber))
            }
            transaction.set(documentRef, dataWithId)
            newId
        }.await()
    }

    suspend fun deleteUserDocument(userId: String) {
        try {
            firestore.collection("users").document(userId).delete().await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Lỗi xóa user document: ${e.message}")
            throw e
        }
    }
}