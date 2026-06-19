package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.CategoryModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface GenreUiState {
    object Loading : GenreUiState
    data class Success(val categories: List<CategoryModel>) : GenreUiState
    data class Error(val message: String) : GenreUiState
}

class GenreSelectionViewModel : ViewModel() {

    private val _genreState = MutableStateFlow<GenreUiState>(GenreUiState.Loading)
    val genreState: StateFlow<GenreUiState> = _genreState.asStateFlow()

    private val firestoreService = FirestoreService()

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        _genreState.value = GenreUiState.Loading
        viewModelScope.launch {
            try {
                val categoriesDto = firestoreService.getCategoriesList()
                // Map biến từ CategoryDto sang CategoryModel thuần túy chuẩn UI
                val categories = categoriesDto.map { dto ->
                    CategoryModel(id = dto.id, name = dto.name)
                }
                _genreState.value = GenreUiState.Success(categories)
            } catch (e: Exception) {
                _genreState.value = GenreUiState.Error(e.localizedMessage ?: "Lỗi tải thể loại")
            }
        }
    }
}