package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.data.model.BookUi
import com.example.appthemuse.data.remote.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val trendingBooks: List<BookUi> = emptyList(),
    val recommendedBooks: List<BookUi> = emptyList(),
    val recentBooks: List<BookUi> = emptyList(),
    val categories: List<String> = emptyList(),
    val newReleaseBooks: List<BookUi> = emptyList(),
    val errorMessage: String? = null
)
class HomeViewModel(
    private val firestoreService: FirestoreService,
    private val favoriteGenres: List<String> = emptyList()
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState
    init { loadHomeData() }
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val trending = firestoreService.getTrendingBooks()
                val recent = firestoreService.getRecentBooks()
                val recommended = firestoreService.getRecommendedBooks(favoriteGenres)
                val categories = firestoreService.getCategoriesList()
                val newRelease = firestoreService.getNewReleaseBooks()
                _uiState.value = HomeUiState(
                    isLoading = false,
                    trendingBooks = trending,
                    recentBooks = recent,
                    recommendedBooks = recommended ,
                    categories = categories,
                    newReleaseBooks = newRelease)
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Đã xảy ra lỗi khi tải dữ liệu."
                )
            }
        }
    }
}