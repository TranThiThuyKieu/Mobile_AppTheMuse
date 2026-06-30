package com.example.appthemuse.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.ui.components.PrimaryButton
import com.example.appthemuse.ui.components.SecondaryButton
import com.example.appthemuse.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

// Màn hình chờ xác minh Email sau khi đăng ký
@Composable
fun VerificationWaitScreen(
    viewModel: AuthViewModel,
    onNavigateToGenres: () -> Unit,
    onCancelVerification: () -> Unit
) {
    val context = LocalContext.current

    var timeLeft by remember { mutableStateOf(300) } // Đếm ngược 5 phút
    var resendCooldown by remember { mutableStateOf(60) } // Chờ 60s mới được gửi lại mail

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    // thoát ra thì xóa acc đang đăng ký dở
    BackHandler {
        viewModel.deleteAccountIfExpired { onCancelVerification() }
    }

    // Đếm ngược 5 phút, hết giờ thì xóa acc
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else {
            viewModel.deleteAccountIfExpired {
                Toast.makeText(context, "Hết thời gian! Vui lòng đăng ký lại.", Toast.LENGTH_LONG).show()
                onCancelVerification()
            }
        }
    }

    // Đếm ngược 60s cho nút gửi lại
    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000)
            resendCooldown--
        }
    }

    // check xem user đã click link xác minh chưa
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            viewModel.checkEmailVerified {
                Toast.makeText(context, "Xác minh thành công!", Toast.LENGTH_SHORT).show()
                onNavigateToGenres()
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Xác nhận Email", style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Chúng mình đã gửi link đến Email của bạn. Hãy kích hoạt trong vòng 5 phút nhé.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))

            // Hiển thị đồng hồ đếm ngược
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Thời gian xác minh còn lại:", color = MaterialTheme.colorScheme.outline)
            Text(
                text = timeString,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 54.sp, fontWeight = FontWeight.Bold),
                color = if (timeLeft < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()

            // Nút Gửi lại và Hủy
            Spacer(modifier = Modifier.height(40.dp))
            PrimaryButton(
                text = if (resendCooldown > 0) "GỬI LẠI SAU (${resendCooldown}s)" else "GỬI LẠI EMAIL XÁC MINH",
                onClick = {
                    if (resendCooldown > 0) return@PrimaryButton
                    viewModel.sendVerifyEmail()
                    resendCooldown = 60
                    timeLeft = 300
                    Toast.makeText(context, "Đã gửi lại email!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            SecondaryButton(
                text = "HỦY",
                onClick = { viewModel.deleteAccountIfExpired { onCancelVerification() } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}