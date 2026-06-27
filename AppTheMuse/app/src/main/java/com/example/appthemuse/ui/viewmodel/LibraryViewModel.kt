package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.repository.BookRepository
import com.example.appthemuse.domain.repository.DownloadRepository
import com.example.appthemuse.domain.repository.LibraryRepository
import com.example.appthemuse.ui.mapper.toBookUi
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.model.HistoryUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val favoriteBooks: List<BookUi> = emptyList(),
    val historyBooks: List<HistoryUi> = emptyList(),
    val downloadedBooks: List<BookUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LibraryViewModel(
    private val repository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val bookRepository: BookRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState

    fun loadFavoriteBooks(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val books = repository.getFavoriteBooks(userId).map { it.toBookUi() }
                _uiState.update { it.copy(favoriteBooks = books, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadHistoryBooks(userId: String) {
        viewModelScope.launch {
            try {
                val books = repository.getHistoryBooks(userId)
                _uiState.update { it.copy(historyBooks = books) }
            } catch (e: Exception) { }
        }
    }

    fun loadDownloadedBooks() {
        viewModelScope.launch {
            try {
                val books = downloadRepository.getDownloadedBooks().map { it.toBookUi() }
                _uiState.update { it.copy(downloadedBooks = books) }
            } catch (e: Exception) { }
        }
    }

    fun insertBook() {
        viewModelScope.launch {
            loadDownloadedBooks()
        }
    }
}
