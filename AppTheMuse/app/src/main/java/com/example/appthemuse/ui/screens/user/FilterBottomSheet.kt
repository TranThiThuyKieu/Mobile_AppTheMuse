package com.example.appthemuse.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    // trả dữ liệu filter về SearchScreen
    onApply:(String, String?, Int?)->Unit, onDismiss:()->Unit){
    // filter theo tên tác giả hay tiêu đề
    var selectedType by remember {
        mutableStateOf("author")
    }
    // filter theo trạng thái truyện
    var selectedStatus by remember {
        mutableStateOf<String?>(null)
    }
    // filter theo rating sao
    var selectedStar by remember {
        mutableStateOf<Int?>(null)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "Bộ lọc", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))
            // Lọc theo
            Text("Lọc theo")
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                // chọn lọc theo tác giả
                FilterChip(selected = selectedType == "author",
                    onClick = { selectedType = "author" },
                    label = { Text("Tác giả") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                // chọn lọc theo tiêu đề
                FilterChip(selected = selectedType == "title",
                    onClick = { selectedType = "title" },
                    label = {
                        Text("Tên tác phẩm")
                    }

                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Lọc theo trạng thái
            Text("Trạng thái")
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                // truyện đã hoàn thành
                FilterChip(selected = selectedStatus == "completed",
                    onClick = {
                        selectedStatus = "completed"
                    },
                    label = {
                        Text("Đã hoàn thành")
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                // truyện đang cập nhật
                FilterChip(selected = selectedStatus=="ongoing",
                    onClick = {
                        selectedStatus="ongoing"
                    },
                    label = {
                        Text("Đang cập nhật")
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Lọc sao
            Text("Lọc theo sao")
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                listOf(5,4,3).forEach { star ->
                    FilterChip(selected = selectedStar == star,
                        onClick = {
                            selectedStar = star
                        },
                        label = {
                            Text("⭐ $star sao")
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Nút áp dụng filter
            Button(modifier = Modifier.fillMaxWidth(),
                   onClick = {
                       onApply(selectedType, selectedStatus, selectedStar)
                }
            ) {
                Text("Áp dụng")
            }
            TextButton(

                modifier = Modifier.fillMaxWidth(),

                onClick = {

                    selectedType = "author"

                    selectedStatus = null

                    selectedStar = null

                    onApply(

                        selectedType,

                        null,

                        null

                    )

                    onDismiss()

                }

            ){
                Text("Xóa bộ lọc")
            }
        }
    }
}
