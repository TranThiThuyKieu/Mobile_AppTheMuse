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

            val books =
                repository.getHistoryBooks(userId)

            _uiState.value =
                _uiState.value.copy(
                    historyBooks = books
                )
        }
    }
    // Hàm load sách đã tải
    fun loadDownloadedBooks() {
        viewModelScope.launch {
            val books = downloadRepository.getDownloadedBooks()
            _uiState.value = _uiState.value.copy(downloadedBooks = books.map {
                            it.toBookUi()
                        }
                )
        }
    }
    // Hàm tải sách về
    fun insertBook() {
        viewModelScope.launch {
            // Dữ liệu tạm để test, chạy lần 1 thì sài, chạy lần 2 trở đi thì nhớ comment lại, sau khi làm tải sách vế thì thay
//            val ids = listOf("book1", "book2", "book3")
//            ids.forEach { id ->
//                val book = bookRepository.getBookById(id)
//                if (book != null) {
//                    downloadRepository.saveBook(book)
//                }
//            }
            loadDownloadedBooks()
        }
    }
}