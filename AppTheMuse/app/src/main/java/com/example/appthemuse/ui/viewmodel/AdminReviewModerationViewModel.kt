package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.data.repository.AdminBookRepositoryImpl
import com.example.appthemuse.domain.repository.AdminBookRepository
import com.example.appthemuse.ui.mapper.toUi
import com.example.appthemuse.ui.model.AdminReviewUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ReviewFilter(val label: String) {
    All("Tat ca"),
    Visible("Dang hien thi"),
    Hidden("Da an")
}

data class AdminReviewModerationUiState(
    val isLoading: Boolean = false,
    val filter: ReviewFilter = ReviewFilter.All,
    val reviews: List<AdminReviewUi> = emptyList(),
    val errorMessage: String? = null
)

class AdminReviewModerationViewModel(
    private val repository: AdminBookRepository = AdminBookRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminReviewModerationUiState())
    val uiState: StateFlow<AdminReviewModerationUiState> = _uiState.asStateFlow()

    private var currentBookId: String? = null

    fun load(bookId: String) {
        currentBookId = bookId
        viewModelScope.launch { loadReviews(bookId) }
    }

    fun selectFilter(filter: ReviewFilter) {
        _uiState.update { it.copy(filter = filter) }
        currentBookId?.let(::load)
    }

    fun hideReview(reviewId: String) {
        setHidden(reviewId, true)
    }

    fun restoreReview(reviewId: String) {
        setHidden(reviewId, false)
    }

    private fun setHidden(reviewId: String, hidden: Boolean) {
        val bookId = currentBookId ?: return
        viewModelScope.launch {
            runCatching {
                repository.setReviewHidden(bookId, reviewId, hidden)
            }.onSuccess {
                loadReviews(bookId)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private suspend fun loadReviews(bookId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        runCatching {
            repository.getReviews(bookId, includeHidden = true).map { it.toUi() }
        }.onSuccess { reviews ->
            val filtered = when (_uiState.value.filter) {
                ReviewFilter.All -> reviews
                ReviewFilter.Visible -> reviews.filter { !it.isHidden }
                ReviewFilter.Hidden -> reviews.filter { it.isHidden }
            }
            _uiState.update {
                it.copy(isLoading = false, reviews = filtered, errorMessage = null)
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(isLoading = false, errorMessage = error.message ?: "Khong tai duoc danh gia")
            }
        }
    }
}
