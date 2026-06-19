package com.example.appthemuse.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appthemuse.domain.model.BookModel
import com.example.appthemuse.domain.model.CategoryModel
import com.example.appthemuse.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBookClick: (String) -> Unit,
    onSeeMoreCategoriesClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { HomeTopBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // 1. Slider / Banner "Truyện Trending"
                if (uiState.trendingBooks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Truyện Trending",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp)
                        )
                        val pagerState = rememberPagerState(pageCount = { uiState.trendingBooks.size })
                        HorizontalPager(state = pagerState) { page ->
                            val book = uiState.trendingBooks[page]
                            BannerItem(book = book, onClick = { onBookClick(book.id.toString()) })
                        }
                    }
                }

                // 2. Section "Truyện Hot"
                item { SectionHeader(title = "Truyện Hot") }
                items(uiState.trendingBooks.take(3)) { book ->
                    VerticalBookItem(book = book, onClick = { onBookClick(book.id.toString()) })
                }

                // 3. Section "Đề xuất cho bạn"
                item { SectionHeader(title = "Đề xuất cho bạn") }
                items(uiState.recommendedBooks.take(3)) { book ->
                    VerticalBookItem(book = book, onClick = { onBookClick(book.id.toString()) })
                }

                // 4. Section "Mới cập nhật"
                if (uiState.recentBooks.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Mới cập nhật")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(uiState.recentBooks) { book ->
                                RecentBookCard(book = book, onClick = { onBookClick(book.id.toString()) })
                            }
                        }
                    }
                }

                // 5. Section "Khám phá thể loại"
                if (uiState.categories.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Khám phá thể loại", onSeeMoreClick = onSeeMoreCategoriesClick)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(uiState.categories) { category ->
                                HomeCategoryCard(category = category)
                            }
                        }
                    }
                }

                // 6. Section "Sách mới phát hành" - Đã sửa đồng bộ lấy đúng từ mảng dữ liệu riêng biệt
                if (uiState.newReleaseBooks.isNotEmpty()) {
                    item { SectionHeader(title = "Sách mới phát hành") }
                    items(uiState.newReleaseBooks.take(3)) { book ->
                        VerticalBookItem(book = book, onClick = { onBookClick(book.id.toString()) })
                    }
                }
            }
        }
    }
}

// Giữ nguyên các thành phần TopBar và SectionHeader từ code gốc của bạn...

@Composable
fun RecentBookCard(book: BookModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(book.title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                // Đã sửa lỗi: Chuyển đổi gọi chính xác từ thuộc tính 'book.author' thay vì 'authorName'
                Text(book.author, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}