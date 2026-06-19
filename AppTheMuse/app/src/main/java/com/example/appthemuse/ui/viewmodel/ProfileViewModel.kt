package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val username: String = "",
    val email: String = "",
    val readCount: Int = 12,
    val favoriteCount: Int = 48,
    val downloadedCount: Int = 5,
    val fontSize: String = "Trung bình",
    val fontSizeValue: Float = 0.5f,
    val lineSpacing: String = "Vừa",
    val themeMode: String = "Dark"
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                _uiState.update {
                    it.copy(username = "Chưa đăng nhập", email = "Vui lòng đăng nhập lại")
                }
            }
        }
        refreshUserProfile()
    }

    fun refreshUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val email = currentUser.email ?: "Chưa cập nhật email"

            _uiState.update {
                it.copy(username = "", email = email)
            }

            // Gọi thẳng Firestore để lấy thông tin thực tế
            fetchUserData(currentUser.uid)
        } else {
            _uiState.update {
                it.copy(username = "Chưa đăng nhập", email = "Vui lòng đăng nhập lại")
            }
        }
    }

    private fun fetchUserData(uid: String) {
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firestoreName = document.getString("username")
                    if (!firestoreName.isNullOrEmpty()) {
                        // Tải thành công từ Firestore -> Cập nhật tên thực tế lên UI
                        _uiState.update { it.copy(username = firestoreName) }
                    } else {
                        // Trường hợp tài khoản tồn tại nhưng document trống/không có trường username
                        _uiState.update { it.copy(username = "Người dùng") }
                    }
                } else {
                    // Document không tồn tại
                    _uiState.update { it.copy(username = "Người dùng") }
                }
            }
            .addOnFailureListener {
                // Thất bại do mất mạng hoặc lỗi truy vấn -> Hiển thị thông báo lỗi thay vì treo "Đang tải..."
                _uiState.update { it.copy(username = "Lỗi tải dữ liệu") }
            }
    }

    fun updateFontSize(value: Float) {
        val label = when {
            value < 0.33f -> "Nhỏ"
            value < 0.66f -> "Trung bình"
            else -> "Lớn"
        }
        _uiState.update { it.copy(fontSizeValue = value, fontSize = label) }
    }

    fun updateLineSpacing(spacing: String) {
        _uiState.update { it.copy(lineSpacing = spacing) }
    }

    fun updateThemeMode(theme: String) {
        _uiState.update { it.copy(themeMode = theme) }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}