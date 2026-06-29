package com.example.appthemuse.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.repository.BookRepository
import com.example.appthemuse.domain.repository.DownloadRepository
import com.example.appthemuse.ui.mapper.toBookUi
import com.example.appthemuse.ui.mapper.toChapterUi
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.model.ChapterUi
import com.example.appthemuse.utils.NetworkUtils
import com.example.appthemuse.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReadingState {
    object Loading : ReadingState()
    data class Success(
        val book: BookUi,
        val currentChapter: ChapterUi,
        val allChapters: List<ChapterUi>,
        val progressPercent: Int,
        val isOnline: Boolean = true
    ) : ReadingState()
    data class Error(val message: String) : ReadingState()
}

class ReadingViewModel(
    application: Application,
    private val bookRepository: BookRepository,
    private val downloadRepository: DownloadRepository,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ReadingState>(ReadingState.Loading)
    val uiState: StateFlow<ReadingState> = _uiState

    private fun isOnline(): Boolean {
        return NetworkUtils.isOnline(getApplication())
    }

    fun loadChapter(bookId: String, chapterNumber: Int) {
        viewModelScope.launch {
            _uiState.value = ReadingState.Loading
            
            val online = isOnline()
            val localBook = downloadRepository.getBookById(bookId)
            val isDownloaded = localBook != null

            if (!online && !isDownloaded) {
                _uiState.value = ReadingState.Error("Truyện này chưa được tải về. Vui lòng bật mạng để tiếp tục.")
                return@launch
            }

            try {
                if (isDownloaded) { 
                    val localChapters = downloadRepository.getChapters(bookId)
                    val currentChapter = localChapters.find { it.chapter_number == chapterNumber }
                        ?: localChapters.firstOrNull()

                    if (currentChapter != null) {
                        _uiState.value = ReadingState.Success(
                            book = localBook.toBookUi(),
                            currentChapter = currentChapter.toChapterUi(),
                            allChapters = localChapters.map { it.toChapterUi() },
                            progressPercent = calculateProgress(chapterNumber, localChapters.size),
                            isOnline = online
                        )
                        if (online) updateProgressOnServer(bookId, chapterNumber)
                        return@launch
                    }
                }

                if (online) {
                    val remoteBook = bookRepository.getBookById(bookId)
                    val remoteChapters = bookRepository.getChapters(bookId)
                    val currentChapter = remoteChapters.find { it.chapter_number == chapterNumber }
                        ?: remoteChapters.firstOrNull()

                    if (remoteBook != null && currentChapter != null) {
                        _uiState.value = ReadingState.Success(
                            book = remoteBook.toBookUi(),
                            currentChapter = currentChapter.toChapterUi(),
                            allChapters = remoteChapters.map { it.toChapterUi() },
                            progressPercent = calculateProgress(chapterNumber, remoteChapters.size),
                            isOnline = online
                        )
                        updateProgressOnServer(bookId, chapterNumber)
                        try { bookRepository.incrementViewCount(bookId) } catch (e: Exception) {}
                    } else {
                        _uiState.value = ReadingState.Error("Nội dung không khả dụng.")
                    }
                } else {
                    _uiState.value = ReadingState.Error("Không có kết nối mạng.")
                }
            } catch (e: Exception) {
                _uiState.value = ReadingState.Error("Lỗi: ${e.localizedMessage}")
            }
        }
    }

    private fun updateProgressOnServer(bookId: String, chapterNumber: Int) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            try {
                bookRepository.updateReadingProgress(userId, bookId, chapterNumber, 0)
            } catch (e: Exception) { }
        }
    }

    private fun calculateProgress(current: Int, total: Int): Int {
        if (total == 0) return 0
        return ((current.toFloat() / total.toFloat()) * 100).toInt()
    }
}
