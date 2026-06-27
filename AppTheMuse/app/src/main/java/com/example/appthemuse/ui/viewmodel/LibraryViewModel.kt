package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.model.Book
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

    // hàm load sách yêu thích
    fun loadFavoriteBooks(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val books = repository.getFavoriteBooks(userId).map {
                    it.toBookUi()
                }
                _uiState.update { it.copy(favoriteBooks = books, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // hàm load lịch sử sách đã đọc
    fun loadHistoryBooks(userId: String) {
        viewModelScope.launch {
            try {
                val books = repository.getHistoryBooks(userId)
                _uiState.update { it.copy(historyBooks = books) }
            } catch (e: Exception) {
                android.util.Log.e("LibraryVM", "Error loading history: ${e.message}")
            }
        }
    }

    // Hàm load sách đã tải và kiểm tra cập nhật
    fun loadDownloadedBooks() {
        viewModelScope.launch {
            try {
                val localBooks = downloadRepository.getDownloadedBooks()
                val booksWithUpdateStatus = localBooks.map { book ->
                    val bookUi = book.toBookUi()
                    // Kiểm tra xem có chương mới trên server không
                    val remoteBook = try { bookRepository.getBookById(book.id) } catch (e: Exception) { null }
                    if (remoteBook != null && remoteBook.chapter_count > book.chapter_count) {
                        bookUi.copy(hasUpdate = true)
                    } else {
                        bookUi
                    }
                }
                _uiState.update { it.copy(downloadedBooks = booksWithUpdateStatus) }
            } catch (e: Exception) {
                android.util.Log.e("LibraryVM", "Error loading downloads: ${e.message}")
            }
        }
    }

    // Hàm tải bản cập nhật mới nhất
    fun updateBook(bookId: String) {
        viewModelScope.launch {
            try {
                val remoteBook = bookRepository.getBookById(bookId)
                if (remoteBook != null) {
                    val chapters = bookRepository.getChapters(bookId)
                    downloadRepository.saveBook(remoteBook)
                    downloadRepository.saveChapters(bookId, chapters)
                    loadDownloadedBooks() // Refresh list
                }
            } catch (e: Exception) {
                android.util.Log.e("LibraryVM", "Update error: ${e.message}")
            }
        }
    }

    fun insertBook() {
        viewModelScope.launch {
            loadDownloadedBooks()
        }
    }
}