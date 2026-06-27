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
import kotlinx.coroutines.launch

data class LibraryUiState(
    val favoriteBooks: List<BookUi> = emptyList(),
    val historyBooks: List<HistoryUi> = emptyList(),
    val downloadedBooks: List<BookUi> = emptyList(),
    val booksWithUpdates: Map<String, Int> = emptyMap(), // bookId -> newChapterCount
    val isLoading: Boolean = false,
    val isUpdating: Map<String, Boolean> = emptyMap(), // bookId -> isUpdating
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            val books = repository.getFavoriteBooks(userId).map {
                it.toBookUi()
            }
            _uiState.value = _uiState.value.copy(favoriteBooks = books, isLoading = false)
        }
    }
    
    // hàm load lịch sử sách đã đọc
    fun loadHistoryBooks(userId: String) {
        viewModelScope.launch {
            val books = repository.getHistoryBooks(userId)
            _uiState.value = _uiState.value.copy(historyBooks = books)
        }
    }
    
    // Hàm load sách đã tải và kiểm tra cập nhật
    fun loadDownloadedBooks() {
        viewModelScope.launch {
            val localBooks = downloadRepository.getDownloadedBooks()
            val bookUis = localBooks.map { it.toBookUi() }
            _uiState.value = _uiState.value.copy(downloadedBooks = bookUis)
            
            // Kiểm tra cập nhật khi có mạng
            val updatesMap = mutableMapOf<String, Int>()
            bookUis.forEach { book ->
                try {
                    val remoteBook = bookRepository.getBookById(book.id)
                    if (remoteBook != null && remoteBook.chapter_count > book.chapter_count) {
                        updatesMap[book.id] = remoteBook.chapter_count
                    }
                } catch (e: Exception) {
                    // Có thể đang offline, bỏ qua kiểm tra cập nhật
                }
            }
            _uiState.value = _uiState.value.copy(booksWithUpdates = updatesMap)
        }
    }

    // Hàm tải thêm chương mới
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
                    // Cập nhật thông tin sách (bao gồm số chương mới)
                    downloadRepository.saveBook(remoteBook)
                    loadDownloadedBooks()
                }
            } catch (e: Exception) {
                // Xử lý lỗi nếu cần
            } finally {
                _uiState.value = _uiState.value.copy(
                    isUpdating = _uiState.value.isUpdating - bookId
                )
            }
        }
    }

    // Hàm tải sách về (giữ lại logic cũ của bạn)
    fun insertBook() {
        viewModelScope.launch {
            loadDownloadedBooks()
        }
    }
}
