package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.repository.BookRepository
import com.example.appthemuse.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp

data class CreateBookUiState(
    val title: String = "",
    val description: String = "",
    val coverImageUri: String? = null,
    val coverImageBase64: String? = null,
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class CreateBookViewModel(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateBookUiState())
    val uiState: StateFlow<CreateBookUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = bookRepository.getCategories()
                _uiState.update { it.copy(categories = categories) }
            } catch (e: Exception) {
                android.util.Log.e("CreateBookVM", "Error loading categories: ${e.message}")
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChange(desc: String) {
        _uiState.update { it.copy(description = desc) }
    }

    fun onImageSelected(uri: String, base64: String? = null) {
        _uiState.update { it.copy(coverImageUri = uri, coverImageBase64 = base64) }
    }

    fun toggleCategory(category: Category) {
        _uiState.update { state ->
            if (state.selectedCategory?.id == category.id) {
                state.copy(selectedCategory = null)
            } else {
                state.copy(selectedCategory = category)
            }
        }
    }

    fun saveDraft() {
        submitBook("Bản thảo")
    }

    fun publishBook() {
        submitBook("Đang cập nhật")
    }

    private fun submitBook(status: String) {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng nhập tên tác phẩm") }
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(error = "Bạn chưa đăng nhập") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val authorName = currentUser.displayName ?: "Ẩn danh"
                val book = Book(
                    title = currentState.title,
                    author_id = currentUser.uid,
                    author_name = authorName,
                    description = currentState.description,
                    category_id = currentState.selectedCategory?.name ?: "Khác",
                    status = status,
                    created_at = Timestamp.now()
                )

                bookRepository.createBook(book, currentState.coverImageBase64)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Có lỗi xảy ra") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetState() {
        _uiState.update {
            CreateBookUiState(categories = it.categories)
        }
    }
}
