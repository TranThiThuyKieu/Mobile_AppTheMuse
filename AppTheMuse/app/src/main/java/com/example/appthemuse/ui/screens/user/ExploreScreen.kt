package com.example.appthemuse.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appthemuse.ui.model.CategoryUi
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.viewmodel.HomeViewModel

@Composable
fun ExploreScreen(viewModel: HomeViewModel, onBookClick: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Tab đang được chọn: mặc định là "Tất cả"
    var selectedTab by remember {
        mutableStateOf("Tất cả")
    }
    
    // Lọc danh sách sách theo tab
    val displayBooks = when (selectedTab) {
        "Hot" -> uiState.trendingBooks   // sách hot
        "Mới" -> uiState.recentBooks     // sách mới
        else -> uiState.allBooks          // tất cả sách
    }
    
    LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                    CategoryCard(category)
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
            Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // 3 tab lọc
                listOf("Tất cả", "Hot", "Mới").forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab, // trạng thái chọn tab
                        onClick = { selectedTab = tab },// đổi tab
                        label = { Text(tab) }
                    )
                }
            }
        }
        
        // hiển thị sách theo tab
        items(displayBooks) { book ->
            VerticalBookItem(
                book = book,
                onClick = {
                    onBookClick(book.id)
                }
            )
        }
    }
}

// Hiển thị các thể loại sách
@Composable
fun CategoryCard(category: CategoryUi) {
    Card(modifier = Modifier.height(90.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            // Tên thể loại
            Text(text = category.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${category.totalBooks} tác phẩm") // số lượng sách trong category
        }
    }
}