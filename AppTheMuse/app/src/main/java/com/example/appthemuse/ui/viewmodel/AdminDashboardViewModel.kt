package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.model.AdminBook
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

data class AdminDashboardUiState(
    val newUserCount: String = "0",
    val newUserChange: String = "0%",
    val todayReadCount: String = "0",
    val todayReadChange: String = "0",
    val weeklyReadingTrend: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val topBooks: List<AdminBook> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFilter: String = "Tuần này", // Hôm nay, Tuần này, Tuần trước, Tháng này, Năm này, Tất cả
    val adminName: String = "Quản trị viên",
    val adminRole: String = "Ban biên tập The Muse"
)

class AdminDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadDashboardData()
    }

    fun onFilterChanged(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Load Top Hot Books
                val booksSnapshot = firestore.collection("books")
                    .orderBy("view_count", Query.Direction.DESCENDING)
                    .limit(5)
                    .get().await()
                
                val books = booksSnapshot.documents.map { doc ->
                    AdminBook(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        authorId = doc.getString("author_id") ?: "",
                        categoryId = doc.get("category_id")?.toString() ?: "",
                        coverUrl = doc.getString("cover_url") ?: "",
                        description = doc.getString("description") ?: "",
                        status = com.example.appthemuse.domain.model.BookStatus.fromValue(doc.getString("status")),
                        isPremium = doc.getBoolean("is_premium") ?: false,
                        viewCount = doc.getLong("view_count")?.toInt() ?: 0,
                        chapterCount = 0,
                        reviewCount = 0,
                        averageRating = 0.0,
                        createdAt = doc.getDate("created_at")
                    )
                }

                // 2. Load Stats
                val usersSnapshot = firestore.collection("users").get().await()
                val totalUserCount = usersSnapshot.size()
                
                // Get admin info from current user
                val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (currentUid != null) {
                    val adminDoc = firestore.collection("users").document(currentUid).get().await()
                    if (adminDoc.exists()) {
                        _uiState.update { it.copy(
                            adminName = adminDoc.getString("username") ?: "Quản trị viên",
                            adminRole = if (adminDoc.getString("role") == "admin") "Ban biên tập The Muse" else "Người dùng"
                        ) }
                    }
                }

                // Calculate total views for todayReadCount
                val allBooksSnapshot = firestore.collection("books").get().await()
                val totalViews = allBooksSnapshot.documents.sumOf { it.getLong("view_count") ?: 0L }

                val filter = _uiState.value.selectedFilter
                val mockTrend = when(filter) {
                    "Tuần này" -> listOf(30f, 45f, 28f, 55f, 40f, 70f, 60f)
                    "Tuần trước" -> listOf(20f, 35f, 40f, 30f, 45f, 50f, 40f)
                    else -> listOf(50f, 50f, 50f, 50f, 50f, 50f, 50f)
                }

                _uiState.update { 
                    it.copy(
                        topBooks = books,
                        newUserCount = totalUserCount.toString(),
                        newUserChange = "+12%",
                        todayReadCount = totalViews.toString(),
                        weeklyReadingTrend = mockTrend,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
