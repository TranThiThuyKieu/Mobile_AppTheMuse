package com.example.appthemuse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.appthemuse.data.model.CategoryUi
import com.example.appthemuse.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(viewModel: HomeViewModel, onBookClick: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    // Tab đang được chọn: mặc định là "Tất cả"
    var selectedTab by remember { mutableStateOf("Tất cả") }
    // Lọc danh sách sách theo tab
    val displayBooks = when (selectedTab) {
        "Hot" -> uiState.trendingBooks   // sách hot
        "Mới" -> uiState.recentBooks     // sách mới
        else -> uiState.allBooks          // tất cả sách
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Thể loại
        item {
            Text(
                text = "Tất cả thể loại",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(350.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(uiState.categories) { category ->
                    CategoryCard(category = category)
                }
            }
        }

        // Danh sách truyện
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tất cả truyện",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Tab chọn loại sách (Filter Chips)
        item {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Tất cả", "Hot", "Mới").forEach { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,// trạng thái chọn tab
                        onClick = { selectedTab = tab },// đổi tab
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // Hiển thị sách theo tab kèm đồng bộ màu sắc
        items(displayBooks) { book ->
            VerticalBookItem(
                book = book,
                onClick = { onBookClick(book.id) }
            )
        }
    }
}

// Hàm hiển thị từng thẻ Thể loại (Category)
@Composable
fun CategoryCard(category: CategoryUi) {
    Card(
        modifier = Modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Tên thể loại
            Text(
                text = category.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${category.totalBooks} tác phẩm",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}