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

/**
 * Lớp FirestoreService cung cấp các phương thức giao tiếp với cơ sở dữ liệu Firestore.
 * Đóng vai trò là lớp Data Access Object (DAO) xử lý nghiệp vụ truy xuất dữ liệu
 * phục vụ cho các chức năng của ứng dụng.
 */
class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()

    // Các bộ nhớ đệm (Cache) cục bộ để tối ưu hiệu năng truy vấn
    private val userCache = mutableMapOf<String, DocumentSnapshot?>()
    private val chapterCache = mutableMapOf<String, Int>()
    private val ratingCache = mutableMapOf<String, Double>()

    /**
     * Lấy dữ liệu người dùng từ collection "users" dựa trên ID.
     */
    suspend fun getUserDocument(userId: String): DocumentSnapshot {
        return firestore.collection("users").document(userId).get().await()
    }

    /**
     * Lưu trữ hoặc cập nhật thông tin người dùng vào Firestore.
     */
    suspend fun saveUserDocument(userId: String, userData: Map<String, Any>) {
        try {
            firestore.collection("users").document(userId).set(userData).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Error saving user: ${e.message}")
        }
    }

    /**
     * Cập nhật danh sách thể loại yêu thích của người dùng.
     */
    suspend fun updateFavoriteGenres(userId: String, genres: List<String>) {
        try {
            firestore.collection("users").document(userId).update("favorite_genres", genres).await()
        } catch (e: Exception) {
            // Log lỗi nếu cần thiết
        }
    }

    /**
     * Chức năng quản trị: Cập nhật trạng thái chặn (block) của người dùng.
     */
    suspend fun updateUserBlockStatus(userId: String, isBlocked: Boolean) {
        try {
            firestore.collection("users").document(userId).update("is_blocked", isBlocked).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Error updating block status: ${e.message}")
        }
    }

    /**
     * Lấy log bảo mật của người dùng dựa trên email.
     */
    suspend fun getSecurityLog(email: String): DocumentSnapshot {
        return firestore.collection("security_logs").document(email).get().await()
    }

    /**
     * Cập nhật thông tin log bảo mật (Merge dữ liệu cũ và mới).
     */
    suspend fun updateSecurityLog(email: String, data: Map<String, Any>) {
        firestore.collection("security_logs").document(email).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    /**
     * Cập nhật trạng thái của một cuốn sách (Ví dụ: Đang tiến hành, Hoàn thành).
     */
    suspend fun updateBookStatus(bookId: String, status: String) {
        try {
            firestore.collection("books").document(bookId).update("status", status).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Error updating book status: ${e.message}")
        }
    }

    /**
     * Lấy danh sách toàn bộ sách (hỗ trợ fallback sang collection tiếng Việt).
     */
    suspend fun getAllBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            firestore.collection("books").limit(limit).get().await().documents
        } catch (e: Exception) {
            firestore.collection("sách").limit(limit).get().await().documents
        }
    }

    /**
     * Lấy danh sách sách mới phát hành (sắp xếp theo thời gian tạo giảm dần).
     */
    suspend fun getNewReleaseBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            firestore.collection("books").orderBy("created_at", Query.Direction.DESCENDING).limit(limit).get().await().documents
        } catch (e: Exception) {
            firestore.collection("sách").orderBy("ngày_tạo", Query.Direction.DESCENDING).limit(limit).get().await().documents
        }
    }

    /**
     * Lấy danh sách sách thịnh hành (sắp xếp theo lượt xem).
     */
    suspend fun getTrendingBooksRaw(limit: Long): List<DocumentSnapshot> {
        return try {
            firestore.collection("books").orderBy("view_count", Query.Direction.DESCENDING).limit(limit).get().await().documents
        } catch (e: Exception) {
            firestore.collection("sách").orderBy("lượt_xem", Query.Direction.DESCENDING).limit(limit).get().await().documents
        }
    }

    /**
     * Lấy danh sách sách gần đây (wrapper của getAllBooksRaw).
     */
    suspend fun getRecentBooksRaw(limit: Long): List<DocumentSnapshot> {
        return getAllBooksRaw(limit)
    }

    /**
     * Gợi ý sách dựa trên danh sách các thể loại yêu thích của người dùng.
     */
    suspend fun getRecommendedBooksRaw(favoriteGenres: List<String>, limit: Long): List<DocumentSnapshot> {
        return try {
            if (favoriteGenres.isEmpty()) return getAllBooksRaw(limit)
            firestore.collection("books").whereArrayContainsAny("genres", favoriteGenres).limit(limit).get().await().documents
        } catch (e: Exception) {
            firestore.collection("sách").whereArrayContainsAny("thể_loại", favoriteGenres).limit(limit).get().await().documents
        }
    }

    /**
     * Truy xuất danh mục thể loại sách từ hệ thống.
     */
    suspend fun getCategoriesListRaw(): List<DocumentSnapshot> {
        return try {
            firestore.collection("categories").get().await().documents
        } catch (e: Exception) {
            firestore.collection("thể_loại_sách").get().await().documents
        }
    }

    /**
     * Lấy thông tin người dùng có sử dụng cơ chế lưu đệm (Cache) để giảm tải truy vấn.
     */
    suspend fun getUserById(userId: String): DocumentSnapshot? {
        userCache[userId]?.let { return it }
        val user = firestore.collection("users").document(userId).get().await()
        userCache[userId] = user
        return user
    }

    /**
     * Tính toán tổng số chương của một cuốn sách.
     */
    suspend fun getChapterCount(bookId: String): Int {
        chapterCache[bookId]?.let { return it }
        val bookNumId = bookId.removePrefix("book").toIntOrNull() ?: 0
        val count = firestore.collection("chapters").whereEqualTo("book_id", bookNumId).get().await().size()
        chapterCache[bookId] = count
        return count
    }

    /**
     * Tính toán điểm đánh giá trung bình dựa trên các bài review của người dùng.
     */
    suspend fun getAverageRating(bookId: String): Double {
        ratingCache[bookId]?.let { return it }
        val bookNumId = bookId.removePrefix("book").toIntOrNull()
        val query = firestore.collection("reviews").whereIn("book_id", listOfNotNull(bookId, bookNumId))
        val snapshot = query.get().await()
        val rating = if (snapshot.isEmpty) 0.0 else snapshot.documents.map { it.getLong("rating")?.toDouble() ?: 0.0 }.average()
        ratingCache[bookId] = rating
        return rating
    }

    /**
     * Lấy tổng số lượt yêu thích của cuốn sách.
     */
    suspend fun getVoteCount(bookId: String): Int {
        return try {
            val bookNumId = bookId.removePrefix("book").toIntOrNull() ?: 0
            firestore.collection("favorites").whereEqualTo("book_id", bookNumId).get().await().size()
        } catch (e: Exception) { 0 }
    }

    /**
     * Lấy tổng số lượt bình luận của cuốn sách.
     */
    suspend fun getCommentCount(bookId: String): Int {
        return try {
            val bookNumId = bookId.removePrefix("book").toIntOrNull()
            firestore.collection("reviews").whereIn("book_id", listOfNotNull(bookId, bookNumId)).get().await().size()
        } catch (e: Exception) { 0 }
    }

    /**
     * Lấy danh sách các chương của cuốn sách.
     */
    suspend fun getChaptersRaw(bookId: String): List<DocumentSnapshot> {
        return try {
            val bookNumId = bookId.removePrefix("book").toIntOrNull() ?: return emptyList()
            firestore.collection("chapters").whereEqualTo("book_id", bookNumId).get().await().documents
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "getChaptersRaw error: ${e.message}")
            emptyList()
        }
    }

    /**
     * Tạo một chương mới và cập nhật số lượng chương trong thông tin sách.
     */
    suspend fun createChapterRaw(bookId: String, chapterData: Map<String, Any>): String {
        val bookNumId = bookId.removePrefix("book").toIntOrNull() ?: return ""
        val existingCount = firestore.collection("chapters").whereEqualTo("book_id", bookNumId).get().await().size()
        val nextChapterNumber = existingCount + 1
        val documentId = "${bookId}_chapter$nextChapterNumber"
        val dataWithNumber = chapterData.toMutableMap().apply {
            put("chapter_number", nextChapterNumber)
            put("book_id", bookNumId)
            put("created_at", Timestamp.now())
            put("view_count", 0L)
            put("status", "đã đăng")
        }
        firestore.collection("chapters").document(documentId).set(dataWithNumber).await()
        firestore.collection("books").document(bookId).update("chapter_count", nextChapterNumber).await()
        chapterCache.remove(bookId)
        return documentId
    }

    /**
     * Lưu từ khóa tìm kiếm vào lịch sử người dùng.
     */
    suspend fun addSearchHistory(userId: String, keyword: String) {
        if (keyword.isBlank()) return
        firestore.collection("users").document(userId).collection("search_history").document(keyword)
            .set(mapOf("keyword" to keyword, "timestamp" to Timestamp.now()))
    }

    /**
     * Truy xuất 7 từ khóa tìm kiếm gần nhất.
     */
    suspend fun getSearchHistory(userId: String): List<String> {
        return firestore.collection("users").document(userId).collection("search_history")
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(7).get().await().documents.mapNotNull { it.getString("keyword") }
    }

    /**
     * Lấy danh sách các cuốn sách người dùng đã đánh dấu yêu thích.
     */
    suspend fun getFavoriteDocuments(userId: String): List<DocumentSnapshot> {
        return firestore.collection("favorites").whereEqualTo("user_id", userId).get().await().documents
    }

    /**
     * Lấy thông tin chi tiết sách theo document ID.
     */
    suspend fun getBookByDocumentId(bookId: String): DocumentSnapshot? {
        val doc = firestore.collection("books").document(bookId).get().await()
        return if (doc.exists()) doc else null
    }

    /**
     * Lấy danh sách lịch sử đọc của người dùng.
     */
    suspend fun getHistoryDocuments(userId: String): List<DocumentSnapshot> {
        val docs = firestore.collection("history").whereEqualTo("user_id", userId).get().await().documents
        return docs.groupBy { it.getString("book_id") }
            .mapNotNull { (_, groupDocs) ->
                groupDocs.maxByOrNull { it.getTimestamp("read_at")?.seconds ?: 0L }
            }
            .sortedByDescending { it.getTimestamp("read_at")?.seconds ?: 0L }
    }

    /**
     * Lấy tiến trình đọc sách hiện tại của người dùng.
     */
    suspend fun getReadingProgress(userId: String, bookId: String): DocumentSnapshot? {
        val bookNumId = bookId.removePrefix("book").toIntOrNull() ?: 0
        return firestore.collection("reading_progress").whereEqualTo("user_id", userId)
            .whereEqualTo("book_id", bookNumId).get().await().documents.firstOrNull()
    }

    /**
     * Cập nhật tiến độ đọc và thêm vào bảng lịch sử đọc của người dùng.
     */
    suspend fun updateReadingProgress(userId: String, bookId: String, chapterNumber: Int, scrollPosition: Int) {
        val bookNumId = bookId.removePrefix("book").toIntOrNull() ?: 0
        val progressRef = firestore.collection("reading_progress")
        val existing = progressRef.whereEqualTo("user_id", userId).whereEqualTo("book_id", bookNumId).get().await().documents.firstOrNull()

        val data = mapOf(
            "user_id" to userId, "book_id" to bookNumId, "chapter_number" to chapterNumber,
            "scroll_position" to scrollPosition, "updated_at" to Timestamp.now()
        )

        if (existing != null) existing.reference.update(data).await() else progressRef.add(data).await()

        val historyRef = firestore.collection("history")
        val existingHistory = historyRef.whereEqualTo("user_id", userId).whereEqualTo("book_id", bookId).get().await().documents
        
        if (existingHistory.isNotEmpty()) {
            existingHistory.first().reference.update("read_at", Timestamp.now()).await()
            if (existingHistory.size > 1) {
                for (i in 1 until existingHistory.size) {
                    existingHistory[i].reference.delete().await()
                }
            }
        } else {
            historyRef.add(mapOf("user_id" to userId, "book_id" to bookId, "read_at" to Timestamp.now())).await()
        }
    }

    /**
     * Tăng lượt xem của sách bằng nguyên tử (Atomic increment).
     */
    suspend fun incrementViewCount(bookId: String) {
        firestore.collection("books").document(bookId).update("view_count", FieldValue.increment(1)).await()
    }

    /**
     * Lấy danh sách sách của một tác giả cụ thể.
     */
    suspend fun getBooksByAuthorRaw(authorId: String): List<DocumentSnapshot> {
        return try {
            firestore.collection("books").whereEqualTo("author_id", authorId).get().await().documents
        } catch (e: Exception) {
            firestore.collection("sách").whereEqualTo("tác_giả_id", authorId).get().await().documents
        }
    }

    /**
     * Upload ảnh bìa sách lên dịch vụ ImgBB và lấy về URL.
     */
    suspend fun uploadBookCoverToImgBB(base64Image: String): String = withContext(Dispatchers.IO) {
        // Chi tiết thực hiện POST request tới API ImgBB
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
            org.json.JSONObject(response).getJSONObject("data").getString("url")
        } else {
            throw Exception("Lỗi ImgBB: ${connection.responseCode}")
        }
    }

    /**
     * Khởi tạo một cuốn sách mới và đảm bảo ID sách được tăng tự động (Atomic Transaction).
     */
    suspend fun createBookRaw(bookData: Map<String, Any>): String {
        val counterRef = firestore.collection("metadata").document("book_counter")
        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val currentCount = snapshot.getLong("count") ?: 0L
            val nextNumber = currentCount + 1
            val newId = "book$nextNumber"
            transaction.set(counterRef, mapOf("count" to nextNumber))
            transaction.set(firestore.collection("books").document(newId), bookData.toMutableMap().apply { put("id", nextNumber) })
            newId
        }.await()
    }

    /**
     * Xóa hồ sơ người dùng khỏi hệ thống.
     */
    suspend fun deleteUserDocument(userId: String) {
        try {
            firestore.collection("users").document(userId).delete().await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Lỗi xóa user document: ${e.message}")
            throw e
        }
    }

    /**
     * Đảo trạng thái yêu thích của sách đối với người dùng (Like/Unlike).
     */
    suspend fun toggleFavorite(userId: String, bookId: String) {
        val favoritesRef = firestore.collection("favorites")
        val existingDoc = favoritesRef.whereEqualTo("user_id", userId).whereEqualTo("book_id", bookId).get().await().documents.firstOrNull()

        if (existingDoc != null) {
            existingDoc.reference.delete().await()
        } else {
            favoritesRef.add(mapOf("user_id" to userId, "book_id" to bookId, "created_at" to Timestamp.now())).await()
        }
    }

    /**
     * Lấy danh sách review đã qua kiểm duyệt và sắp xếp theo thời gian mới nhất.
     */
    suspend fun getReviewsRaw(bookId: String): List<DocumentSnapshot> {
        val bookNumId = bookId.removePrefix("book").toIntOrNull()
        val results = firestore.collection("reviews").whereIn("book_id", listOfNotNull(bookId, bookNumId)).get().await().documents
        return results.filter { it.getBoolean("is_hidden") == false }.sortedByDescending { it.getTimestamp("created_at") }
    }

    /**
     * Thêm một đánh giá mới cho sách.
     */
    suspend fun addReview(reviewData: Map<String, Any>) {
        firestore.collection("reviews").add(reviewData).await()
    }
}