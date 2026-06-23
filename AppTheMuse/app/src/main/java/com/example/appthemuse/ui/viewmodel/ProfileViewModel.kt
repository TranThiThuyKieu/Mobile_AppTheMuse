package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.repository.UserRepository
import com.example.appthemuse.ui.model.UserUi
import com.example.appthemuse.ui.mapper.toUserUi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserUi? = null,
    val readCount: Int = 12,
    val favoriteCount: Int = 48,
    val downloadedCount: Int = 5,
    val fontSize: String = "Trung bình",
    val fontSizeValue: Float = 0.5f,
    val lineSpacing: String = "Vừa",
    val themeMode: String = "Dark"
)

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val firebaseAuth: FirebaseAuth = firebaseAuthInstance()

    private fun firebaseAuthInstance(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    init {
        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                _uiState.update {
                    it.copy(user = UserUi(id = "", username = "Chưa đăng nhập", email = "Vui lòng đăng nhập lại", role = "user", isBlocked = false, favoriteGenres = emptyList()))
                }
            }
        }
        refreshUserProfile()
    }

    fun refreshUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                userRepository.getUserProfile(currentUser.uid)
                    .onSuccess { userDomain ->
                        _uiState.update {
                            it.copy(user = userDomain.toUserUi())
                        }
                    }
                    .onFailure {
                        _uiState.update {
                            it.copy(user = UserUi(id = currentUser.uid, username = "Lỗi tải dữ liệu", email = currentUser.email ?: "", role = "user", isBlocked = false, favoriteGenres = emptyList()))
                        }
                    }
            }
        } else {
            _uiState.update {
                it.copy(user = UserUi(id = "", username = "Chưa đăng nhập", email = "Vui lòng đăng nhập lại", role = "user", isBlocked = false, favoriteGenres = emptyList()))
            }
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