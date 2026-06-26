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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import com.example.appthemuse.ui.components.AdminStatCard
import com.example.appthemuse.ui.components.StatusChip
import com.example.appthemuse.ui.viewmodel.AdminBookDetailViewModel

@Composable
fun AdminBookDetailScreen(
    bookId: String,
    viewModel: AdminBookDetailViewModel,
    onBack: () -> Unit,
    onOpenReviews: (String) -> Unit,
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

            if (uiState.isLoading) {
                item { Text("Dang tai chi tiet sach...") }
            }

            uiState.errorMessage?.let { message ->
                item { Text(text = message, color = MaterialTheme.colorScheme.error) }
            }

            uiState.book?.let { book ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusChip(label = book.statusLabel)
                        }
                        Text(text = "Tac gia: ${book.authorId}")
                        Text(text = "The loai: ${book.categoryId}")
                        Text(text = "Ngay tao: ${book.createdAtText}")
                        Text(text = book.description.ifBlank { "Chua co mo ta" })
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        AdminStatCard("Luot xem", book.viewCountText, Modifier.weight(1f))
                        AdminStatCard("Chuong", book.chapterCountText, Modifier.weight(1f))
                        AdminStatCard("Danh gia", book.reviewCountText, Modifier.weight(1f))
                    }
                }

                item {
                    Button(onClick = { onOpenReviews(book.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Xem danh gia va binh luan")
                    }
                }
            }

            item {
                Text(
                    text = "Danh sach chuong",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!uiState.isLoading && uiState.chapters.isEmpty()) {
                item { Text("Sach nay chua co chuong") }
            }

            items(uiState.chapters, key = { it.id }) { chapter ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = chapter.chapterNumberText, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = chapter.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(text = "${chapter.stateText} - ${chapter.createdAtText}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
