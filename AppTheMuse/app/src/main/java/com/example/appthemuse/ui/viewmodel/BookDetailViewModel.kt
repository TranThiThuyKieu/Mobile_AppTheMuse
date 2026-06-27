package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.repository.BookRepository
import com.example.appthemuse.domain.repository.DownloadRepository
import com.example.appthemuse.domain.repository.LibraryRepository
import com.example.appthemuse.ui.mapper.toBookUi
import com.example.appthemuse.ui.mapper.toChapterUi
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.model.ChapterUi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BookDetailState {
    object Loading : BookDetailState()
    data class Success(
        val book: BookUi,
        val chapters: List<ChapterUi>,
        val isFavorite: Boolean,
        val isDownloaded: Boolean,
        val lastReadChapterNumber: Int = 1,
        val isFinished: Boolean = false
    ) : BookDetailState()
    data class Error(val message: String) : BookDetailState()
}

class BookDetailViewModel(
    private val bookRepository: BookRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookDetailState>(BookDetailState.Loading)
    val uiState: StateFlow<BookDetailState> = _uiState
    private val auth = FirebaseAuth.getInstance()

    fun loadBookDetail(bookId: String) {
        viewModelScope.launch {
            _uiState.value = BookDetailState.Loading
            try {
                val book = bookRepository.getBookById(bookId)
                if (book != null) {
                    val chapters = bookRepository.getChapters(bookId)
                    val userId = auth.currentUser?.uid
                    
                    val isFavorite = if (userId != null) {
                        libraryRepository.isFavorite(userId, bookId)
                    } else false
                    
                    val isDownloaded = try {
                        downloadRepository.getBookById(bookId) != null
                    } catch (e: Exception) { false }
                    
                    var progressPercent = 0
                    var lastChapter = 1
                    var isFinished = false

                    if (userId != null) {
                        val progress = bookRepository.getReadingProgress(userId, bookId)
                        if (progress != null) {
                            lastChapter = progress.first
                            if (chapters.isNotEmpty()) {
                                progressPercent = ((lastChapter.toFloat() / chapters.size.toFloat()) * 100).toInt()
                                if (lastChapter >= chapters.size) isFinished = true
                            }
                        }
                    }

                    _uiState.value = BookDetailState.Success(
                        book = book.toBookUi().copy(progressPercent = progressPercent),
                        chapters = chapters.map { it.toChapterUi() },
                        isFavorite = isFavorite,
                        isDownloaded = isDownloaded,
                        lastReadChapterNumber = lastChapter,
                        isFinished = isFinished
                    )
                } else {
                    _uiState.value = BookDetailState.Error("Không tìm thấy thông tin tác phẩm này.")
                }
            } catch (e: Exception) {
                _uiState.value = BookDetailState.Error("Lỗi tải dữ liệu: ${e.localizedMessage}")
            }
        }
    }

    fun toggleFavorite(bookId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is BookDetailState.Success) {
                    val newStatus = !currentState.isFavorite
                    if (newStatus) {
                        libraryRepository.addFavorite(userId, bookId)
                    } else {
                        libraryRepository.removeFavorite(userId, bookId)
                    }
                    _uiState.value = currentState.copy(isFavorite = newStatus)
                }
            } catch (e: Exception) {
                android.util.Log.e("BookDetailVM", "Toggle Favorite Error: ${e.message}")
            }
        }
    }

    fun downloadBook(bookUi: BookUi) {
        viewModelScope.launch {
            try {
                val book = bookRepository.getBookById(bookUi.id) ?: return@launch
                val chapters = bookRepository.getChapters(book.id)
                downloadRepository.saveBook(book)
                downloadRepository.saveChapters(book.id, chapters)
                val currentState = _uiState.value
                if (currentState is BookDetailState.Success) {
                    _uiState.value = currentState.copy(isDownloaded = true)
                }
            } catch (e: Exception) {
                android.util.Log.e("BookDetailVM", "Download Error", e)
            }
        }
    }
}