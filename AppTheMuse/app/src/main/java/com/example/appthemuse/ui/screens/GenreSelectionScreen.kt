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
import androidx.compose.runtime.saveable.rememberSaveable // IMPORT ĐỂ GIỮ STATE
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.ui.components.PrimaryButton
import com.example.appthemuse.ui.viewmodel.AuthState
import com.example.appthemuse.ui.viewmodel.AuthViewModel

@Composable
fun GenreSelectionScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit
) {
    val genresList by viewModel.categories

    // Tối ưu hóa: Dùng rememberSaveable kết hợp với bộ convert list để tránh mất dữ liệu khi xoay màn hình
    val selectedGenres = rememberSaveable { mutableStateListOf<String>() }
    val context = LocalContext.current
    val authState by viewModel.authState

    LaunchedEffect(Unit) {
        viewModel.fetchCategories()
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.GenresUpdated) {
            Toast.makeText(context, "Chào mừng bạn đến với thế giới sách!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onNavigateToHome()
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

            if (genresList.isEmpty()) {
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
                        val isSelected = selectedGenres.contains(genre)
                        val boxBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(boxBg, shape = RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isSelected) selectedGenres.remove(genre) else selectedGenres.add(genre)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = genre, color = textColor, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            PrimaryButton(
                text = "Tiếp tục (${selectedGenres.size}/3)",
                onClick = {
                    if (selectedGenres.size >= 3) {
                        viewModel.saveFavoriteGenres(selectedGenres.toList())
                    } else {
                        Toast.makeText(context, "Vui lòng chọn tối thiểu 3 thể loại!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}