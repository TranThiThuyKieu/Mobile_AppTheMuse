package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appthemuse.data.repository.AdminBookRepositoryImpl
import com.example.appthemuse.domain.model.BookStatus
import com.example.appthemuse.domain.repository.AdminBookRepository
import com.example.appthemuse.ui.mapper.toUi
import com.example.appthemuse.ui.model.AdminBookStatsUi
import com.example.appthemuse.ui.model.AdminBookUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminBookManagementUiState(
    val isLoading: Boolean = false,
    val selectedStatus: BookStatus? = null,
    val keyword: String = "",
    val books: List<AdminBookUi> = emptyList(),
    val stats: AdminBookStatsUi? = null,
    val errorMessage: String? = null
)

class AdminBookManagementViewModel(
    private val repository: AdminBookRepository = AdminBookRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminBookManagementUiState())
    val uiState: StateFlow<AdminBookManagementUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            loadBooks()
        }
    }

    fun selectStatus(status: BookStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
        refresh()
    }

    fun updateKeyword(keyword: String) {
        _uiState.update { it.copy(keyword = keyword) }
        refresh()
    }

    fun approveBook(bookId: String) {
        changeBookStatus(bookId, BookStatus.Ongoing)
    }

    fun toggleHideBook(book: AdminBookUi) {
        val newStatus = if (book.statusValue == "hidden") BookStatus.Ongoing else BookStatus.Hidden
        changeBookStatus(book.id, newStatus)
    }

    private fun changeBookStatus(bookId: String, status: BookStatus) {
        viewModelScope.launch {
            runCatching {
                repository.updateBookStatus(bookId, status)
            }.onSuccess {
                loadBooks()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private suspend fun loadBooks() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val state = _uiState.value

        runCatching {
            val stats = repository.getBookStats().toUi()
            val books = repository.getBooks(state.selectedStatus, state.keyword).map { it.toUi() }
            stats to books
        }.onSuccess { (stats, books) ->
            _uiState.update {
                it.copy(isLoading = false, stats = stats, books = books, errorMessage = null)
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(isLoading = false, errorMessage = error.message ?: "Khong tai duoc du lieu")
            }
        }
    }
}
