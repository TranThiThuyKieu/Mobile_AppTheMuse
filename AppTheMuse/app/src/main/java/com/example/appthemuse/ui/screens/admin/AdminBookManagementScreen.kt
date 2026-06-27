package com.example.appthemuse.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appthemuse.domain.model.BookStatus
import com.example.appthemuse.ui.components.AdminBookRow
import com.example.appthemuse.ui.components.AdminStatCard
import com.example.appthemuse.ui.viewmodel.AdminBookManagementViewModel

@Composable
fun AdminBookManagementScreen(
    viewModel: AdminBookManagementViewModel,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Quan ly sach",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                uiState.stats?.let { stats ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            AdminStatCard("Tong", stats.total, Modifier.weight(1f))
                            AdminStatCard("Cho duyet", stats.pending, Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            AdminStatCard("Dang cap nhat", stats.ongoing, Modifier.weight(1f))
                            AdminStatCard("Da an", stats.hidden, Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.keyword,
                    onValueChange = viewModel::updateKeyword,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Tim theo ten sach hoac tac gia") }
                )
            }

            item {
                StatusFilters(
                    selectedStatus = uiState.selectedStatus,
                    onSelect = viewModel::selectStatus
                )
            }

            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (uiState.isLoading) {
                item { Text("Dang tai du lieu...") }
            }

            if (!uiState.isLoading && uiState.books.isEmpty()) {
                item { Text("Khong co sach phu hop") }
            }

            items(uiState.books, key = { it.id }) { book ->
                AdminBookRow(
                    book = book,
                    onClick = { onBookClick(book.id) },
                    onApprove = { viewModel.approveBook(book.id) },
                    onHide = { viewModel.hideBook(book.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatusFilters(
    selectedStatus: BookStatus?,
    onSelect: (BookStatus?) -> Unit
) {
    Column {
        Text(text = "Trang thai", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { onSelect(null) },
                label = { Text("Tat ca") }
            )
            BookStatus.values().forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onSelect(status) },
                    label = { Text(status.label) }
                )
            }
        }
    }
}
