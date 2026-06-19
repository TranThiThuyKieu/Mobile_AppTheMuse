package com.example.appthemuse.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.ui.components.PrimaryButton
import com.example.appthemuse.ui.viewmodel.GenreState
import com.example.appthemuse.ui.viewmodel.GenreViewModel

@Composable
fun GenreSelectionScreen(
    viewModel: GenreViewModel, // ĐỒNG BỘ: Chuyển sang dùng GenreViewModel chuyên trách
    onNavigateToHome: () -> Unit
) {
    // Tải danh sách thể loại sách từ ViewModel (Kiểu List<CategoryModel>)
    val genresList by viewModel.categories
    val genreState by viewModel.genreState

    // Lưu trữ danh sách ID hoặc Tên thể loại được chọn dưới dạng String
    val selectedGenres = rememberSaveable { mutableStateListOf<String>() }
    val context = LocalContext.current

    // Kích hoạt tải dữ liệu khi màn hình được khởi tạo
    LaunchedEffect(Unit) {
        viewModel.fetchCategories()
    }

    // Lắng nghe trạng thái cập nhật sở thích để điều hướng
    LaunchedEffect(genreState) {
        when (genreState) {
            is GenreState.Success -> {
                Toast.makeText(context, "Chào mừng bạn đến với thế giới sách!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onNavigateToHome()
            }
            is GenreState.Error -> {
                Toast.makeText(context, (genreState as GenreState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Bạn thích thể loại nào?", style = MaterialTheme.typography.titleLarge, fontSize = 28.sp)
            Text(
                text = "Chọn ít nhất 3 thể loại để chúng mình gợi ý sách chuẩn gu bạn nhé.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Hiển thị trạng thái Loading khi danh sách trống hoặc đang gửi dữ liệu lên Firebase
            if (genresList.isEmpty() || genreState is GenreState.Loading) {
                Box(modifier = Modifier.weight(1.0f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1.0f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(genresList) { genre ->
                        val isSelected = selectedGenres.contains(genre.name)
                        val boxBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(boxBg, shape = RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isSelected) {
                                        selectedGenres.remove(genre.name)
                                    } else {
                                        selectedGenres.add(genre.name)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = genre.name, color = textColor, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút bấm xác nhận lựa chọn
            PrimaryButton(
                text = "Tiếp tục (${selectedGenres.size}/3)",
                onClick = {
                    if (selectedGenres.size >= 3) {
                        // CHUẨN KIẾN TRÚC: Không gọi FirebaseAuth trực tiếp tại đây,
                        // logic lấy UID đã được xử lý gọn gàng bên trong GenreViewModel của bạn rồi!
                        viewModel.saveFavoriteGenres(selectedGenres.toList())
                    } else {
                        Toast.makeText(context, "Vui lòng chọn tối thiểu 3 thể loại!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}