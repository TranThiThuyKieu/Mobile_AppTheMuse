package com.example.appthemuse.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class LoginSuccess(val hasGenres: Boolean) : AuthState
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
                .onSuccess { user ->
                    checkGenresAndNavigate(user.id)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Đăng nhập thất bại")
                }
        }
    }

    // 2. Đăng ký tài khoản
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
                .onSuccess { user ->
                    checkGenresAndNavigate(user.id)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Lỗi đăng nhập Google")
                }
        }
    }

    // Hàm kiểm tra thể loại để rẽ nhánh điều hướng
    private suspend fun checkGenresAndNavigate(userId: String) {
        authRepository.checkUserGenresSelected(userId)
            .onSuccess { hasGenres ->
                _authState.value = AuthState.LoginSuccess(hasGenres)
            }
            .onFailure {
                _authState.value = AuthState.LoginSuccess(hasGenres = false)
            }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}