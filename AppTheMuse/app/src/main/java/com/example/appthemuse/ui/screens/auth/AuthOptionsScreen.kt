package com.example.appthemuse.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.R
import com.example.appthemuse.ui.components.GoogleButton
import com.example.appthemuse.ui.components.PrimaryButton
import com.example.appthemuse.ui.components.SecondaryButton
import com.example.appthemuse.ui.viewmodel.AuthState
import com.example.appthemuse.ui.viewmodel.AuthViewModel
import com.example.appthemuse.utils.AuthUtils
import kotlinx.coroutines.launch

@Composable
fun AuthOptionsScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: (Boolean) -> Unit,
    onNavigateToLoginEmail: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authState by viewModel.authState

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoginSuccess -> {
                val hasGenres = (authState as AuthState.LoginSuccess).hasGenres
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onNavigateToHome(hasGenres)
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
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
                modifier = Modifier.fillMaxSize().padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_the_muse),
                    contentDescription = "Logo The Muse",
                    modifier = Modifier.size(50.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Chào mừng bạn\nđến với ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                            append("The Muse")
                        }
                    },
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Hành trình vào thế giới ngôn từ của bạn bắt đầu tại đây.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                GoogleButton(
                    onClick = {
                        coroutineScope.launch {
                            AuthUtils.triggerGoogleSignIn(context, autoSelect = true) { idToken ->
                                viewModel.loginWithGoogle(idToken)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(
                    text = "ĐĂNG NHẬP",
                    onClick = onNavigateToLoginEmail,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                SecondaryButton(
                    text = "TẠO TÀI KHOẢN",
                    onClick = onNavigateToRegister
                )
            }
        }
    }
}