package com.example.appthemuse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

data class UserAdminUi(
    val uid: String,
    val username: String,
    val email: String,
    val role: String,
    val isBlocked: Boolean,
    val joinDate: String = "Oct 12, 2023"
)

data class AdminUserUiState(
    val users: List<UserAdminUi> = emptyList(),
    val filteredUsers: List<UserAdminUi> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterStatus: String = "Tất cả", // Tất cả, Hoạt động, Đã khóa
    val sortOption: String = "Mới nhất", // Mới nhất, Tên A-Z
    val adminName: String = "Quản trị viên",
    val adminRole: String = "Ban biên tập The Muse",
    val error: String? = null
)

class AdminUserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUserUiState())
    val uiState: StateFlow<AdminUserUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadUsers()
        loadAdminInfo()
    }

    private fun loadAdminInfo() {
        viewModelScope.launch {
            try {
                val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (currentUid != null) {
                    val adminDoc = firestore.collection("users").document(currentUid).get().await()
                    if (adminDoc.exists()) {
                        _uiState.update { it.copy(
                            adminName = adminDoc.getString("username") ?: "Quản trị viên",
                            adminRole = if (adminDoc.getString("role") == "admin") "Ban biên tập The Muse" else "Người dùng"
                        ) }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val snapshot = firestore.collection("users").get().await()
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                
                val userList = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.id
                    val username = doc.getString("username") ?: return@mapNotNull null
                    val email = doc.getString("email") ?: ""
                    val role = doc.getString("role") ?: "user"
                    val isBlocked = doc.getBoolean("is_blocked") ?: false
                    
                    val timestamp = doc.getTimestamp("created_at")
                    val joinDate = if (timestamp != null) sdf.format(timestamp.toDate()) else "Oct 12, 2023"
                    
                    UserAdminUi(uid, username, email, role, isBlocked, joinDate)
                }
                _uiState.update { it.copy(users = userList, isLoading = false) }
                applyFilterAndSort()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilterAndSort()
    }

    fun onFilterStatusChanged(status: String) {
        _uiState.update { it.copy(filterStatus = status) }
        applyFilterAndSort()
    }
    
    fun onSortOptionChanged(option: String) {
        _uiState.update { it.copy(sortOption = option) }
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        val currentState = _uiState.value
        var result = currentState.users

        // Search
        if (currentState.searchQuery.isNotEmpty()) {
            result = result.filter {
                it.username.contains(currentState.searchQuery, ignoreCase = true) ||
                        it.email.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        // Filter status
        result = when (currentState.filterStatus) {
            "Hoạt động" -> result.filter { !it.isBlocked }
            "Đã khóa" -> result.filter { it.isBlocked }
            else -> result
        }
        
        // Sort
        result = when (currentState.sortOption) {
            "Tên A-Z" -> result.sortedBy { it.username }
            "Mới nhất" -> result
            else -> result
        }

        _uiState.update { it.copy(filteredUsers = result) }
    }

    fun toggleBlockUser(user: UserAdminUi) {
        viewModelScope.launch {
            try {
                firestore.collection("users").document(user.uid)
                    .update("is_blocked", !user.isBlocked).await()
                loadUsers()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
