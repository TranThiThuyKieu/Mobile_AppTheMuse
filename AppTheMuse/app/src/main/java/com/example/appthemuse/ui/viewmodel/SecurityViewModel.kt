package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
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
    private val auth = FirebaseAuth.getInstance()
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
        val user = auth.currentUser
        val form = _uiState.value.passwordForm
        
        if (form.currentPassword.isBlank() || form.newPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập đầy đủ thông tin") }
            return
        }

        if (form.newPassword.length < 6) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu mới phải có ít nhất 6 ký tự") }
            return
        }

        if (form.newPassword != form.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu xác nhận không trùng khớp!") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        // TODO: Gọi hàm reauthenticate và updatePassword của Firebase Auth tại đây
        // Sau khi thành công:
        // _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
        val email = user?.email ?: return
        val credential = EmailAuthProvider.getCredential(email, form.currentPassword)

        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.updatePassword(form.newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        _uiState.update { it.copy(isLoading = false, isSaveSuccess = true, passwordForm = PasswordForm()) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = updateTask.exception?.message) }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Mật khẩu hiện tại không chính xác") }
            }
        }
    }

    fun resetSaveState() {
        _uiState.update { it.copy(isSaveSuccess = false, errorMessage = null) }
    }
}