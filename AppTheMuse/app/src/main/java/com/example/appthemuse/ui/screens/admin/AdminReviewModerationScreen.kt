package com.example.appthemuse.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appthemuse.ui.viewmodel.AdminReviewModerationViewModel
import com.example.appthemuse.ui.viewmodel.ReviewFilter

@Composable
fun AdminReviewModerationScreen(
    bookId: String,
    viewModel: AdminReviewModerationViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bookId) {
        viewModel.load(bookId)
    }

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TextButton(onClick = onBack) {
                    Text("Quay lai")
                }
            }

            item {
                Text(
                    text = "Danh gia va binh luan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ReviewFilter.values().forEach { filter ->
                        FilterChip(
                            selected = uiState.filter == filter,
                            onClick = { viewModel.selectFilter(filter) },
                            label = { Text(filter.label) }
                        )
                    }
                }
            }

            uiState.errorMessage?.let { message ->
                item { Text(text = message, color = MaterialTheme.colorScheme.error) }
            }

            if (uiState.isLoading) {
                item { Text("Dang tai danh gia...") }
            }

            if (!uiState.isLoading && uiState.reviews.isEmpty()) {
                item { Text("Khong co danh gia phu hop") }
            }

            items(uiState.reviews, key = { it.id }) { review ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "User: ${review.userId}", fontWeight = FontWeight.SemiBold)
                            Text(text = review.ratingText)
                        }
                        Text(text = review.comment)
                        Text(
                            text = "${review.statusText} - ${review.createdAtText}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (review.isHidden) {
                            OutlinedButton(onClick = { viewModel.restoreReview(review.id) }) {
                                Text("Hoan tac")
                            }
                        } else {
                            OutlinedButton(onClick = { viewModel.hideReview(review.id) }) {
                                Text("An danh gia")
                            }
                        }
                    }
                }
            }
        }
    }
}
