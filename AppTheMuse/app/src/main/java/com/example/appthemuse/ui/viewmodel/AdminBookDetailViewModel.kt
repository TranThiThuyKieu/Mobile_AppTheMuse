package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.data.repository.AdminBookRepositoryImpl
import com.example.appthemuse.domain.repository.AdminBookRepository
import com.example.appthemuse.ui.mapper.toUi
import com.example.appthemuse.ui.model.AdminBookUi
import com.example.appthemuse.ui.model.AdminChapterUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminBookDetailUiState(
    val isLoading: Boolean = false,
    val book: AdminBookUi? = null,
    val chapters: List<AdminChapterUi> = emptyList(),
    val errorMessage: String? = null
)

class AdminBookDetailViewModel(
    private val repository: AdminBookRepository = AdminBookRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminBookDetailUiState())
    val uiState: StateFlow<AdminBookDetailUiState> = _uiState.asStateFlow()

    fun load(bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                repository.getBookDetail(bookId)
            }.onSuccess { detail ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        book = detail.book.toUi(),
                        chapters = detail.chapters.map { chapter -> chapter.toUi() }
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Khong tai duoc chi tiet")
                }
            }
        }
    }
}
