package com.example.appthemuse.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.repository.BookRepository
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.model.CategoryUi
import com.example.appthemuse.ui.mapper.toBookUi
import com.example.appthemuse.ui.mapper.toCategoryUi
import com.example.appthemuse.utils.NetworkUtils
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
    val searchResults: List<BookUi> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val isOnline: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(
    application: Application,
    private val bookRepository: BookRepository,
    private val favoriteGenres: List<String> = emptyList()
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init { loadHomeData() }

    private fun checkOnlineStatus(): Boolean {
        return NetworkUtils.isOnline(getApplication())
    }

    fun loadHomeData() {
        viewModelScope.launch {
            val online = checkOnlineStatus()
            _uiState.value = _uiState.value.copy(isLoading = true, isOnline = online)
            
            try {
                // Luôn thử tải dữ liệu. Firestore sẽ tự lấy từ Cache nếu Offline.
                val trending = bookRepository.getTrendingBooks().map { it.toBookUi() }.filter { it.status != "hidden" && it.status != "pending" }
                val recent = bookRepository.getRecentBooks().map { it.toBookUi() }.filter { it.status != "hidden" && it.status != "pending" }
                val recommended = bookRepository.getRecommendedBooks(favoriteGenres).map { it.toBookUi() }.filter { it.status != "hidden" && it.status != "pending" }
                val categories = bookRepository.getCategories().map { it.toCategoryUi() }
                val newRelease = bookRepository.getNewReleaseBooks().map { it.toBookUi() }.filter { it.status != "hidden" && it.status != "pending" }
                val allBooks = bookRepository.getAllBooks().map { it.toBookUi() }.filter { it.status != "hidden" && it.status != "pending" }

                _uiState.value = HomeUiState(
                    isLoading = false,
                    trendingBooks = trending,
                    recentBooks = recent,
                    recommendedBooks = recommended,
                    categories = categories,
                    newReleaseBooks = newRelease,
                    allBooks = allBooks,
                    isOnline = online
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isOnline = online,
                    errorMessage = if (!online) "Bạn đang ở chế độ ngoại tuyến. Một số dữ liệu có thể không hiển thị."
                                  else (e.localizedMessage ?: "Đã xảy ra lỗi khi tải dữ liệu.")
                )
            }
        }
    }

    fun searchBooks(keyword: String, filterType: String, status: String?, star: Int?) {
        val source = _uiState.value.allBooks
        if (source.isEmpty()) return
        var results = source
        if (keyword.isNotBlank()) {
            results = when (filterType) {
                "author" -> results.filter { it.author_name.contains(keyword, true) }
                else -> results.filter { it.title.contains(keyword, true) }
            }
        }
        status?.let { selectedStatus ->
            results = results.filter { it.status.equals(selectedStatus, true) }
        }
        star?.let { selectedStar ->
            results = results.filter { it.rating.toInt() >= selectedStar }
        }
        _uiState.value = _uiState.value.copy(searchResults = results)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
    }

    fun loadSearchHistory(userId: String) {
        viewModelScope.launch {
            val history = bookRepository.getSearchHistory(userId)
            _uiState.value = _uiState.value.copy(searchHistory = history)
        }
    }

    fun saveSearchHistory(userId: String, keyword: String) {
        viewModelScope.launch {
            bookRepository.saveSearchHistory(userId, keyword)
        }
    }
}
