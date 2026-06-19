package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.BookModel
import com.example.appthemuse.domain.model.CategoryModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val trendingBooks: List<BookModel> = emptyList(),
    val recentBooks: List<BookModel> = emptyList(),
    val recommendedBooks: List<BookModel> = emptyList(),
    val categories: List<CategoryModel> = emptyList(),
    val newReleaseBooks: List<BookModel> = emptyList(),
    val allBooks: List<BookModel> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val firestoreService = FirestoreService()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // 1. Lấy dữ liệu DTO từ service từ xa
                val trendingDto = firestoreService.getTrendingBooks()
                val recentDto = firestoreService.getRecentBooks()
                val recommendedDto = firestoreService.getRecommendedBooks()
                val categoriesDto = firestoreService.getCategoriesList()
                val newReleaseDto = firestoreService.getNewReleaseBooks()
                val allBooksDto = firestoreService.getAllBooks()

                // 2. Chuyển đổi (Map) mảng DTO sang các Domain Model tương thích giao diện
                val trending = trendingDto.map { dto ->
                    BookModel(id = dto.id, title = dto.title, author = dto.author, coverUrl = dto.coverUrl)
                }
                val recent = recentDto.map { dto ->
                    BookModel(id = dto.id, title = dto.title, author = dto.author, coverUrl = dto.coverUrl)
                }
                val recommended = recommendedDto.map { dto ->
                    BookModel(id = dto.id, title = dto.title, author = dto.author, coverUrl = dto.coverUrl)
                }
                val newRelease = newReleaseDto.map { dto ->
                    BookModel(id = dto.id, title = dto.title, author = dto.author, coverUrl = dto.coverUrl)
                }
                val allBooks = allBooksDto.map { dto ->
                    BookModel(id = dto.id, title = dto.title, author = dto.author, coverUrl = dto.coverUrl)
                }

                // Loại bỏ thuộc tính thừa 'bookCount' nếu CategoryModel không định nghĩa trong constructor chính
                val categories = categoriesDto.map { dto ->
                    CategoryModel(id = dto.id, name = dto.name)
                }

                // 3. Cập nhật trạng thái an toàn lên UI State
                _uiState.update {
                    HomeUiState(
                        isLoading = false,
                        trendingBooks = trending,
                        recentBooks = recent,
                        recommendedBooks = recommended,
                        categories = categories,
                        newReleaseBooks = newRelease,
                        allBooks = allBooks
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Đã xảy ra lỗi khi tải dữ liệu từ máy chủ.")
                }
            }
        }
    }
}