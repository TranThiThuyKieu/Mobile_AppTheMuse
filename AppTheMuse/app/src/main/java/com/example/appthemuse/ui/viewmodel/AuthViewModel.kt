package com.example.appthemuse.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.appthemuse.domain.repository.AuthRepository
import com.example.appthemuse.ui.model.UserUi
import com.example.appthemuse.ui.mapper.toUserUi

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    object WaitingForVerification : AuthState
    data class LoginSuccess(val user: UserUi, val hasGenres: Boolean) : AuthState
    object RegisterSuccess : AuthState
    data class Error(val message: String) : AuthState
    object PasswordResetSent : AuthState
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { domainUser ->
                    val userUi = domainUser.toUserUi()
                    checkGenresAndNavigate(userUi)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Đăng nhập thất bại")
                }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.loginWithGoogle(idToken)
                .onSuccess { domainUser ->
                    val userUi = domainUser.toUserUi()
                    checkGenresAndNavigate(userUi)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Đăng nhập Google thất bại")
                }
        }
    }

    fun register(email: String, password: String, username: String) {
        // ✅ KIỂM TRA MẬT KHẨU MẠNH: 8 ký tự, 1 hoa, 1 thường, 1 số, 1 ký hiệu
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$".toRegex()
        if (!password.matches(passwordRegex)) {
            _authState.value = AuthState.Error("Mật khẩu phải từ 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt!")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.register(email, password, username)
                .onSuccess {
                    _authState.value = AuthState.WaitingForVerification
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Đăng ký thất bại")
                }
        }
    }

    // ✅ QUÊN MẬT KHẨU VỚI GIỚI HẠN 5 LẦN
    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng nhập Email!")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _authState.value = AuthState.PasswordResetSent
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Lỗi gửi yêu cầu")
                }
        }
    }

    private suspend fun checkGenresAndNavigate(userUi: UserUi) {
        authRepository.checkUserGenresSelected(userUi.id)
            .onSuccess { hasGenres ->
                _authState.value = AuthState.LoginSuccess(user = userUi, hasGenres = hasGenres)
            }
            .onFailure {
                _authState.value = AuthState.LoginSuccess(user = userUi, hasGenres = false)
            }
    }

    fun sendVerifyEmail(onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            authRepository.sendEmailVerification()
                .onSuccess { onResult(true, null) }
                .onFailure { error -> onResult(false, error.message) }
        }
    }

    fun checkEmailVerified(onVerified: () -> Unit) {
        viewModelScope.launch {
            val isVerified = authRepository.isEmailVerified()
            if (isVerified) {
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    authRepository.updateUserBlockStatus(userId, false)
                    authRepository.checkUserGenresSelected(userId)
                        .onSuccess { hasGenres ->
                            _authState.value = AuthState.LoginSuccess(UserUi(id = userId), hasGenres)
                            onVerified()
                        }
                }
            }
        }
    }

    // ✅ HỦY ĐĂNG KÝ VÀ XÓA TÀI KHOẢN (Khi hết giờ hoặc bấm Hủy)
    fun deleteAccountIfExpired(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                authRepository.deleteUnverifiedAccount(userId)
            }
            _authState.value = AuthState.Idle
            onComplete()
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
