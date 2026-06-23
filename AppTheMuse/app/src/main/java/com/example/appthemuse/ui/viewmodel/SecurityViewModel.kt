package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Định nghĩa form dữ liệu nhập vào giống userForm của các màn hình trước
data class PasswordForm(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = ""
)

data class SecurityUiState(
    val isLoading: Boolean = false,
    val passwordForm: PasswordForm = PasswordForm(),
    val isSaveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class SecurityViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    // Lắng nghe thay đổi text từ các ô nhập liệu
    fun onCurrentPasswordChange(value: String) {
        _uiState.update { it.copy(passwordForm = it.passwordForm.copy(currentPassword = value)) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(passwordForm = it.passwordForm.copy(newPassword = value)) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(passwordForm = it.passwordForm.copy(confirmPassword = value)) }
    }

    // Xử lý gửi dữ liệu lên Firebase Auth để cập nhật mật khẩu
    fun updatePassword() {
        val form = _uiState.value.passwordForm
        if (form.newPassword != form.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu xác nhận không trùng khớp!") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        // TODO: Gọi hàm reauthenticate và updatePassword của Firebase Auth tại đây
        // Sau khi thành công:
        // _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
    }

    fun resetSaveState() {
        _uiState.update { it.copy(isSaveSuccess = false, errorMessage = null) }
    }
}