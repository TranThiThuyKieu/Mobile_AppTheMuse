package com.example.appthemuse.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.domain.model.BookModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    onBackClick: () -> Unit,
    onReadClick: (String) -> Unit
) {
    // Ép ngược String ID nhận từ Route sang Int an toàn để tạo Model mẫu đúng quy chuẩn
    val numericBookId = remember(bookId) { bookId.toIntOrNull() ?: (bookId.hashCode() and 0x7FFFFFFF) }

    val mockBook = remember(numericBookId) {
        BookModel(
            id = numericBookId,
            title = "Thám Tử Trong Đêm",
            slug = "tham-tu-trong-dem",
            authorId = "author_firebase_uid",
            categoryId = 1,
            coverUrl = "",
            description = "Một vụ án bí ẩn xảy ra trong đêm tối. Thám tử Minh phải đối mặt với những manh mối đầy rẫy nguy hiểm để tìm ra sự thật.",
            status = "ongoing",
            isPremium = false,
            viewCount = 125000,
            createdAt = Date()
        )
    }

    val chapters = remember {
        listOf(
            "Chương 1: Khởi đầu",
            "Chương 2: Khởi đầu",
            "Chương 3: Khởi đầu",
            "Chương 4: Khởi đầu",
            "Chương 5: Khởi đầu",
            "Chương 6: Điều tra",
            "Chương 7: Điều tra"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sách", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color(0xFFE9ECEF)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(220.dp)
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .background(Color(0xFF343A40))
                    ) {
                        Text(
                            text = "The Muse\nBook",
                            color = Color.White,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-16).dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(text = mockBook.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Tác giả: Nguyễn Văn A", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("⭐ 4.8", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB100))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("👁️ ${mockBook.viewCount / 1000}K lượt xem", fontSize = 14.sp, color = Color.Gray)
                    }

                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        SuggestionChip(onClick = {}, label = { Text("Trinh Thám", fontSize = 11.sp) })
                        Spacer(modifier = Modifier.width(6.dp))
                        SuggestionChip(onClick = {}, label = { Text("Kinh Dị", fontSize = 11.sp) })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F3F5))

                    Text("Mô tả", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = mockBook.description ?: "Chưa có mô tả nội dung.",
                        fontSize = 13.sp, color = Color(0xFF495057), maxLines = 3, overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onReadClick("chapter_5") },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Đọc tiếp (2%)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = { onReadClick("chapter_1") },
                            modifier = Modifier.weight(1f).height(46.dp),
                            border = BorderStroke(1.dp, Color(0xFF6366F1)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Đọc lại", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Danh sách chương", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("${chapters.size} chương", fontSize = 13.sp, color = Color.Gray)
                }
            }

            itemsIndexed(chapters) { index, chapterTitle ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFE9ECEF))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Chương ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = chapterTitle, fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(text = "19/6/2026", fontSize = 11.sp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}