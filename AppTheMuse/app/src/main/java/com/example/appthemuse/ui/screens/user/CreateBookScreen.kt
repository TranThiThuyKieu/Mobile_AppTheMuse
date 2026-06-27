package com.example.appthemuse.ui.screens.user.creator_studio

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import com.example.appthemuse.domain.model.Category
import com.example.appthemuse.ui.viewmodel.CreateBookViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateBookScreen(
    viewModel: CreateBookViewModel,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAppInDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val themeColors = if (!isAppInDarkMode) {
        CreatorThemeColors(
            backgroundColor = Color(0xFFF9F9FB),
            cardColor = Color(0xFFFFFFFF),
            titleTextColor = Color(0xFF1A2536),
            contentTextColor = Color(0xFF5A677D),
            accentColor = Color(0xFF6B66FF),
            positiveColor = Color(0xFFE8505B),
            infoColor = Color(0xFF4A90E2)
        )
    } else {
        CreatorThemeColors(
            backgroundColor = Color(0xFF0F1524),
            cardColor = Color(0xFF1E2638),
            titleTextColor = Color.White,
            contentTextColor = Color.LightGray,
            accentColor = Color(0xFF6B66FF),
            positiveColor = Color(0xFFFF6B6B),
            infoColor = Color(0xFF64B5F6)
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (bitmap != null) {
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

                        withContext(Dispatchers.Main) {
                            // Truyền uri (để hiển thị) và base64 (để upload)
                            viewModel.onImageSelected(selectedUri.toString(), base64)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Đăng tác phẩm mới", color = themeColors.titleTextColor, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = themeColors.accentColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.backgroundColor)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = themeColors.backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- ẢNH BÌA ---
            Text("ẢNH BÌA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.contentTextColor, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isAppInDarkMode) Color(0xFF252A3A) else Color(0xFFF0F0F5))
                    .clickable { launcher.launch("image/*") }, // Mở thư viện ảnh
                contentAlignment = Alignment.Center
            ) {
                // Vẽ viền
                Box(modifier = Modifier.fillMaxSize().border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(12.dp)))

                if (uiState.coverImageUri != null) {
                    AsyncImage(
                        model = uiState.coverImageUri,
                        contentDescription = "Cover Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = themeColors.contentTextColor, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tải lên ảnh bìa", color = themeColors.contentTextColor, fontSize = 13.sp)
                        Text("(Tỷ lệ 2:3)", color = themeColors.contentTextColor.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TÊN TÁC PHẨM ---
            Text("TÊN TÁC PHẨM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.contentTextColor, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                placeholder = { Text("Nhập tên câu chuyện của bạn...", color = Color.Gray, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = if (isAppInDarkMode) Color(0xFF1E2638) else Color(0xFFF4F6FA),
                    focusedContainerColor = if (isAppInDarkMode) Color(0xFF1E2638) else Color(0xFFF4F6FA),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = themeColors.accentColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- THỂ LOẠI ---
            Text("THỂ LOẠI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.contentTextColor, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Thay FlowRow bằng Row có scroll ngang cho an toàn tuyệt đối trên mọi phiên bản Compose
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.categories.forEach { category ->
                    val isSelected = uiState.selectedCategory?.id == category.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) themeColors.accentColor else (if (isAppInDarkMode) Color(0xFF1E2638) else Color(0xFFE2E2E2)))
                            .clickable { viewModel.toggleCategory(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category.name,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else themeColors.titleTextColor
                        )
                    }
                }

                // Nút +
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isAppInDarkMode) Color(0xFF1E2638) else Color(0xFFE2E2E2))
                        .clickable { /* TODO */ }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("+", fontSize = 13.sp, color = themeColors.accentColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TÓM TẮT ---
            Text("TÓM TẮT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.contentTextColor, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                placeholder = { Text("Viết vài lời dẫn dắt người đọc vào thế giới của bạn...", color = Color.Gray, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = if (isAppInDarkMode) Color(0xFF1E2638) else Color(0xFFF4F6FA),
                    focusedContainerColor = if (isAppInDarkMode) Color(0xFF1E2638) else Color(0xFFF4F6FA),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = themeColors.accentColor
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- NÚT BẤM ---

            Button(
                onClick = { viewModel.publishBook() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accentColor),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Đăng tác phẩm", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}