package com.example.appthemuse.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(title: String = "The Muse", onSearchClick: () -> Unit, onNotificationClick: () -> Unit = {}) {
    TopAppBar(
        // Phần tiêu đề bên trái
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        },
        // Các nút chức năng bên phải
        actions = {
            // Nút tìm kiếm
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
            }
            // Nút thông báo
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Default.Notifications, contentDescription = "Notification", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    )
}