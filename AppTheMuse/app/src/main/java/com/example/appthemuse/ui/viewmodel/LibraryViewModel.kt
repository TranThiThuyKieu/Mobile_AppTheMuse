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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class LibraryUiState(
    val favoriteBooks: List<BookUi> = emptyList(),
    val historyBooks: List<HistoryUi> = emptyList(),
    val downloadedBooks: List<BookUi> = emptyList(),
    val booksWithUpdates: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val isUpdating: Map<String, Boolean> = emptyMap(),
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            val books = repository.getFavoriteBooks(userId).map { it.toBookUi() }
            _uiState.value = _uiState.value.copy(favoriteBooks = books, isLoading = false)
        }
    }
    
    fun loadHistoryBooks(userId: String) {
        viewModelScope.launch {
            val books = repository.getHistoryBooks(userId)
            _uiState.value = _uiState.value.copy(historyBooks = books)
        }
    }
    
    fun loadDownloadedBooks() {
        viewModelScope.launch {
            // 1. Hiển thị sách từ Room ngay lập tức (Cực nhanh, không cần mạng)
            val localBooks = downloadRepository.getDownloadedBooks()
            val bookUis = localBooks.map { it.toBookUi() }
            _uiState.value = _uiState.value.copy(downloadedBooks = bookUis)
            
            // 2. Kiểm tra cập nhật âm thầm trong background (Chỉ thực hiện nếu có mạng)
            launch {
                localBooks.forEach { book ->
                    try {
                        val remoteBook = withTimeoutOrNull(2000) { 
                            bookRepository.getBookById(book.id) 
                        }
                        if (remoteBook != null && remoteBook.chapter_count > book.chapter_count) {
                            _uiState.value = _uiState.value.copy(
                                booksWithUpdates = _uiState.value.booksWithUpdates + (book.id to remoteBook.chapter_count)
                            )
                        }
                    } catch (e: Exception) { }
                }
            }
        }
    }

    fun updateBookChapters(bookId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdating = _uiState.value.isUpdating + (bookId to true)
            )
            try {
                val remoteBook = bookRepository.getBookById(bookId)
                if (remoteBook != null) {
                    val remoteChapters = bookRepository.getChapters(bookId)
                    val localChapters = downloadRepository.getChapters(bookId)
                    val localNumbers = localChapters.map { it.chapter_number }.toSet()
                    
                    val newChapters = remoteChapters.filter { it.chapter_number !in localNumbers }
                    if (newChapters.isNotEmpty()) {
                        downloadRepository.saveChapters(bookId, newChapters)
                    }
                    downloadRepository.saveBook(remoteBook)
                    loadDownloadedBooks()
                }
            } catch (e: Exception) { } finally {
                _uiState.value = _uiState.value.copy(
                    isUpdating = _uiState.value.isUpdating - bookId
                )
            }
        }
    }

    fun insertBook() {
        viewModelScope.launch {
            loadDownloadedBooks()
        }
    }
}
