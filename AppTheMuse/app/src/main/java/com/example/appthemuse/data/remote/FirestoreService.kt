package com.example.appthemuse.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import com.google.firebase.Timestamp
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.FieldValue

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

    suspend fun getSecurityLog(email: String): DocumentSnapshot {
        return firestore.collection("security_logs").document(email).get().await()
    }

    suspend fun updateSecurityLog(email: String, data: Map<String, Any>) {
        firestore.collection("security_logs").document(email).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    suspend fun updateBookStatus(bookId: String, status: String) {
        try {
            firestore.collection("books").document(bookId).update("status", status).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Error updating book status: ${e.message}")
        }
    }

    suspend fun getAllBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("books").limit(limit).get().await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách").limit(limit).get().await()
            snapshot.documents
        }
    }

    suspend fun getNewReleaseBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("books")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .orderBy("ngày_tạo", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            snapshot.documents
        }
    }

    suspend fun getTrendingBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("books")
                .orderBy("view_count", Query.Direction.DESCENDING)
                .limit(limit).get().await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .orderBy("lượt_xem", Query.Direction.DESCENDING)
                .limit(limit).get().await()
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
                .limit(limit).get().await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách")
                .whereArrayContainsAny("thể_loại", favoriteGenres)
                .limit(limit).get().await()
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
        userCache[userId]?.let { return it }
        val user = firestore.collection("users").document(userId).get().await()
        userCache[userId] = user
        return user
    }

    suspend fun getChapterCount(bookId: String): Int {
        chapterCache[bookId]?.let { return it }
        val count = getChaptersRaw(bookId).size
        chapterCache[bookId] = count
        return count
    }

    suspend fun getAverageRating(bookId: String): Double {
        ratingCache[bookId]?.let { return it }
        val bookNumId = bookId.removePrefix("book").toLongOrNull()
        val query = firestore.collection("reviews")
            .whereIn("book_id", listOfNotNull(bookId, bookNumId, bookNumId?.toString()))

        val snapshot = query.get().await()
        val rating = if (snapshot.isEmpty) 0.0 else snapshot.documents.map { it.getLong("rating")?.toDouble() ?: 0.0 }.average()
        ratingCache[bookId] = rating
        return rating
    }

    suspend fun getVoteCount(bookId: String): Int {
        val bookNumId = bookId.removePrefix("book").toLongOrNull()
        return firestore.collection("favorites")
            .whereIn("book_id", listOfNotNull(bookId, bookNumId, bookNumId?.toString()))
            .get().await().size()
    }

    suspend fun getCommentCount(bookId: String): Int {
        val bookNumId = bookId.removePrefix("book").toLongOrNull()
        return firestore.collection("reviews")
            .whereIn("book_id", listOfNotNull(bookId, bookNumId, bookNumId?.toString()))
            .get().await().size()
    }

    suspend fun getChaptersRaw(bookId: String): List<DocumentSnapshot> {
        return try {
            val bookNumId = bookId.removePrefix("book").toLongOrNull()
            val ids = listOfNotNull(bookId, bookNumId, bookNumId?.toString())
            
            // 1. Thử collection "chapters" với nhiều kiểu book_id
            var snapshot = firestore.collection("chapters").whereIn("book_id", ids).get().await()
            if (!snapshot.isEmpty) return snapshot.documents

            // 2. Fallback: collection "chương" hoặc field "sách_id"
            snapshot = firestore.collection("chương").whereIn("sách_id", ids).get().await()
            if (!snapshot.isEmpty) return snapshot.documents

            snapshot = firestore.collection("chapters").whereIn("sách_id", ids).get().await()
            if (!snapshot.isEmpty) return snapshot.documents
            
            emptyList()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "getChaptersRaw error: ${e.message}")
            emptyList()
        }
    }

    suspend fun createChapterRaw(bookId: String, chapterData: Map<String, Any>): String {
        val bookNumId = bookId.removePrefix("book").toLongOrNull()
        val existingCount = getChaptersRaw(bookId).size
        val nextChapterNumber = existingCount + 1
        val documentId = "${bookId}_chapter$nextChapterNumber"
        val dataWithNumber = chapterData.toMutableMap()
        dataWithNumber["chapter_number"] = nextChapterNumber
        dataWithNumber["book_id"] = bookNumId ?: bookId
        dataWithNumber["created_at"] = Timestamp.now()
        dataWithNumber["view_count"] = 0L
        dataWithNumber["status"] = "đã đăng"
        firestore.collection("chapters").document(documentId).set(dataWithNumber).await()
        firestore.collection("books").document(bookId).update("chapter_count", nextChapterNumber).await()
        chapterCache.remove(bookId)
        return documentId
    }

    suspend fun addSearchHistory(userId: String, keyword: String) {
        if (keyword.isBlank()) return
        firestore.collection("users").document(userId)
            .collection("search_history").document(keyword)
            .set(mapOf("keyword" to keyword, "timestamp" to Timestamp.now()))
    }

    suspend fun getSearchHistory(userId: String): List<String> {
        return firestore.collection("users").document(userId)
            .collection("search_history").orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(7).get().await().documents.mapNotNull { it.getString("keyword") }
    }

    suspend fun getFavoriteDocuments(userId: String): List<DocumentSnapshot> {
        return firestore.collection("favorites").whereEqualTo("user_id", userId).get().await().documents
    }

    suspend fun getBookByDocumentId(bookId: String): DocumentSnapshot? {
        val doc = firestore.collection("books").document(bookId).get().await()
        return if (doc.exists()) doc else null
    }

    suspend fun getHistoryDocuments(userId: String): List<DocumentSnapshot> {
        return firestore.collection("history").whereEqualTo("user_id", userId).get().await().documents
    }

    suspend fun getReadingProgress(userId: String, bookId: String): DocumentSnapshot? {
        val bookNumId = bookId.removePrefix("book").toLongOrNull()
        return firestore.collection("reading_progress")
            .whereEqualTo("user_id", userId)
            .whereIn("book_id", listOfNotNull(bookId, bookNumId, bookNumId?.toString()))
            .get().await().documents.firstOrNull()
    }

    suspend fun updateReadingProgress(userId: String, bookId: String, chapterNumber: Int, scrollPosition: Int) {
        val bookNumId = bookId.removePrefix("book").toLongOrNull()
        val progressRef = firestore.collection("reading_progress")
        val existing = getReadingProgress(userId, bookId)

        val data = mapOf(
            "user_id" to userId,
            "book_id" to (bookNumId ?: bookId),
            "chapter_number" to chapterNumber,
            "scroll_position" to scrollPosition,
            "updated_at" to Timestamp.now()
        )

        if (existing != null) existing.reference.update(data).await()
        else progressRef.add(data).await()

        firestore.collection("history").add(mapOf("user_id" to userId, "book_id" to bookId, "read_at" to Timestamp.now())).await()
    }

    suspend fun incrementViewCount(bookId: String) {
        firestore.collection("books").document(bookId).update("view_count", FieldValue.increment(1)).await()
    }

    suspend fun getBooksByAuthorRaw(authorId: String): List<DocumentSnapshot> {
        return try {
            val snapshot = firestore.collection("books").whereEqualTo("author_id", authorId).get().await()
            snapshot.documents
        } catch (e: Exception) {
            val snapshot = firestore.collection("sách").whereEqualTo("tác_giả_id", authorId).get().await()
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
        if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val dataObject = org.json.JSONObject(response).getJSONObject("data")
            return@withContext dataObject.getString("url")
        } else throw Exception("Lỗi ImgBB")
    }

    suspend fun createBookRaw(bookData: Map<String, Any>): String {
        val counterRef = firestore.collection("metadata").document("book_counter")
        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val nextNumber = (snapshot.getLong("count") ?: 0L) + 1
            val newId = "book$nextNumber"
            val dataWithId = bookData.toMutableMap()
            dataWithId["id"] = nextNumber
            transaction.set(counterRef, mapOf("count" to nextNumber))
            transaction.set(firestore.collection("books").document(newId), dataWithId)
            newId
        }.await()
    }

    suspend fun deleteUserDocument(userId: String) {
        firestore.collection("users").document(userId).delete().await()
    }

    suspend fun toggleFavorite(userId: String, bookId: String) {
        val bookNumId = bookId.removePrefix("book").toLongOrNull()
        val favoritesRef = firestore.collection("favorites")
        val existing = favoritesRef.whereEqualTo("user_id", userId)
            .whereIn("book_id", listOfNotNull(bookId, bookNumId, bookNumId?.toString()))
            .get().await().documents.firstOrNull()

        if (existing != null) existing.reference.delete().await()
        else favoritesRef.add(mapOf("user_id" to userId, "book_id" to (bookNumId ?: bookId), "created_at" to Timestamp.now())).await()
    }

    suspend fun getReviewsRaw(bookId: String): List<DocumentSnapshot> {
        val bookNumId = bookId.removePrefix("book").toLongOrNull()
        return firestore.collection("reviews")
            .whereIn("book_id", listOfNotNull(bookId, bookNumId, bookNumId?.toString()))
            .get().await().documents
            .filter { it.getBoolean("is_hidden") == false }
            .sortedByDescending { it.getTimestamp("created_at") }
    }

    suspend fun addReview(reviewData: Map<String, Any>) {
        firestore.collection("reviews").add(reviewData).await()
    }
}
