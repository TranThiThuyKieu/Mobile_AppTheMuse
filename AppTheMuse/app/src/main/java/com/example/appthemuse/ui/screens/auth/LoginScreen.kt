package com.example.appthemuse.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.R
import com.example.appthemuse.ui.components.GoogleButton
import com.example.appthemuse.ui.components.PrimaryButton
import com.example.appthemuse.ui.viewmodel.AuthState
import com.example.appthemuse.ui.viewmodel.AuthViewModel
import com.example.appthemuse.utils.AuthUtils
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: (Boolean) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState
    
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoginSuccess -> {
                val successState = authState as AuthState.LoginSuccess
                Toast.makeText(context, "Chào mừng ${successState.user.username} trở lại!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                if (successState.user.role == "admin") {
                    onNavigateToAdmin()
                } else {
                    onNavigateToHome(successState.hasGenres)
                }
            }
            is AuthState.PasswordResetSent -> {
                Toast.makeText(context, "Link đặt lại mật khẩu đã được gửi vào Email của bạn!", Toast.LENGTH_LONG).show()
                viewModel.resetState()
                showForgotDialog = false
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Quên mật khẩu") },
            text = {
                Column {
                    Text("Nhập email để nhận link đặt lại mật khẩu (Giới hạn 5 lần/ngày).")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.forgotPassword(forgotEmail) }) {
                    Text("Gửi link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (authState is AuthState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_the_muse),
                        contentDescription = "Logo",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "The Muse", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Chào mừng trở lại", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = "Đăng nhập để tiếp tục đọc truyện", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email hoặc tên đăng nhập") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        text = "Quên mật khẩu?",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp).clickable { 
                            forgotEmail = email
                            showForgotDialog = true 
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                PrimaryButton(
                    text = "Đăng nhập",
                    onClick = {
                        viewModel.login(email.trim(), password.trim())
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Hoặc đăng nhập với", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(16.dp))

                GoogleButton(
                    onClick = {
                        coroutineScope.launch {
                            val idToken = AuthUtils.triggerGoogleSignIn(context, autoSelect = false)
                            if (idToken != null) {
                                viewModel.loginWithGoogle(idToken)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    Text(text = "Chưa có tài khoản? ")
                    Text(
                        text = "Đăng ký ngay",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}
