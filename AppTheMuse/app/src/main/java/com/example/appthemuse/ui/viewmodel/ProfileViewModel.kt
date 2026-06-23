package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.repository.UserRepository
import com.example.appthemuse.ui.model.UserUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserUi = UserUi(),
    val fontSize: String = "Trung bình",
    val fontSizeValue: Float = 0.5f,
    val lineSpacing: String = "Vừa",
    val themeMode: String = "Dark",
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refreshUserProfile()
    }

    fun refreshUserProfile() {
        if (userRepository.isUserLoggedIn()) {
            val email = userRepository.getCurrentUserEmail() ?: "Chưa cập nhật email"
            val uid = userRepository.getCurrentUserUid()

            // 🛠️ Sửa lỗi 1: Copy lồng email và trạng thái đang tải vào object user
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    user = currentState.user.copy(email = email, username = "")
                )
            }

            if (uid != null) {
                viewModelScope.launch {
                    try {
                        val name = userRepository.getUserName(uid)
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                user = currentState.user.copy(username = name)
                            )
                        }
                    } catch (e: Exception) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                errorMessage = e.localizedMessage,
                                user = currentState.user.copy(username = "Lỗi tải dữ liệu")
                            )
                        }
                    }
                }
            }
        } else {
            // 🛠️ Sửa lỗi 2: Copy lồng khi người dùng chưa đăng nhập
            _uiState.update { currentState ->
                currentState.copy(
                    user = currentState.user.copy(
                        username = "Chưa đăng nhập",
                        email = "Vui lòng đăng nhập lại"
                    )
                )
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
        viewModelScope.launch {
            userRepository.logout()
            // 🛠️ Sửa lỗi 3: Reset thông tin user sau khi đăng xuất thành công
            _uiState.update { currentState ->
                currentState.copy(
                    user = currentState.user.copy(
                        username = "Chưa đăng nhập",
                        email = "Vui lòng đăng nhập lại"
                    )
                )
            }
        }
    }
}