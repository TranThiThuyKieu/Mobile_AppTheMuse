package com.example.appthemuse.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Định nghĩa các trạng thái của màn hình Auth
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LoginSuccess(val hasGenres: Boolean) : AuthState()
    object RegisterSuccess : AuthState()
    object GenresUpdated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository, // ĐỔI TỪ SERVICE SANG REPOSITORY
    private val firestoreService: FirestoreService // Dùng để lấy danh sách categories tách biệt
) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    private val _categories = mutableStateOf<List<String>>(emptyList())
    val categories: State<List<String>> = _categories

    // 1. Tải thể loại từ FirestoreService
    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val list = firestoreService.getCategoriesList()
                _categories.value = list
            } catch (e: Exception) {
                _categories.value = emptyList()
            }
        }
    }

    // 2. Xử lý Đăng nhập qua Repository
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng điền đầy đủ thông tin!")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            // Gọi thông qua Repository nhận về một đối tượng Result chuẩn Kotlin
            authRepository.login(email, password)
                .onSuccess { firebaseUser ->
                    // Kiểm tra xem User đã chọn thể loại chưa qua Repository
                    authRepository.checkUserGenresSelected(firebaseUser.uid)
                        .onSuccess { hasGenres ->
                            _authState.value = AuthState.LoginSuccess(hasGenres)
                        }
                        .onFailure { error ->
                            _authState.value = AuthState.Error(error.localizedMessage ?: "Lỗi kiểm tra thể loại.")
                        }
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Tài khoản hoặc mật khẩu không chính xác!")
                }
        }
    }

    // 3. Xử lý Đăng ký tài khoản mới qua Repository
    fun register(email: String, password: String, username: String) {
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _authState.value = AuthState.Error("Không được để trống thông tin đăng ký!")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.register(email, password, username)
                .onSuccess {
                    _authState.value = AuthState.RegisterSuccess
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Đăng ký tài khoản thất bại!")
                }
        }
    }

    // 4. Xử lý Lưu thể loại yêu thích qua Repository
    fun saveFavoriteGenres(genres: List<String>) {
        // Tối ưu: Repository nên xử lý việc check login, nhưng tạm thời lấy uid an toàn
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUid == null) {
            _authState.value = AuthState.Error("Phiên đăng nhập hết hạn!")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.saveFavoriteGenres(currentUid, genres)
                .onSuccess {
                    _authState.value = AuthState.GenresUpdated
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Không thể lưu thể loại yêu thích!")
                }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}