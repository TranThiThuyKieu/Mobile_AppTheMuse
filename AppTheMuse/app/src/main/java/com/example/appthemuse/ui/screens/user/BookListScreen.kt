package com.example.appthemuse.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appthemuse.ui.components.AppBottomBar
import com.example.appthemuse.ui.components.HomeTopBar
import com.example.appthemuse.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(title: String, type: String, viewModel: HomeViewModel,
                   navController: NavController, onBookClick: (String) -> Unit) {
    var showSearch by remember {
        mutableStateOf(false)
    }
    val uiState by viewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    // Chọn danh sách sách tùy theo loại truyền vào
    val books = when (type) {
        "tatca" -> uiState.allBooks
        // Sách nổi bật
        "hot" -> uiState.trendingBooks
        // Sách mới phát hành
        "new" -> uiState.newReleaseBooks
        // Sách đề xuất
        "recommend" -> uiState.recommendedBooks
        // Nếu không phải 3 loại trên thì xem như category_id
        else -> uiState.allBooks.filter {
            it.category_id == type
        }
    }
    Scaffold(
        // Thanh top bar
        topBar = {
            HomeTopBar(onSearchClick = {
                    showSearch = true
                }
            )
        },
        // Thanh bottom navigation
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = currentRoute)
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Tiêu đề trạng thái / loại sách
            Text("TRẠNG THÁI", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            // Hiển thị title
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFEAEAEA)) {
                Text(text = title, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
            }
            Spacer(Modifier.height(16.dp))
            // Danh sách sách
            LazyColumn {
                items(books) {
                    VerticalBookItem(book = it, onClick = {
                        onBookClick(it.id)
                        }
                    )
                }
            }
        }
    }
    // Hiển thị màn hình tìm kiếm nếu showSearch = true
    if (showSearch) {
        SearchScreen(viewModel = viewModel, onClose = {
                showSearch = false
            }
        )
    }
}