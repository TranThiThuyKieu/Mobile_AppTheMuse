package com.example.appthemuse.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.repository.BookRepository
import com.example.appthemuse.domain.repository.DownloadRepository
import com.example.appthemuse.domain.repository.LibraryRepository
import com.example.appthemuse.ui.mapper.toBookUi
import com.example.appthemuse.ui.mapper.toChapterUi
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.model.ChapterUi
import com.example.appthemuse.ui.model.ReviewUi
import com.example.appthemuse.utils.NetworkUtils
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
        val isFavorite: Boolean = false,
        val isDownloaded: Boolean = false,
        val lastReadChapterNumber: Int = 1,
        val isFinished: Boolean = false,
        val isOnline: Boolean = true
    ) : BookDetailState()
    data class Error(val message: String) : BookDetailState()
}

class BookDetailViewModel(
    application: Application,
    private val bookRepository: BookRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<BookDetailState>(BookDetailState.Loading)
    val uiState: StateFlow<BookDetailState> = _uiState
    private val auth = FirebaseAuth.getInstance()

    private fun isOnline(): Boolean {
        return NetworkUtils.isOnline(getApplication())
    }

    fun loadBookDetail(bookId: String) {
        viewModelScope.launch {
            _uiState.value = BookDetailState.Loading
            
            val online = isOnline()
            val localBook = downloadRepository.getBookById(bookId)

            try {
                // Không chặn người dùng bằng `online`. Thử lấy data từ repo trước (Firestore tự cache).
                val book = try { bookRepository.getBookById(bookId) } catch (e: Exception) { null } ?: localBook
                
                if (book != null) {
                    val userId = auth.currentUser?.uid
                    
                    // Lấy danh sách chương: ưu tiên mạng, nếu lỗi thì lấy local
                    val chapters = try {
                        val remoteChapters = bookRepository.getChapters(bookId)
                        if (remoteChapters.isNotEmpty()) remoteChapters else downloadRepository.getChapters(bookId)
                    } catch (e: Exception) {
                        downloadRepository.getChapters(bookId)
                    }

                    val reviews = try { bookRepository.getReviews(bookId) } catch (e: Exception) { emptyList() }
                    val isFavorite = if (userId != null) {
                        try { bookRepository.isBookFavorite(userId, bookId) } catch (e: Exception) { false }
                    } else false
                    
                    var lastChapter = 1
                    var progressPercent = 0
                    var isFinished = false
                    if (userId != null) {
                        try {
                            bookRepository.getReadingProgress(userId, bookId)?.let {
                                lastChapter = it.first
                                if (chapters.isNotEmpty()) {
                                    progressPercent = ((lastChapter.toFloat() / chapters.size.toFloat()) * 100).toInt()
                                    if (lastChapter >= chapters.size) isFinished = true
                                }
                            }
                        } catch (e: Exception) {}
                    }

                    _uiState.value = BookDetailState.Success(
                        book = book.toBookUi().copy(progressPercent = progressPercent),
                        chapters = chapters.map { it.toChapterUi() },
                        reviews = reviews.map { ReviewUi(it.id, it.user_id, it.user_name, it.user_avatar, it.rating, it.comment, it.created_at) },
                        isFavorite = isFavorite,
                        isDownloaded = localBook != null,
                        lastReadChapterNumber = lastChapter,
                        isFinished = isFinished,
                        isOnline = online
                    )
                } else {
                    _uiState.value = BookDetailState.Error("Không tìm thấy dữ liệu. Vui lòng bật mạng để tải truyện.")
                }
            } catch (e: Exception) {
                _uiState.value = BookDetailState.Error("Lỗi tải dữ liệu: ${e.localizedMessage}")
            }
        }
    }

    fun addReview(bookId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            try {
                bookRepository.addReview(bookId, auth.currentUser?.uid ?: return@launch, rating, comment)
                loadBookDetail(bookId)
            } catch (e: Exception) {}
        }
    }

    fun toggleFavorite(bookId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value as? BookDetailState.Success ?: return@launch
            _uiState.value = currentState.copy(isFavorite = !currentState.isFavorite)
            try {
                bookRepository.toggleFavorite(auth.currentUser?.uid ?: return@launch, bookId)
            } catch (e: Exception) {}
        }
    }

    fun downloadBook(bookUi: BookUi) {
        viewModelScope.launch {
            try {
                val book = bookRepository.getBookById(bookUi.id) ?: return@launch
                downloadRepository.saveBook(book)
                downloadRepository.saveChapters(book.id, bookRepository.getChapters(book.id))
                (_uiState.value as? BookDetailState.Success)?.let { _uiState.value = it.copy(isDownloaded = true) }
            } catch (e: Exception) {}
        }
    }
}
