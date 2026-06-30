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

// ViewModel xử lý Đăng nhập, Đăng ký, Quên mật khẩu
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    // Đăng nhập bằng email và mật khẩu
    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
            _authState.value = AuthState.Error("Vui lòng nhập đầy đủ thông tin!")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.login(trimmedEmail, trimmedPassword)
                .onSuccess { domainUser -> checkGenresAndNavigate(domainUser.toUserUi()) }
                .onFailure { error -> _authState.value = AuthState.Error(error.message ?: "Đăng nhập thất bại") }
        }
    }

    // Đăng nhập bằng Google
    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.loginWithGoogle(idToken)
                .onSuccess { domainUser -> checkGenresAndNavigate(domainUser.toUserUi()) }
                .onFailure { error -> _authState.value = AuthState.Error(error.message ?: "Đăng nhập Google thất bại") }
        }
    }

    // Đăng ký tài khoản mới
    fun register(email: String, password: String, confirmPassword: String, username: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng nhập đầy đủ thông tin!")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Mật khẩu xác nhận không trùng khớp!")
            return
        }

        // Kiểm tra độ mạnh mật khẩu (8 ký tự, chữ hoa, chữ thường, số, ký tự đặc biệt)
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$".toRegex()
        if (!password.matches(passwordRegex)) {
            _authState.value = AuthState.Error("Mật khẩu chưa đủ mạnh!")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.register(email, password, username)
                .onSuccess { _authState.value = AuthState.WaitingForVerification }
                .onFailure { error -> _authState.value = AuthState.Error(error.message ?: "Đăng ký thất bại") }
        }
    }

    // Gửi email khôi phục mật khẩu
    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng nhập Email!")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email)
                .onSuccess { _authState.value = AuthState.PasswordResetSent }
                .onFailure { error -> _authState.value = AuthState.Error(error.message ?: "Lỗi gửi yêu cầu") }
        }
    }

    // Kiểm tra xem user đã chọn thể loại truyện chưa 
    private suspend fun checkGenresAndNavigate(userUi: UserUi) {
        authRepository.checkUserGenresSelected(userUi.id)
            .onSuccess { hasGenres -> _authState.value = AuthState.LoginSuccess(user = userUi, hasGenres = hasGenres) }
            .onFailure { _authState.value = AuthState.LoginSuccess(user = userUi, hasGenres = false) }
    }

    // Gửi email xác thực tài khoản
    fun sendVerifyEmail(onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            authRepository.sendEmailVerification()
                .onSuccess { onResult(true, null) }
                .onFailure { error -> onResult(false, error.message) }
        }
    }

    // Kiểm tra trạng thái xác thực email
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

    // Xóa tài khoản chưa xác thực nếu quá hạn
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

    // Khôi phục trạng thái ban đầu
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}