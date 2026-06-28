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
import com.example.appthemuse.ui.model.ReviewUi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BookDetailState {
    object Loading : BookDetailState()
    data class Success(
        val book: BookUi,
        val chapters: List<ChapterUi>,
        val reviews: List<ReviewUi> = emptyList(),
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
                // Kiểm tra xem truyện đã có trong máy chưa (hỗ trợ offline)
                val localBook = downloadRepository.getBookById(bookId)
                val isDownloaded = localBook != null
                
                // Lấy thông tin sách: Ưu tiên Firestore, nếu lỗi/mất mạng thì dùng bản local
                val book = try {
                    bookRepository.getBookById(bookId) ?: localBook
                } catch (e: Exception) {
                    localBook
                }

                if (book != null) {
                    // Lấy danh sách chương: tương tự sách
                    val chapters = try {
                        if (isDownloaded) downloadRepository.getChapters(bookId)
                        else bookRepository.getChapters(bookId)
                    } catch (e: Exception) {
                        if (isDownloaded) downloadRepository.getChapters(bookId)
                        else throw e
                    }

                    // Reviews và Favorite chỉ lấy được khi có mạng
                    val reviews = try {
                        bookRepository.getReviews(bookId).map {
                            ReviewUi(
                                id = it.id,
                                userId = it.user_id,
                                userName = it.user_name,
                                userAvatar = it.user_avatar,
                                rating = it.rating,
                                comment = it.comment,
                                createdAt = it.created_at
                            )
                        }
                    } catch (e: Exception) { emptyList<ReviewUi>() }

                    val userId = auth.currentUser?.uid
                    val isFavorite = if (userId != null) {
                        try { bookRepository.isBookFavorite(userId, bookId) } catch (e: Exception) { false }
                    } else false
                    
                    var lastChapter = 1
                    var isFinished = false
                    var progressPercent = 0

                    if (userId != null) {
                        try {
                            val progress = bookRepository.getReadingProgress(userId, bookId)
                            if (progress != null) {
                                lastChapter = progress.first
                                if (chapters.isNotEmpty()) {
                                    progressPercent = ((lastChapter.toFloat() / chapters.size.toFloat()) * 100).toInt()
                                    if (lastChapter >= chapters.size) isFinished = true
                                }
                            }
                        } catch (e: Exception) { }
                    }

                    _uiState.value = BookDetailState.Success(
                        book = book.toBookUi().copy(progressPercent = progressPercent),
                        chapters = chapters.map { it.toChapterUi() },
                        reviews = reviews,
                        isFavorite = isFavorite,
                        isDownloaded = isDownloaded,
                        lastReadChapterNumber = lastChapter,
                        isFinished = isFinished
                    )
                } else {
                    _uiState.value = BookDetailState.Error("Không có kết nối internet và truyện này chưa được tải.")
                }
            } catch (e: Exception) {
                _uiState.value = BookDetailState.Error("Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun addReview(bookId: String, rating: Int, comment: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                bookRepository.addReview(bookId, userId, rating, comment)
                loadBookDetail(bookId)
            } catch (e: Exception) { }
        }
    }

    fun toggleFavorite(bookId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is BookDetailState.Success) {
                    val oldStatus = currentState.isFavorite
                    val newStatus = !oldStatus

                    _uiState.value = currentState.copy(isFavorite = newStatus)
                    bookRepository.toggleFavorite(userId, bookId)
                }
            } catch (e: Exception) { }
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
            } catch (e: Exception) { }
        }
    }
}
