package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.model.AdminBook
import com.example.appthemuse.ui.model.UserUi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminUserDetailUiState(
    val user: UserUi? = null,
    val publishedBooks: List<AdminBook> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class AdminUserDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUserDetailUiState())
    val uiState: StateFlow<AdminUserDetailUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun loadUserDetail(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch user info
                val userDoc = firestore.collection("users").document(userId).get().await()
                if (userDoc.exists()) {
                    val userUi = UserUi(
                        id = userId,
                        username = userDoc.getString("username") ?: "",
                        email = userDoc.getString("email") ?: "",
                        role = userDoc.getString("role") ?: "user",
                        fullName = userDoc.getString("fullName") ?: "",
                        phoneNumber = userDoc.getString("phoneNumber") ?: "",
                        birthday = userDoc.getString("birthday") ?: "",
                        gender = userDoc.getString("gender") ?: "",
                        isBlocked = userDoc.getBoolean("is_blocked") ?: false,
                        favoriteGenres = emptyList()
                    )
                    
                    // Fetch user's books
                    val booksSnapshot = firestore.collection("books")
                        .whereEqualTo("author_id", userId)
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
                            chapterCount = 0, // Simplified for now
                            reviewCount = 0,
                            averageRating = 0.0,
                            createdAt = doc.getDate("created_at")
                        )
                    }

                    _uiState.update { it.copy(user = userUi, publishedBooks = books, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Người dùng không tồn tại") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateUserProfile(updatedUser: UserUi) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            try {
                val data = mapOf(
                    "username" to updatedUser.username,
                    "fullName" to updatedUser.fullName,
                    "phoneNumber" to updatedUser.phoneNumber,
                    "birthday" to updatedUser.birthday,
                    "gender" to updatedUser.gender
                )
                firestore.collection("users").document(updatedUser.id).update(data).await()
                _uiState.update { it.copy(user = updatedUser, isUpdating = false, message = "Cập nhật thành công") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUpdating = false, error = e.message) }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.update { it.copy(message = "Link đặt lại mật khẩu đã được gửi đến email") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
