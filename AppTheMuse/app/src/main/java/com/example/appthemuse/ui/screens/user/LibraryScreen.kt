package com.example.appthemuse.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.appthemuse.ui.components.HomeTopBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.viewmodel.HomeViewModel
import com.example.appthemuse.ui.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    initialTab: Int = 0,
    viewModel: LibraryViewModel,
    navController: NavController,
    userId: String,
    homeViewModel: HomeViewModel,
    onBookClick: (String) -> Unit
) {
    var showSearch by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    // Tab đang được chọn
    var selectedTab by remember(initialTab) {
        mutableStateOf(initialTab)
    }
    // Route hiện tại của Navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(userId) {
        viewModel.loadFavoriteBooks(userId)
        viewModel.loadHistoryBooks(userId)
        viewModel.loadDownloadedBooks()
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onSearchClick = {
                    showSearch = true
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Thư viện",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Quản lý truyện của bạn",
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            /**
             * Thanh tab:
             * 0 = Yêu thích
             * 1 = Lịch sử
             * 2 = Đã tải
             */
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                LibraryTabs(selectedTab = selectedTab, onSelected = {
                    selectedTab = it
                })
            }
            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                // Tab Yêu thích
                0 -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.favoriteBooks) { book ->
                            VerticalBookItem(book = book,
                                onClick = {
                                    onBookClick(book.id)
                                }
                            )
                        }
                    }
                }
                // Tab Lịch sử
                1 -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.historyBooks) { history ->
                            VerticalBookItem(
                                book = history.book,
                                onClick = {
                                    onBookClick(history.book.id)
                                }
                            )
                        }
                    }
                }
                // Tab Đã tải
                2 -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.downloadedBooks) { book ->
                            val hasUpdate = uiState.booksWithUpdates.containsKey(book.id)
                            val isUpdating = uiState.isUpdating[book.id] ?: false

                            DownloadedBookItem(
                                book = book,
                                hasUpdate = hasUpdate,
                                isUpdating = isUpdating,
                                onUpdateClick = { viewModel.updateBookChapters(book.id) },
                                onDeleteClick = { viewModel.deleteBook(book.id) },
                                onClick = { onBookClick(book.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    if (showSearch) {
        SearchScreen(viewModel = homeViewModel, onClose = {
            showSearch = false
        }
        )
    }
}

@Composable
fun DownloadedBookItem(
    book: BookUi,
    hasUpdate: Boolean,
    isUpdating: Boolean,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.cover_url,
                contentDescription = null,
                modifier = Modifier
                    .size(width = 70.dp, height = 95.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author_name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📚 ${book.chapter_count} Chương  •  ⭐ ${book.rating}  •  👁️ ${book.view_count}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                if (hasUpdate) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Button(
                            onClick = onUpdateClick,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cập nhật", fontSize = 12.sp)
                        }
                    }
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa sách",
                    tint = Color.Red
                )
            }
        }
    }
}

// Thanh tab của màn hình Thư viện.
@Composable
fun LibraryTabs(selectedTab: Int, onSelected: (Int) -> Unit) {
    val tabs = listOf("Yêu thích", "Lịch sử", "Đã tải")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF0F0F0))
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSelected(index)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTab == index)
                        MaterialTheme.colorScheme.surface
                    else Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selectedTab == index)
                            MaterialTheme.colorScheme.primary
                        else Color.Gray
                    )
                }
            }
        }
    }
}
