package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.model.Chapter
import com.example.appthemuse.domain.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class CreatorBookDetailUiState(
    val isLoading: Boolean = true,
    val book: Book? = null,
    val chapters: List<Chapter> = emptyList(),
    val totalReadsStr: String = "0",
    val totalVotesStr: String = "0",
    val totalCommentsStr: String = "0",
    val categoryName: String = "Chưa phân loại",
    val error: String? = null
)

class CreatorBookDetailViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatorBookDetailUiState())
    val uiState: StateFlow<CreatorBookDetailUiState> = _uiState.asStateFlow()

    fun loadBookDetails(bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val book = bookRepository.getBookById(bookId)
                if (book == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy tác phẩm") }
                    return@launch
                }

                val chapters = bookRepository.getChapters(bookId)
                val voteCount = bookRepository.getVoteCount(bookId)
                val commentCount = bookRepository.getCommentCount(bookId)
                
                val categories = bookRepository.getCategories()
                val categoryName = categories.find { it.id == book.category_id }?.name ?: "Chưa phân loại"

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        book = book,
                        chapters = chapters,
                        totalReadsStr = formatNumber(book.view_count),
                        totalVotesStr = formatNumber(voteCount.toLong()),
                        totalCommentsStr = formatNumber(commentCount.toLong()),
                        categoryName = categoryName
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Đã xảy ra lỗi") }
            }
        }
    }

    private fun formatNumber(number: Long): String {
        if (number < 1000) return number.toString()
        val formatted = number / 1000.0
        val suffix = if (number >= 1000000) "M" else "k"
        val value = if (number >= 1000000) number / 1000000.0 else formatted
        return String.format(Locale.US, "%.1f%s", value, suffix).replace(".0", "")
    }

    fun updateBookStatus(bookId: String, status: String) {
        viewModelScope.launch {
            try {
                bookRepository.updateBookStatus(bookId, status)
                // Reload the book details to reflect the updated status
                loadBookDetails(bookId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Không thể cập nhật trạng thái: ${e.message}") }
            }
        }
    }
}
