package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.repository.BookRepository
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.model.CategoryUi
import com.example.appthemuse.ui.mapper.toBookUi
import com.example.appthemuse.ui.mapper.toCategoryUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val trendingBooks: List<BookUi> = emptyList(),
    val recommendedBooks: List<BookUi> = emptyList(),
    val recentBooks: List<BookUi> = emptyList(),
    val categories: List<CategoryUi> = emptyList(),
    val newReleaseBooks: List<BookUi> = emptyList(),
    val allBooks: List<BookUi> = emptyList(),
    val searchResults:List<BookUi> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val bookRepository: BookRepository,
    private val favoriteGenres: List<String> = emptyList()
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init { loadHomeData() }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val trending = bookRepository.getTrendingBooks().map { it.toBookUi() }
                val recent = bookRepository.getRecentBooks().map { it.toBookUi() }
                val recommended = bookRepository.getRecommendedBooks(favoriteGenres).map { it.toBookUi() }
                val categories = bookRepository.getCategories().map { it.toCategoryUi() }
                val newRelease = bookRepository.getNewReleaseBooks().map { it.toBookUi() }
                val allBooks = bookRepository.getAllBooks().map { it.toBookUi() }

                _uiState.value = HomeUiState(
                    isLoading = false,
                    trendingBooks = trending,
                    recentBooks = recent,
                    recommendedBooks = recommended,
                    categories = categories,
                    newReleaseBooks = newRelease,
                    allBooks = allBooks
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Đã xảy ra lỗi khi tải dữ liệu."
                )
            }
        }
    }
    // hàm tìm kiếm
    fun searchBooks(keyword: String, filterType: String, status: String?, star: Int?) {
        // lấy toàn bộ sách đã load từ Firestore về trước đó
        val source = _uiState.value.allBooks
        // nếu chưa có data thì không search
        if (source.isEmpty()) return
        // bắt đầu từ full list
        var results = source
        // filter theo keyword
        if (keyword.isNotBlank()) {
            results = when (filterType) {
                // search theo tác giả
                "author" -> results.filter {
                    it.author_name.contains(keyword, true)
                }
                // search theo title
                else -> results.filter {
                    it.title.contains(keyword, true)
                }
            }
        }
        // filter theo status
        status?.let { selectedStatus ->
            results = results.filter { book ->
                book.status.equals(selectedStatus, true)
            }
        }
        // filter theo rating
        star?.let { selectedStar ->
            results = results.filter { book ->
                book.rating.toInt() >= selectedStar
            }
        }
        _uiState.value = _uiState.value.copy(searchResults = results)
    }
    // hàm clear search result
    fun clearSearch(){
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
    }
    // hàm gọi lịch sử tìm kiếm khi mở màn hình search
    fun loadSearchHistory(userId: String) {
        viewModelScope.launch {
            val history = bookRepository.getSearchHistory(userId)
            _uiState.value = _uiState.value.copy(searchHistory = history)
        }
    }
    // hàm lưu lịch sử tìm kiếm khi user tìm kiếm
    fun saveSearchHistory(userId: String, keyword: String) {
        viewModelScope.launch {
            bookRepository.saveSearchHistory(userId, keyword)
        }
    }
}