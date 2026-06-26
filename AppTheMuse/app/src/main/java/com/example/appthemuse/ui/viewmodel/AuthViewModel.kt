package com.example.appthemuse.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.appthemuse.domain.repository.AuthRepository
import com.example.appthemuse.ui.model.UserUi
import com.example.appthemuse.ui.mapper.toUserUi // 👉 ĐÃ THÊM: Import hàm mapper

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    object WaitingForVerification : AuthState
    // 👉 ĐÃ SỬA: Chuyển từ chứa Boolean đơn thuần sang chứa đầy đủ thông tin UserUi để hiển thị ở UI
    data class LoginSuccess(val user: UserUi, val hasGenres: Boolean) : AuthState
    object RegisterSuccess : AuthState
    data class Error(val message: String) : AuthState
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    // 1. Đăng nhập bằng Email
    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { domainUser ->
                    // 👉 ĐÃ SỬA: Dùng mapper biến đổi dữ liệu Domain thành UI trước khi chạy tiếp
                    val userUi = domainUser.toUserUi()
                    checkGenresAndNavigate(userUi)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Đăng nhập thất bại")
                }
        }
    }

    // 2. Đăng ký tài khoản (Giữ nguyên)
    fun register(email: String, password: String, username: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.register(email, password, username)
                .onSuccess {
                    _authState.value = AuthState.RegisterSuccess
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Đăng ký thất bại")
                }
        }
    }

    // 3. Đăng nhập bằng Google
    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.loginWithGoogle(idToken)
                .onSuccess { domainUser ->
                    // 👉 ĐÃ SỬA: Dùng mapper đồng bộ với hàm login email
                    val userUi = domainUser.toUserUi()
                    checkGenresAndNavigate(userUi)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Lỗi đăng nhập Google")
                }
        }
    }

    // 👉 ĐÃ SỬA: Hàm nhận vào một UserUi hoàn chỉnh
    private suspend fun checkGenresAndNavigate(userUi: UserUi) {
        authRepository.checkUserGenresSelected(userUi.id)
            .onSuccess { hasGenres ->
                _authState.value = AuthState.LoginSuccess(user = userUi, hasGenres = hasGenres)
            }
            .onFailure {
                _authState.value = AuthState.LoginSuccess(user = userUi, hasGenres = false)
            }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
    fun sendVerifyEmail() {
        viewModelScope.launch {
            authRepository.sendEmailVerification()
        }
    }

    fun checkEmailVerified(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.isEmailVerified()
            onResult(result)
        }
    }

    fun deleteAccountIfExpired() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            authRepository.deleteUnverifiedAccount(userId)
        }
    }

}