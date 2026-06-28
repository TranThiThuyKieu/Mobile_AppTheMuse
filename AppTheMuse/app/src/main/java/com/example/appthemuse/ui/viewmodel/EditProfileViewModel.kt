// File: app/src/main/java/com/example/appthemuse/ui/viewmodel/EditProfileViewModel.kt
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

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val userForm: UserUi = UserUi(),
    val errorMessage: String? = null
)

class EditProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val uid = userRepository.getCurrentUserUid()
        if (uid != null) {
            _uiState.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                val profile = userRepository.getFullUserProfile(uid)
                if (profile != null) {
                    _uiState.update { it.copy(isLoading = false, userForm = profile) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Không thể tải thông tin") }
                }
            }
        }
    }

    // Các hàm cập nhật form khi User gõ chữ trên ô Text
    fun onFullNameChange(newValue: String) {
        _uiState.update { it.copy(userForm = it.userForm.copy(fullName = newValue)) }
    }

    fun onPhoneChange(newValue: String) {
        _uiState.update { it.copy(userForm = it.userForm.copy(phoneNumber = newValue)) }
    }

    fun onBirthdayChange(newValue: String) {
        _uiState.update { it.copy(userForm = it.userForm.copy(birthday = newValue)) }
    }

    fun onGenderChange(newValue: String) {
        _uiState.update { it.copy(userForm = it.userForm.copy(gender = newValue)) }
    }

    fun onUsernameChange(newValue: String) {
        _uiState.update { it.copy(userForm = it.userForm.copy(username = newValue)) }
    }

    fun onAvatarUrlChange(newValue: String) {
        _uiState.update { it.copy(userForm = it.userForm.copy(avatarUrl = newValue)) }
    }

    // Hàm gọi khi bấm "LƯU THAY ĐỔI"
    fun saveChanges() {
        val uid = userRepository.getCurrentUserUid() ?: return
        _uiState.update { it.copy(isLoading = true, isSaveSuccess = false) }

        viewModelScope.launch {
            val success = userRepository.saveUserProfile(uid, _uiState.value.userForm)
            if (success) {
                _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lưu thất bại. Vui lòng thử lại") }
            }
        }
    }
    fun resetSaveState() {
        _uiState.update { it.copy(isSaveSuccess = false, errorMessage = null) }
    }
}