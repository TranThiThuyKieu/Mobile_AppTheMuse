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
import kotlinx.coroutines.launch

sealed class ReadingState {
    object Loading : ReadingState()
    data class Success(
        val book: BookUi,
        val currentChapter: ChapterUi,
        val allChapters: List<ChapterUi>,
        val progressPercent: Int
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
                // Ưu tiên kiểm tra dữ liệu đã tải (offline)
                val localBook = downloadRepository.getBookById(bookId)
                val chapters = if (localBook != null) {
                    downloadRepository.getChapters(bookId)
                } else {
                    // Nếu chưa tải, lấy từ Firebase (sẽ ném lỗi nếu không có mạng)
                    bookRepository.getChapters(bookId)
                }

                val book = localBook ?: bookRepository.getBookById(bookId)
                val currentChapter = chapters.find { it.chapter_number == chapterNumber }
                    ?: chapters.firstOrNull()

                if (book != null && currentChapter != null) {
                    val progress = calculateProgress(chapterNumber, chapters.size)
                    _uiState.value = ReadingState.Success(
                        book = book.toBookUi(),
                        currentChapter = currentChapter.toChapterUi(),
                        allChapters = chapters.map { it.toChapterUi() },
                        progressPercent = progress
                    )
                    
                    // Cập nhật lượt xem và tiến độ lên Firebase trong background
                    // Dùng try-catch để không làm gián đoạn việc đọc khi offline
                    launch {
                        try {
                            bookRepository.incrementViewCount(bookId)
                            auth.currentUser?.uid?.let { userId ->
                                bookRepository.updateReadingProgress(userId, bookId, chapterNumber, 0)
                            }
                        } catch (e: Exception) {
                            android.util.Log.d("ReadingVM", "Skip Firebase update: offline mode")
                        }
                    }
                } else {
                    _uiState.value = ReadingState.Error("Không tìm thấy nội dung. Vui lòng kiểm tra kết nối internet.")
                }
            } catch (e: Exception) {
                val errorMsg = if (e.message?.contains("network", true) == true)
                    "Không có internet. Vui lòng kết nối để đọc truyện này."
                else "Lỗi tải nội dung: ${e.localizedMessage}"
                _uiState.value = ReadingState.Error(errorMsg)
            }
        }
    }

    private fun calculateProgress(current: Int, total: Int): Int {
        if (total == 0) return 0
        return ((current.toFloat() / total.toFloat()) * 100).toInt()
    }
}
