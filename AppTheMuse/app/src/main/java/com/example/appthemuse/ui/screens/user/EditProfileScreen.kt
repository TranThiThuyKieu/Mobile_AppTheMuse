package com.example.appthemuse.ui.screens.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appthemuse.ui.viewmodel.EditProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Tự động tải lại dữ liệu mới nhất mỗi khi màn hình được kích hoạt
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // Xử lý thông báo Toast khi Lưu Thành Công hoặc Có Lỗi xảy ra
    LaunchedEffect(uiState.isSaveSuccess, uiState.errorMessage) {
        if (uiState.isSaveSuccess) {
            Toast.makeText(context, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
            viewModel.resetSaveState()
            onBackClick() // Quay về màn hình Profile sau khi lưu thành công
        }
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.resetSaveState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thông tin cá nhân",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF7C4DFF))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // --- KHU VỰC AVATAR ---
                AsyncImage(
                    model = if (uiState.userForm.avatarUrl.isNotEmpty()) uiState.userForm.avatarUrl
                            else "https://ui-avatars.com/api/?name=${uiState.userForm.username}&background=random&size=200",
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- TÊN USERNAME DƯỚI AVATAR ---
                // Đồng bộ thay đổi text trực tiếp qua hàm gõ ô Username
                BasicEditField(
                    value = uiState.userForm.username,
                    onValueChange = { viewModel.onUsernameChange(it) },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- TIÊU ĐỀ PHÂN KHU THÔNG TIN ---
                Text(
                    text = "THÔNG TIN CHI TIẾT",
                    color = Color(0xFF3F51B5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- CÁC Ô NHẬP LIỆU (THEO ĐÚNG ẢNH MẪU) ---
                EditInputField(
                    label = "HỌ VÀ TÊN",
                    value = uiState.userForm.fullName,
                    onValueChange = { viewModel.onFullNameChange(it) }
                )

                EditInputField(
                    label = "EMAIL",
                    value = uiState.userForm.email,
                    onValueChange = {},
                    enabled = false // Email thường cố định theo tài khoản Auth, khóa lại không cho sửa trực tiếp
                )

                EditInputField(
                    label = "SỐ ĐIỆN THOẠI",
                    value = uiState.userForm.phoneNumber,
                    onValueChange = { viewModel.onPhoneChange(it) }
                )

                EditInputField(
                    label = "NGÀY SINH",
                    value = uiState.userForm.birthday,
                    onValueChange = { viewModel.onBirthdayChange(it) }
                )

                EditInputField(
                    label = "GIỚI TÍNH",
                    value = uiState.userForm.gender,
                    onValueChange = { viewModel.onGenderChange(it) }
                )

                Spacer(modifier = Modifier.height(40.dp))

                // --- NÚT LƯU THAY ĐỔI ---
                Button(
                    onClick = { viewModel.saveChanges() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7986CB)), // Màu tím pastel bo góc mịn giống ảnh mẫu
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Lưu thay đổi", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- NÚT XÓA TÀI KHOẢN ---
                TextButton(
                    onClick = { /* Xử lý các logic cảnh báo/xóa tài khoản ở đây */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "XÓA TÀI KHOẢN",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Component ô nhập có nhãn viết hoa đặt ở trên theo mẫu layout
@Composable
fun EditInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFFE0E0E0),
                unfocusedIndicatorColor = Color(0xFFF0F0F0),
                disabledIndicatorColor = Color(0xFFF5F5F5),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )
    }
}

// Component bổ trợ để nhập nhanh text dưới avatar không cần khung viền thô cứng
@Composable
fun BasicEditField(
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onBackground),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        singleLine = true
    )
}
