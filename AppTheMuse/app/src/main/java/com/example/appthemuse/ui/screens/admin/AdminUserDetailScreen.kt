package com.example.appthemuse.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appthemuse.domain.model.AdminBook
import com.example.appthemuse.ui.model.UserUi
import com.example.appthemuse.ui.viewmodel.AdminUserDetailViewModel

private val AdminPrimary = Color(0xFF6C63FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    userId: String,
    viewModel: AdminUserDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        viewModel.loadUserDetail(userId)
    }

    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết người dùng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AdminPrimary)
            }
        } else if (uiState.user != null) {
            val user = uiState.user!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { UserInfoCard(user, viewModel) }
                
                item {
                    Text(
                        text = "Truyện đã đăng (${uiState.publishedBooks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.publishedBooks.isEmpty()) {
                    item {
                        Text(
                            "Người dùng này chưa đăng truyện nào.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(uiState.publishedBooks) { book ->
                        UserPublishedBookRow(book)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.resetPassword(user.email) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Gửi link đổi mật khẩu")
                    }
                }
            }
        }
    }
}

@Composable
fun UserInfoCard(user: UserUi, viewModel: AdminUserDetailViewModel) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(user.fullName) }
    var editedUsername by remember { mutableStateOf(user.username) }
    var editedPhone by remember { mutableStateOf(user.phoneNumber) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AdminPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AdminPrimary, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.username, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(user.email, color = Color.Gray, fontSize = 14.sp)
                }
                IconButton(onClick = { isEditing = !isEditing }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AdminPrimary)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = editedUsername,
                    onValueChange = { editedUsername = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Họ tên") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editedPhone,
                    onValueChange = { editedPhone = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.updateUserProfile(user.copy(
                            username = editedUsername,
                            fullName = editedName,
                            phoneNumber = editedPhone
                        ))
                        isEditing = false
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Lưu thay đổi")
                }
            } else {
                DetailRow("Họ tên", user.fullName.ifEmpty { "Chưa cập nhật" })
                DetailRow("Số điện thoại", user.phoneNumber.ifEmpty { "Chưa cập nhật" })
                DetailRow("Ngày sinh", user.birthday.ifEmpty { "Chưa cập nhật" })
                DetailRow("Giới tính", user.gender.ifEmpty { "Chưa cập nhật" })
                DetailRow("Vai trò", user.role.uppercase())
                DetailRow("Trạng thái", if (user.isBlocked) "Bị khóa" else "Hoạt động", color = if (user.isBlocked) Color.Red else Color(0xFF22C55E))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = color)
    }
}

@Composable
fun UserPublishedBookRow(book: AdminBook) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp, 60.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(book.title, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("Trạng thái: ${book.status.value}", fontSize = 12.sp, color = Color.Gray)
                Text("${book.viewCount} lượt xem", fontSize = 12.sp, color = AdminPrimary)
            }
        }
    }
}
