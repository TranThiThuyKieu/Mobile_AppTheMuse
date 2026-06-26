package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.repository.LibraryRepository
import com.example.appthemuse.ui.mapper.toBookUi
import com.example.appthemuse.ui.model.BookUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val favoriteBooks: List<BookUi> = emptyList(),
    val historyBooks: List<BookUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LibraryViewModel(
    private val repository: LibraryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState
    // hàm load sách yêu thích
    fun loadFavoriteBooks(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val books = repository.getFavoriteBooks(userId).map {
                it.toBookUi()
            }
            _uiState.value = LibraryUiState(favoriteBooks = books, isLoading = false)
        }
    }
    // hàm load lịch sử sách đã đọc
    fun loadHistoryBooks(userId: String) {
        viewModelScope.launch {
            val books = repository.getHistoryBooks(userId).map {
                        it.toBookUi()
                    }
            _uiState.value = _uiState.value.copy(historyBooks = books)
        }
    }
}