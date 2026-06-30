package com.example.appthemuse.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onChapterClick: (Int) -> Unit = {},
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
                    Text("Quay lại")
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            uiState.errorMessage?.let { message ->
                item { Text(text = message, color = MaterialTheme.colorScheme.error) }
            }

            uiState.book?.let { book ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            
                            val statusColor = when (book.statusValue) {
                                "pending" -> Color(0xFFFACC15) // Vàng
                                "ongoing" -> Color(0xFF3B82F6) // Xanh dương
                                "completed" -> Color(0xFF22C55E) // Xanh lá
                                else -> Color.Gray
                            }
                            StatusChip(label = book.statusLabel, color = statusColor)
                        }
                        Text(text = "Tác giả: ${book.authorId}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Thể loại: ${book.categoryId}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Ngày tạo: ${book.createdAtText}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = book.description.ifBlank { "Chưa có mô tả" },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        AdminStatCard("Lượt xem", book.viewCountText, Modifier.weight(1f))
                        AdminStatCard("Chương", book.chapterCountText, Modifier.weight(1f))
                        AdminStatCard("Đánh giá", book.reviewCountText, Modifier.weight(1f))
                    }
                }

                item {
                    Button(
                        onClick = { onOpenReviews(book.id) }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Xem đánh giá và bình luận")
                    }
                }
            }

            item {
                Text(
                    text = "Danh sách chương",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (!uiState.isLoading && uiState.chapters.isEmpty()) {
                item { Text("Sách này chưa có chương") }
            }

            items(uiState.chapters, key = { it.id }) { chapter ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChapterClick(chapter.chapterNumber) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = chapter.chapterNumberText, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = chapter.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(text = "${chapter.stateText} - ${chapter.createdAtText}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}
