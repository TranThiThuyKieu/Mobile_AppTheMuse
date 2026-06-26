package com.example.appthemuse.ui.screens.user

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
import com.example.appthemuse.ui.model.BookUi
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.appthemuse.ui.model.HistoryUi
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, navController: NavController, onBookClick: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSearch by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = { HomeTopBar(onSearchClick = {
            showSearch = true
        }) },
        containerColor = MaterialTheme.colorScheme.background
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
                if (uiState.trendingBooks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Truyện Trending",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                        val pagerState = rememberPagerState(pageCount = { uiState.trendingBooks.size })
                        HorizontalPager(state = pagerState) { page ->
                            val book = uiState.trendingBooks[page]
                            BannerItem(book = book, onClick = { onBookClick(book.id) })
                        }
                    }
                }
                // Chuyển sang trang hiển thị tất cả truyện hot
                item { SectionHeader(title = "Truyện Hot", onSeeMoreClick = {
                        navController.navigate("book/Truyện Hot/hot")
                    })
                }
                items(uiState.trendingBooks.take(2)) { book ->
                    VerticalBookItem(book = book, onClick = { onBookClick(book.id) })
                }
                // Chuyển sang trang hiển thị tất cả truyện được đề xuất
                item { SectionHeader(title="Đề xuất cho bạn", onSeeMoreClick={
                        navController.navigate("book/Đề xuất/recommend")
                    }
                ) }
                items(uiState.recommendedBooks.take(2)) { book ->
                    VerticalBookItem(book = book, onClick = { onBookClick(book.id) })
                }
                if (uiState.recentBooks.isNotEmpty()) {
                    // Chuyển sang trang hiển thị tất cả truyện mới cập nhật
                    item {
                        SectionHeader(title="Mới cập nhật", onSeeMoreClick={
                                navController.navigate("book/Sách mới/new")
                            }
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.recentBooks) { book ->
                                RecentBookCard(book = book, onClick = { onBookClick(book.id) })
                            }
                        }
                    }
                    items(uiState.newReleaseBooks.take(2)) { book ->
                        VerticalBookItem(book = book, onClick = { onBookClick(book.id) })
                    }
                }
                // Chuyển sang trang tất cả thể loại
                item {
                    SectionHeader(title = "Khám phá thể loại" ,onSeeMoreClick = {
                        navController.navigate("categories")
                    })
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(140.dp).padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)){
                        items(uiState.categories.size){ index ->
                            val category = uiState.categories[index]
                            Card(
                                modifier = Modifier.clickable {
                                    navController.navigate("book/${category.name}/${category.id.removePrefix("cate")}")
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center) {
                                    val category = uiState.categories[index]
                                    Text(text = category.name, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val count = uiState.allBooks.count {
                                        it.category_id == category.id.removePrefix("cate")
                                    }
                                    Text(text = "$count truyện", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                // Chuyển sang trang Sách mới phát hành
                item { SectionHeader(title="Sách mới phát hành", onSeeMoreClick={
                        navController.navigate("book/Sách mới/new")
                    }
                ) }
                items(uiState.newReleaseBooks.take(2)) { book ->
                    VerticalBookItem(book = book, onClick = { onBookClick(book.id) })
                }

            }
        }
    }
    // Hiển thị kết quả tìm kiếm
    if (showSearch) {
        SearchScreen(
            viewModel = viewModel,
            onClose = {
                showSearch = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onSearchClick: () -> Unit) {
    TopAppBar(
        title = { Text("The Muse", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        actions = {
            IconButton(onClick = onSearchClick) { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground) }
            IconButton(onClick = {}) { Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = MaterialTheme.colorScheme.onBackground) }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
fun SectionHeader(title: String, onSeeMoreClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Xem thêm →", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, modifier = Modifier.clickable { onSeeMoreClick() })
    }
}

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

@Composable
fun VerticalBookItem(book: BookUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onClick() },
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
                modifier = Modifier.size(width = 70.dp, height = 95.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1.0f)) {
                Text(book.title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(book.author_name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("📚 ${book.chapter_count} Chương  •  ⭐ ${book.rating}  •  👁️ ${book.view_count}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun RecentBookCard(book: BookUi, onClick: () -> Unit) {
    Card(modifier = Modifier.width(160.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column {
            AsyncImage(model = book.cover_url, contentDescription = null, modifier = Modifier.fillMaxWidth().height(120.dp), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(8.dp)) {
                Text(book.title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(book.author_name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
@Composable
fun HistoryBookItem(
    historyBook: HistoryUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = historyBook.book.cover_url,
                contentDescription = null,
                modifier = Modifier
                    .size(width = 70.dp, height = 95.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = historyBook.book.title,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = historyBook.book.author_name,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${historyBook.book.chapter_count} chương",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Đã đọc ${historyBook.progressPercent}%"
                )

                LinearProgressIndicator(
                    progress = {
                        historyBook.progressPercent / 100f
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                historyBook.lastReadAt?.let {

                    Text(
                        text = "Cập nhật ${
                            SimpleDateFormat(
                                "dd/MM/yyyy",
                                LocalLocale.current.platformLocale
                            ).format(it.toDate())
                        }",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}