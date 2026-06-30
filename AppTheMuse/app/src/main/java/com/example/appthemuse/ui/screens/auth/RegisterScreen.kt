package com.example.appthemuse.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.R
import com.example.appthemuse.ui.components.PrimaryButton
import com.example.appthemuse.ui.viewmodel.AuthState
import com.example.appthemuse.ui.viewmodel.AuthViewModel

// Màn hình Đăng ký tài khoản mới
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val authState by viewModel.authState
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Xử lý gửi mã xác minh hoặc báo lỗi
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.WaitingForVerification -> {
                Toast.makeText(context, "Mã xác minh đã được gửi đến Email!", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // reset trạng thái khi rời khỏi màn hình
    DisposableEffect(Unit) {
        onDispose { viewModel.resetState() }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp).verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Ảnh logo và tên app
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_logo_the_muse), contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "The Muse", style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), color = MaterialTheme.colorScheme.onBackground)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Tạo tài khoản", style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp))
            Text(text = "Đăng ký để bắt đầu hành trình đọc", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

            //form nhập dữ liệu đăng ký
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Tên đăng nhập", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedTextField(value = username, onValueChange = { username = it }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Email", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedTextField(value = email, onValueChange = { email = it }, leadingIcon = { Icon(Icons.Default.Email, null) }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Mật khẩu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedTextField(value = password, onValueChange = { password = it }, visualTransformation = PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Default.Lock, null) }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Xác nhận mật khẩu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, visualTransformation = PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Default.Lock, null) }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(32.dp))

            // Nút xử lý đăng ký
            Box(modifier = Modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator()
                } else {
                    PrimaryButton(text = "Đăng ký", onClick = { viewModel.register(email, password, confirmPassword, username) }, modifier = Modifier.fillMaxWidth())
                }
            }

            // Chuyển hướng sang màn hình đăng nhập
            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "Đã có tài khoản? ", color = MaterialTheme.colorScheme.outline)
                Text(text = "Đăng nhập", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToLogin() })
            }
        }
    }
}