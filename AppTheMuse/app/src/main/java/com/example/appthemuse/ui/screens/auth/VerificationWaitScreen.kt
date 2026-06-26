package com.example.appthemuse.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appthemuse.ui.components.PrimaryButton
import com.example.appthemuse.ui.components.SecondaryButton
import com.example.appthemuse.ui.viewmodel.AuthState
import com.example.appthemuse.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun VerificationWaitScreen(
    viewModel: AuthViewModel,
    onNavigateToGenres: () -> Unit,
    onCancelVerification: () -> Unit
) {
    val context = LocalContext.current
    val authState by viewModel.authState

    // ✅ CHẶN BACK VẬT LÝ: Bắt buộc kích hoạt xóa tài khoản tạm thời nếu bấm nút thoát vật lý
    BackHandler {
        viewModel.deleteAccountIfExpired {
            onCancelVerification()
        }
    }

    // ✅ VÒNG LẶP KIỂM TRA ĐỊNH KỲ: Check liên kết xác minh mỗi 3 giây
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            viewModel.checkEmailVerified(
                onVerified = {
                    onNavigateToGenres()
                }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Xác nhận Email của bạn",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chúng mình đã gửi một liên kết xác nhận đến Email đăng ký. Vui lòng kiểm tra hộp thư (hoặc thư rác) và nhấn vào liên kết để kích hoạt tài khoản.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(40.dp))

            PrimaryButton(
                text = "GỬI LẠI EMAIL XÁC MINH",
                onClick = {
                    viewModel.sendVerifyEmail()
                    Toast.makeText(context, "Đã gửi lại email xác minh!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryButton(
                text = "HỦY ĐĂNG KÝ",
                onClick = {
                    // Người dùng chủ động hủy -> Tiến hành dọn sạch Document và Auth rác
                    viewModel.deleteAccountIfExpired {
                        Toast.makeText(context, "Đã hủy tiến trình đăng ký.", Toast.LENGTH_SHORT).show()
                        onCancelVerification()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}