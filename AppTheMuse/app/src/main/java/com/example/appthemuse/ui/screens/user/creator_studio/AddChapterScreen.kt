package com.example.appthemuse.ui.screens.user.creator_studio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.ui.viewmodel.AddChapterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChapterScreen(
    bookId: String,
    chapterNumber: Int,       // Số thứ tự chương tiếp theo
    viewModel: AddChapterViewModel,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAppInDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Màu sắc theo thiết kế (nền trắng nhẹ giống ảnh)
    val bgColor = if (isAppInDarkMode) Color(0xFF0F1524) else Color(0xFFFAFAFC)
    val titleColor = if (isAppInDarkMode) Color.White else Color(0xFF1A2536)
    val hintColor = if (isAppInDarkMode) Color(0xFF6B7A99) else Color(0xFFB0BBC9)
    val contentColor = if (isAppInDarkMode) Color.LightGray else Color(0xFF3A4558)
    val accentColor = Color(0xFF5D5FEF)
    val toolbarBgColor = if (isAppInDarkMode) Color(0xFF1E2638) else Color(0xFFF0F0F5)
    val dividerColor = if (isAppInDarkMode) Color(0xFF2E3A52) else Color(0xFFE8E8F0)

    // Điều hướng khi thành công
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetState()
            onSuccess()
        }
    }

    // Hiển thị lỗi
    if (uiState.error != null) {
        LaunchedEffect(uiState.error) {
            // Error sẽ hiển thị qua Snackbar bên dưới
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = bgColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Header nhỏ gọn giống ảnh: X | THE MUSE | Đăng tải
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút X đóng
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Logo "THE MUSE"
                Text(
                    text = "THE MUSE",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    color = titleColor
                )

                // Nút Đăng tải
                Button(
                    onClick = { viewModel.publishChapter(bookId) },
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Đăng tải",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Thanh công cụ định dạng văn bản ở dưới cùng
            Column {
                Divider(color = dividerColor, thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(toolbarBgColor)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Bold
                    FormatToolbarButton(
                        onClick = { /* TODO: Bold */ },
                        contentColor = contentColor
                    ) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold",
                            tint = contentColor, modifier = Modifier.size(20.dp))
                    }

                    // Italic
                    FormatToolbarButton(
                        onClick = { /* TODO: Italic */ },
                        contentColor = contentColor
                    ) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic",
                            tint = contentColor, modifier = Modifier.size(20.dp))
                    }

                    // Căn trái
                    FormatToolbarButton(
                        onClick = { /* TODO: Align Left */ },
                        contentColor = contentColor
                    ) {
                        Icon(Icons.Default.FormatAlignLeft, contentDescription = "Align Left",
                            tint = contentColor, modifier = Modifier.size(20.dp))
                    }

                    // Căn giữa
                    FormatToolbarButton(
                        onClick = { /* TODO: Align Center */ },
                        contentColor = contentColor
                    ) {
                        Icon(Icons.Default.FormatAlignCenter, contentDescription = "Align Center",
                            tint = contentColor, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Số từ
                    Text(
                        text = "${uiState.wordCount}",
                        fontSize = 13.sp,
                        color = hintColor,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Nút chèn ảnh
                    FormatToolbarButton(
                        onClick = { /* TODO: Chèn ảnh */ },
                        contentColor = contentColor
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Chèn ảnh",
                            tint = contentColor, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Badge số chương
            Box(
                modifier = Modifier
                    .background(
                        color = accentColor.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "CHƯƠNG ${String.format("%02d", chapterNumber)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ô nhập tiêu đề
            BasicTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                textStyle = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    lineHeight = 34.sp
                ),
                cursorBrush = SolidColor(accentColor),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (uiState.title.isEmpty()) {
                        Text(
                            text = "Nhập tiêu đề chương...",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = hintColor,
                            lineHeight = 34.sp
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Đường phân cách mờ
            Divider(color = dividerColor, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(24.dp))

            // Ô nhập nội dung chương
            BasicTextField(
                value = uiState.content,
                onValueChange = { viewModel.onContentChange(it) },
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    color = contentColor,
                    lineHeight = 28.sp,
                    letterSpacing = 0.3.sp
                ),
                cursorBrush = SolidColor(accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 400.dp),
                decorationBox = { innerTextField ->
                    if (uiState.content.isEmpty()) {
                        Text(
                            text = "Bắt đầu câu chuyện của bạn tại đây...",
                            fontSize = 17.sp,
                            color = hintColor,
                            lineHeight = 28.sp
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun FormatToolbarButton(
    onClick: () -> Unit,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
