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
}