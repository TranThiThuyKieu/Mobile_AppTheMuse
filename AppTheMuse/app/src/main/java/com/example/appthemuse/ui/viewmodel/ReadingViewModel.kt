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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    private val bookRepository: BookRepository,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReadingState>(ReadingState.Loading)
    val uiState: StateFlow<ReadingState> = _uiState
    private val auth = FirebaseAuth.getInstance()

    // Kiểm tra trạng thái kết nối thực tế với Firebase
    private suspend fun isOnline(): Boolean {
        return try {
            val result = FirebaseFirestore.getInstance().collection(".info").document("connected").get().await()
            result.getBoolean("connected") == true
        } catch (e: Exception) {
            false
        }
    }

    fun loadChapter(bookId: String, chapterNumber: Int) {
        viewModelScope.launch {
            _uiState.value = ReadingState.Loading
            
            val online = isOnline()
            val localBook = downloadRepository.getBookById(bookId)
            val isDownloaded = localBook != null

            // NẾU: Không có mạng VÀ truyện chưa được tải về máy -> KHÔNG CHO ĐỌC
            if (!online && !isDownloaded) {
                _uiState.value = ReadingState.Error("Truyện này chưa được tải về. Vui lòng bật mạng để tiếp tục.")
                return@launch
            }

            try {
                if (isDownloaded && (!online || online)) { 
                    // Ưu tiên lấy từ máy nếu đã tải (dù có mạng hay không để đảm bảo tốc độ)
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

                // Nếu chưa tải và đang Online -> Lấy từ Firebase
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
            } catch (e: Exception) {
                _uiState.value = ReadingState.Error("Lỗi: ${e.localizedMessage}")
            }
        }
    }

    private fun updateProgressOnServer(bookId: String, chapterNumber: Int) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
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
