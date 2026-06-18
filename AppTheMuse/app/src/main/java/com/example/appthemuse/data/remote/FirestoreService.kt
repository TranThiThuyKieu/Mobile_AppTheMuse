package com.example.appthemuse.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
}