package com.example.appthemuse.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appthemuse.ui.viewmodel.HomeViewModel
import com.example.appthemuse.data.model.BookUi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, onBookClick: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = { HomeTopBar() }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // 1. Slider / Banner "Truyện Trending"
                if (uiState.trendingBooks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Truyện Trending",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                        val pagerState = rememberPagerState(pageCount = { uiState.trendingBooks.size })
                        HorizontalPager(state = pagerState) { page ->
                            val book = uiState.trendingBooks[page]
                            BannerItem(book = book, onClick = { onBookClick(book.id) })
                        }
                    }
                }
                // 2. Section "Truyện Hot" (Danh sách hàng dọc thu nhỏ)
                item { SectionHeader(title = "Truyện Hot") }
                items(uiState.trendingBooks.take(2)) { book ->
                    VerticalBookItem(book = book, onClick = { onBookClick(book.id) })
                }
                // 3. Section "Đề xuất cho bạn"
                item { SectionHeader(title = "Đề xuất cho bạn") }
                items(uiState.recommendedBooks.take(2)) { book ->
                    VerticalBookItem(book = book, onClick = { onBookClick(book.id) })
                }
                // 4. Section "Mới cập nhật" (Dạng thẻ cuộn ngang lớn)
                if (uiState.recentBooks.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Mới cập nhật")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.recentBooks) { book ->
                                RecentBookCard(book = book, onClick = { onBookClick(book.id) })
                            }
                        }
                    }
                    item {
                        SectionHeader(
                            title = "Khám phá thể loại"
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.height(140.dp).padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)){
                            items(uiState.categories.size){ index ->
                                Card {
                                    // Hiển thị danh sách thể loại sách + số sách của thể loại đó
                                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp),
                                           horizontalAlignment = Alignment.CenterHorizontally,
                                           verticalArrangement = Arrangement.Center) {
                                        val category = uiState.categories[index]
                                        Text(text = category.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "${category.totalBooks} truyện", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        SectionHeader(
                            title = "Sách mới phát hành"
                        )
                    }
                    items(uiState.newReleaseBooks.take(2)) { book ->
                        VerticalBookItem(
                            book = book,
                            onClick = {
                                onBookClick(book.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
// Thanh tiêu đề phía trên của màn hình Home
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    TopAppBar(
        title = { Text("The Muse", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        actions = {
            IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = "Search") }
            IconButton(onClick = {}) { Icon(Icons.Default.Notifications, contentDescription = "Alerts") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}
// Tiêu đề của từng mục nội dung
@Composable
fun SectionHeader(title: String, onSeeMoreClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text("Xem thêm →", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, modifier = Modifier.clickable { onSeeMoreClick() })
    }
}
// Banner truyện trending dạng slider
@Composable
fun BannerItem(book: BookUi, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 16.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp)) {
        Box {
            AsyncImage(model = book.cover_url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
            Text(
                text = book.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
        }
    }
}
// Item truyện hiển thị theo chiều dọc
@Composable
fun VerticalBookItem(book: BookUi, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = book.cover_url,
            contentDescription = null,
            modifier = Modifier.size(width = 80.dp, height = 110.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            Text(book.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(book.author_name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text("📚 ${book.chapter_count} Chương  •  ⭐ ${book.rating}  •  👁️ ${book.view_count}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}
// Thẻ truyện dạng ngang dùng cho mục mới cập nhật
@Composable
fun RecentBookCard(book: BookUi, onClick: () -> Unit) {
    Card(modifier = Modifier.width(160.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column {
            AsyncImage(model = book.cover_url, contentDescription = null, modifier = Modifier.fillMaxWidth().height(120.dp), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(8.dp)) {
                Text(book.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(book.author_name, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}