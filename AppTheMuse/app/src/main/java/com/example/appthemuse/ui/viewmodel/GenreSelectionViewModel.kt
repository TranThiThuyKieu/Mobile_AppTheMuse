package com.example.appthemuse.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.data.repository.AuthRepository
import com.example.appthemuse.domain.model.CategoryModel
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
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _genreState = mutableStateOf<GenreState>(GenreState.Idle)
    val genreState: State<GenreState> = _genreState

    private val _categories = mutableStateOf<List<CategoryModel>>(emptyList())
    val categories: State<List<CategoryModel>> = _categories

    // 1. Tải danh sách thể loại
    fun fetchCategories() {
        viewModelScope.launch {
            _categories.value = firestoreService.getCategoriesList()
        }
    }

    // 2. Lưu thể loại đã chọn
    fun saveFavoriteGenres(genres: List<String>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _genreState.value = GenreState.Loading
        viewModelScope.launch {
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