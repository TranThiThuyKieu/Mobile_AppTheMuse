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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

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
    private val bookRepository: BookRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookDetailState>(BookDetailState.Loading)
    val uiState: StateFlow<BookDetailState> = _uiState
    private val auth = FirebaseAuth.getInstance()

    private suspend fun isOnline(): Boolean {
        return try {
            val result = withTimeoutOrNull(2000) {
                FirebaseFirestore.getInstance().collection(".info").document("connected").get().await()
            }
            result?.getBoolean("connected") == true
        } catch (e: Exception) { false }
    }

    fun loadBookDetail(bookId: String) {
        viewModelScope.launch {
            _uiState.value = BookDetailState.Loading
            
            val online = isOnline()
            val localBook = downloadRepository.getBookById(bookId)
            
            if (!online && localBook == null) {
                _uiState.value = BookDetailState.Error("Truyện chưa được tải về. Vui lòng bật mạng để xem.")
                return@launch
            }

            try {
                val book = (if (online) bookRepository.getBookById(bookId) else null) ?: localBook
                if (book != null) {
                    val userId = auth.currentUser?.uid
                    val chapters = if (online) bookRepository.getChapters(bookId) else downloadRepository.getChapters(bookId)
                    val reviews = if (online) bookRepository.getReviews(bookId) else emptyList()
                    val isFavorite = if (online && userId != null) bookRepository.isBookFavorite(userId, bookId) else false
                    
                    var lastChapter = 1
                    var progressPercent = 0
                    var isFinished = false
                    if (userId != null && online) {
                        bookRepository.getReadingProgress(userId, bookId)?.let {
                            lastChapter = it.first
                            if (chapters.isNotEmpty()) {
                                progressPercent = ((lastChapter.toFloat() / chapters.size.toFloat()) * 100).toInt()
                                if (lastChapter >= chapters.size) isFinished = true
                            }
                        }
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
                    _uiState.value = BookDetailState.Error("Không tìm thấy dữ liệu.")
                }
            } catch (e: Exception) {
                _uiState.value = BookDetailState.Error("Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun addReview(bookId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            if (!isOnline()) return@launch
            bookRepository.addReview(bookId, auth.currentUser?.uid ?: return@launch, rating, comment)
            loadBookDetail(bookId)
        }
    }

    fun toggleFavorite(bookId: String) {
        viewModelScope.launch {
            if (!isOnline()) return@launch
            val currentState = _uiState.value as? BookDetailState.Success ?: return@launch
            _uiState.value = currentState.copy(isFavorite = !currentState.isFavorite)
            bookRepository.toggleFavorite(auth.currentUser?.uid ?: return@launch, bookId)
        }
    }

    fun downloadBook(bookUi: BookUi) {
        viewModelScope.launch {
            if (!isOnline()) return@launch
            val book = bookRepository.getBookById(bookUi.id) ?: return@launch
            downloadRepository.saveBook(book)
            downloadRepository.saveChapters(book.id, bookRepository.getChapters(book.id))
            (_uiState.value as? BookDetailState.Success)?.let { _uiState.value = it.copy(isDownloaded = true) }
        }
    }
}
