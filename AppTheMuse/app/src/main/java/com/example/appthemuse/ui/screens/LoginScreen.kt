package com.example.appthemuse.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.R
import com.example.appthemuse.ui.components.GoogleButton
import com.example.appthemuse.ui.components.PrimaryButton
import com.example.appthemuse.ui.viewmodel.AuthState
import com.example.appthemuse.ui.viewmodel.AuthViewModel
import com.example.appthemuse.ui.util.AuthUtils
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: (Boolean) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState

    LaunchedEffect(authState) {
        if (authState is AuthState.LoginSuccess) {
            val hasGenres = (authState as AuthState.LoginSuccess).hasGenres
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onNavigateToHome(hasGenres)
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
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
                    Text(text = "The Muse", style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Chào mừng trở lại", style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Đăng nhập để tiếp tục đọc truyện", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp), color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email của bạn") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "Đăng nhập",
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Hoặc đăng nhập với", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))

                GoogleButton(
                    onClick = {
                        coroutineScope.launch {
                            AuthUtils.triggerGoogleSignIn(context, autoSelect = false) { idToken ->
                                viewModel.loginWithGoogle(idToken)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row {
                    Text(text = "Chưa có tài khoản? ", color = MaterialTheme.colorScheme.onBackground)
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