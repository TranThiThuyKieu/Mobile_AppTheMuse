package com.example.appthemuse.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
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
){
    var showSearch by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    // Tab đang được chọn
    var selectedTab by remember(initialTab) {
        mutableStateOf(initialTab)
    }
    // Route hiện tại của Navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    LaunchedEffect(userId) {
        viewModel.loadFavoriteBooks(userId)
        viewModel.loadHistoryBooks(userId)
        viewModel.insertBook()
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Thư viện", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = "Quản lý truyện của bạn", color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            /**
             * Thanh tab:
             * 0 = Yêu thích
             * 1 = Lịch sử
             * 2 = Đã tải
             */
            LibraryTabs(selectedTab = selectedTab, onSelected = {
                    selectedTab = it
                }
            )
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
                        items(uiState.historyBooks){ history ->

                            HistoryBookItem(
                                historyBook = history,
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
                            VerticalBookItem(book = book, onClick = {
                                    onBookClick(book.id)
                                }
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
// Thanh tab của màn hình Thư viện.
@Composable
fun LibraryTabs(selectedTab: Int, onSelected: (Int) -> Unit) {
    val tabs = listOf("Yêu thích", "Lịch sử", "Đã tải")
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF0F0F0)).padding(4.dp)) {
        tabs.forEachIndexed { index, title ->
            Card(modifier = Modifier.weight(1f).clickable {
                        onSelected(index)
                    },
                colors = CardDefaults.cardColors(
                    containerColor =
                        if (selectedTab == index)
                            MaterialTheme.colorScheme.surface
                        else Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center) {
                    Text(text = title,
                        color =
                            if (selectedTab == index)
                                MaterialTheme.colorScheme.primary
                            else Color.Gray
                    )
                }
            }
        }
    }
}