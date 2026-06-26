package com.example.appthemuse.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.appthemuse.ui.viewmodel.AuthViewModel
import com.example.appthemuse.ui.components.PrimaryButton
import com.example.appthemuse.ui.components.SecondaryButton

@Composable
fun VerifyScreen(
    viewModel: AuthViewModel,
    onVerified: () -> Unit,
    onExpired: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(300) }

    // ⏳ Countdown
    LaunchedEffect(Unit) {
        viewModel.sendVerifyEmail()

        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }

        // ❗ hết giờ
        viewModel.deleteAccountIfExpired()
        onExpired()
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Xác nhận email",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Vui lòng kiểm tra email để xác nhận tài khoản.",
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Thời gian còn: $timeFormatted",
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Tôi đã xác nhận",
            onClick = {
                viewModel.checkEmailVerified { verified ->
                    if (verified) {
                        onVerified()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryButton(
            text = "Gửi lại email",
            onClick = {
                viewModel.sendVerifyEmail()
            }
        )
    }

}