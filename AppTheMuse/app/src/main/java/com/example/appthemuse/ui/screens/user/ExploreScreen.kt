package com.example.appthemuse.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appthemuse.ui.components.HomeTopBar
import com.example.appthemuse.ui.model.CategoryUi
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.viewmodel.HomeViewModel

@Composable
fun ExploreScreen(viewModel: HomeViewModel, navController: NavController, onBookClick: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val books = uiState.allBooks
    // Tab đang được chọn: mặc định là "Tất cả"
    var selectedTab by remember {
        mutableStateOf("Tất cả")
    }
    var showSearch by remember {
        mutableStateOf(false)
    }
    // Lọc danh sách sách theo tab
    val displayBooks = when (selectedTab) {
        "Hot" -> uiState.trendingBooks   // sách hot
        "Mới" -> uiState.recentBooks     // sách mới
        else -> uiState.allBooks          // tất cả sách
    }
    val limitedBooks = displayBooks.take(5)
    // Thanh top bar
    Scaffold(
        topBar = {
            HomeTopBar(
                onSearchClick = {
                    showSearch = true
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
        // Thể loại
        item {
            Text(text = "Tất cả thể loại", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(350.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)) {
                items(uiState.categories) { category ->
                    CategoryCard(category = category, books = uiState.allBooks, onClick = {
                            navController.navigate("book/${category.name}/${category.id.removePrefix("cate")}")
                        }
                    )
                }
            }
        }

        // Danh sách truyện
        item {
            Spacer(Modifier.height(16.dp))
            Text(text = "Tất cả truyện", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        }

        // Tab chọn loại sách
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Tất cả", "Hot", "Mới").forEach { tab ->
                        FilterChip(selected = selectedTab == tab, onClick = { selectedTab = tab },
                            label = { Text(tab) }
                        )
                    }
                }
                // Chuyển sang trang danh sách thể loại
                Text(
                    text = "Xem thêm →",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        when (selectedTab) {
                            "Hot" -> {
                                navController.navigate("book/Truyện Hot/hot")
                            }
                            "Mới" -> {
                                navController.navigate("book/Sách mới/new")
                            }
                            else -> {
                                navController.navigate("book/Tất cả/tatca")
                            }
                        }
                    }
                )
            }
        }
        // hiển thị sách theo tab
        items(limitedBooks) { book ->
            VerticalBookItem(
                book = book,
                onClick = {
                    onBookClick(book.id)
                }
            )
        }
    }
}
    if (showSearch) {
        SearchScreen(
            viewModel = viewModel,
            onClose = {
                showSearch = false
            }
        )
    }}
// Hiển thị các thể loại sách
@Composable
fun CategoryCard(category: CategoryUi, books: List<BookUi>, onClick: () -> Unit) {
    Card(modifier = Modifier.height(90.dp).clickable {
            onClick()
        }) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            // Tên thể loại
            Text(text = category.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            // Đếm số lượng sách của một thể loại
            val count = books.count {
                it.category_id == category.id.removePrefix("cate")
            }
            Text(text = "$count tác phẩm")
        }
    }
}