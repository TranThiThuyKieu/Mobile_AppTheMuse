package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.repository.BookRepository
import com.example.appthemuse.domain.repository.DownloadRepository
import com.example.appthemuse.ui.mapper.toBookUi
import com.example.appthemuse.ui.mapper.toChapterUi
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.model.ChapterUi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ReadingState {
    object Loading : ReadingState()
    data class Success(
        val book: BookUi,
        val currentChapter: ChapterUi,
        val allChapters: List<ChapterUi>,
        val progressPercent: Int,
        val isBookmarked: Boolean = false
    ) : ReadingState()
    data class Error(val message: String) : ReadingState()
}

class ReadingViewModel(
    private val bookRepository: BookRepository,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReadingState>(ReadingState.Loading)
    val uiState: StateFlow<ReadingState> = _uiState
    private val auth = FirebaseAuth.getInstance()

    fun loadChapter(bookId: String, chapterNumber: Int) {
        viewModelScope.launch {
            _uiState.value = ReadingState.Loading
            try {
                // Tăng lượt xem (chỉ khi online)
                try { bookRepository.incrementViewCount(bookId) } catch (e: Exception) {}

                // 1. Ưu tiên lấy từ Local (Offline)
                val localChapters = downloadRepository.getChapters(bookId)
                val chapters = if (localChapters.isNotEmpty()) {
                    localChapters
                } else {
                    bookRepository.getChapters(bookId)
                }

                val localBook = downloadRepository.getBookById(bookId)
                val book = localBook ?: bookRepository.getBookById(bookId)
                
                val currentChapter = chapters.find { it.chapter_number == chapterNumber }
                    ?: chapters.firstOrNull()

                if (book != null && currentChapter != null) {
                    val progress = calculateProgress(chapterNumber, chapters.size)
                    val userId = auth.currentUser?.uid
                    val isBookmarked = if (userId != null) {
                        bookRepository.isBookmarked(userId, bookId, chapterNumber)
                    } else false

                    _uiState.value = ReadingState.Success(
                        book = book.toBookUi(),
                        currentChapter = currentChapter.toChapterUi(),
                        allChapters = chapters.map { it.toChapterUi() },
                        progressPercent = progress,
                        isBookmarked = isBookmarked
                    )
                    
                    // Lưu tiến độ đọc (Lịch sử)
                    userId?.let { uid ->
                        bookRepository.updateReadingProgress(uid, bookId, chapterNumber, 0)
                    }
                } else {
                    _uiState.value = ReadingState.Error("Không tìm thấy nội dung")
                }
            } catch (e: Exception) {
                _uiState.value = ReadingState.Error(e.localizedMessage ?: "Lỗi tải chương")
            }
        }
    }

    fun toggleBookmark() {
        val state = _uiState.value
        if (state is ReadingState.Success) {
            val userId = auth.currentUser?.uid ?: return
            viewModelScope.launch {
                try {
                    val newBookmarkStatus = !state.isBookmarked
                    if (newBookmarkStatus) {
                        bookRepository.addBookmark(userId, state.book.id, state.currentChapter.chapter_number)
                    } else {
                        bookRepository.removeBookmark(userId, state.book.id, state.currentChapter.chapter_number)
                    }
                    _uiState.update { 
                        if (it is ReadingState.Success) it.copy(isBookmarked = newBookmarkStatus) else it
                    }
                } catch (e: Exception) {}
            }
        }
    }

    private fun calculateProgress(current: Int, total: Int): Int {
        if (total == 0) return 0
        return ((current.toFloat() / total.toFloat()) * 100).toInt()
    }
}