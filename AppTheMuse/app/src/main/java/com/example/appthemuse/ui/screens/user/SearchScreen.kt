package com.example.appthemuse.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.appthemuse.ui.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SearchScreen(viewModel: HomeViewModel, onClose: () -> Unit) {
    // search theo title
    var filterType by remember {
        mutableStateOf("title")
    }
    // search theo trạng thái
    var status by remember {
        mutableStateOf<String?>(null)
    }
    // search theo rating
    var star by remember {
        mutableStateOf<Int?>(null)
    }
    // keyword người dùng nhập
    var keyword by remember {
        mutableStateOf("")
    }
    // bật/tắt bottom sheet filter
    var showFilter by remember {
        mutableStateOf(false)
    }
    // lấy state từ ViewModel (search result + history)
    val uiState by viewModel.uiState.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    // Load search history khi mở screen
    LaunchedEffect(Unit) {
        if (uid != null) {
            viewModel.loadSearchHistory(uid)
        }
    }
    // mỗi khi keyword/filter thay đổi thì search lại
    LaunchedEffect(keyword, filterType, status, star) {
        // nếu chưa nhập gì thì reset kết quả
        if (keyword.isBlank()) {
            viewModel.clearSearch()
            return@LaunchedEffect
        }
        // chống spam search
        kotlinx.coroutines.delay(300)
        viewModel.searchBooks(keyword = keyword, filterType = filterType, status = status, star = star)
    }
    Scaffold(
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // input search
                OutlinedTextField(value = keyword,
                    onValueChange = {
                        keyword = it
                        // nếu xoá hết text thì clear search result
                        if (it.isBlank()) {
                            viewModel.clearSearch()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Tìm truyện, tác giả...")
                    }
                )
                IconButton(
                    onClick = {
                        // lưu lịch sử tìm kiếm theo user hiện tại
                        if (uid != null) {
                            viewModel.saveSearchHistory(userId = uid, keyword = keyword)
                        }
                        // chạy search
                        viewModel.searchBooks(keyword = keyword, filterType = filterType, status = status, star = star)
                    }
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
                // mở filter bottom sheet
                IconButton(
                    onClick = {
                        showFilter = true
                    }
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "")

                }
                // đóng màn hình search
                IconButton(
                    onClick = onClose
                ) {
                    Icon(Icons.Default.Close, contentDescription = "")
                }
            }
        }
    ) { padding ->
        // body list (search / history)
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (keyword.isBlank()) {
                item {
                    Text(text = "Lịch sử tìm kiếm", style = MaterialTheme.typography.titleMedium)
                }
                // list history từ firestore
                items(uiState.searchHistory) { history ->
                    Text(text = history, modifier = Modifier.fillMaxWidth()
                            .clickable {
                                // click history → set keyword + search lại
                                keyword = history
                                viewModel.searchBooks(keyword = history, filterType = filterType, status = status, star = star)
                            }.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(uiState.searchResults) { book ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            AsyncImage(model = book.cover_url, contentDescription = null, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(book.title)
                                Text("Tác giả: ${book.author_name}")
                                Text("${book.chapter_count} chương")
                                Text("⭐ ${book.rating}")
                            }
                        }
                    }
                }
            }

        }
        // filter bottom sheet
        if (showFilter) {
            FilterBottomSheet(
                onApply = { type, selectedStatus, selectedStar ->
                    // cập nhật filter state
                    filterType = type
                    status = selectedStatus
                    star = selectedStar
                    // chạy search lại theo filter mới
                    viewModel.searchBooks(keyword = keyword, filterType = filterType, status = status, star = star)
                    showFilter = false
                },
                onDismiss = {
                    showFilter = false
                }
            )
        }
    }
}