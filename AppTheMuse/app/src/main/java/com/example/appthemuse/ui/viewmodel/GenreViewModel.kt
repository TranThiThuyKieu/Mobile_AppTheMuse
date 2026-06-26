package com.example.appthemuse.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.domain.repository.AuthRepository
import com.example.appthemuse.domain.repository.BookRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

sealed interface GenreState {
    object Idle : GenreState
    object Loading : GenreState
    object Success : GenreState
    data class Error(val message: String) : GenreState
}

class GenreViewModel(
    private val authRepository: AuthRepository,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _genreState = mutableStateOf<GenreState>(GenreState.Idle)
    val genreState: State<GenreState> = _genreState

    private val _categories = mutableStateOf<List<Category>>(emptyList())
    val categories: State<List<Category>> = _categories

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val categoryList = bookRepository.getCategories()
                _categories.value = categoryList
            } catch (e: Exception) {
                _genreState.value = GenreState.Error(e.localizedMessage ?: "Lỗi tải thể loại")
            }
        }
    }

    fun saveFavoriteGenres(genres: List<String>) {
        _genreState.value = GenreState.Loading
        viewModelScope.launch {

            val userId = authRepository.getCurrentUserId() ?: return@launch

            authRepository.saveFavoriteGenres(userId, genres)
                .onSuccess {
                    _genreState.value = GenreState.Success
                }
                .onFailure { error ->
                    _genreState.value = GenreState.Error(error.message ?: "Không thể lưu sở thích")
                }
        }
    }

    fun resetState() {
        _genreState.value = GenreState.Idle
    }
}
