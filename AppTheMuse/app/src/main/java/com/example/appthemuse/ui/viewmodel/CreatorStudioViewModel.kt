package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.model.Book
import com.example.appthemuse.domain.repository.BookRepository
import com.example.appthemuse.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class CreatorStudioUiState(
    val isLoading: Boolean = true,
    val publishedBooks: List<Book> = emptyList(),
    val drafts: List<Book> = emptyList(),
    val totalReadsStr: String = "0",
    val totalFollowersStr: String = "0",
    val totalChaptersStr: String = "0",
    val error: String? = null
)

class CreatorStudioViewModel(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatorStudioUiState())
    val uiState: StateFlow<CreatorStudioUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Bạn chưa đăng nhập.") }
                    return@launch
                }

                // 1. Lấy thông tin user để có số follower (Giả định sau này có trường followerCount, tạm thời dùng 0 hoặc thêm logic vào hàm)
                // Cập nhật sau: val user = userRepository.getUserProfile()
                val totalFollowers = 0 // Tạm thời để 0 hoặc lấy từ user document nếu có

                // 2. Lấy toàn bộ sách do tác giả sáng tác
                val authorBooks = bookRepository.getBooksByAuthor(userId)

                // 3. Phân loại tác phẩm đã xuất bản và bản thảo (Dựa vào status)
                // Dựa trên giả định: "Bản thảo" hoặc "Draft" là bản nháp, còn lại là đã xuất bản
                val publishedBooks = authorBooks.filter {
                    it.status.lowercase(Locale.getDefault()) != "bản thảo" &&
                            it.status.lowercase(Locale.getDefault()) != "draft"
                }
                val drafts = authorBooks.filter {
                    it.status.lowercase(Locale.getDefault()) == "bản thảo" ||
                            it.status.lowercase(Locale.getDefault()) == "draft"
                }

                // 4. Tính toán thống kê
                val totalReads = authorBooks.sumOf { it.view_count }
                val totalChapters = authorBooks.sumOf { it.chapter_count }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        publishedBooks = publishedBooks,
                        drafts = drafts,
                        totalReadsStr = formatNumber(totalReads),
                        totalChaptersStr = formatNumber(totalChapters.toLong()),
                        totalFollowersStr = formatNumber(totalFollowers.toLong())
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Đã xảy ra lỗi.") }
            }
        }
    }

    private fun formatNumber(number: Long): String {
        if (number < 1000) return number.toString()
        val formatted = number / 1000.0
        val suffix = if (number >= 1000000) "M" else "K"
        val value = if (number >= 1000000) number / 1000000.0 else formatted
        return String.format(Locale.US, "%.1f%s", value, suffix).replace(".0", "")
    }
}