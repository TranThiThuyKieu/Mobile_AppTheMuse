package com.example.appthemuse.data.remote

import com.example.appthemuse.domain.model.CategoryModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()

    // ĐỔI TỪ List<String> SANG List<CategoryModel>
    suspend fun getCategoriesList(): List<CategoryModel> {
        return try {
            val snapshot = firestore.collection("categories").get().await()
            snapshot.documents.map { document ->
                CategoryModel(
                    id = document.id,
                    name = document.getString("name") ?: "Chưa đặt tên"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}