package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddChapterUiState(
    val title: String = "",
    val content: String = "",
    val wordCount: Int = 0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AddChapterViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddChapterUiState())
    val uiState: StateFlow<AddChapterUiState> = _uiState.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onContentChange(newContent: String) {
        val wordCount = if (newContent.isBlank()) 0
        else newContent.trim().split(Regex("\\s+")).size
        _uiState.update { it.copy(content = newContent, wordCount = wordCount) }
    }

    fun publishChapter(bookId: String) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng nhập tiêu đề chương") }
            return
        }
        if (state.content.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng nhập nội dung chương") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                bookRepository.createChapter(
                    bookId = bookId,
                    title = state.title.trim(),
                    content = state.content.trim()
                )
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Đăng chương thất bại: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetState() {
        _uiState.value = AddChapterUiState()
    }
}
