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

@Composable
fun VerificationWaitScreen(
    viewModel: AuthViewModel,
    onNavigateToGenres: () -> Unit,
    onCancelVerification: () -> Unit
) {
    val context = LocalContext.current

    // 1. Đếm ngược 5 phút = 300 giây
    var timeLeft by remember { mutableStateOf(300) }
    
    // 2. Trạng thái cooldown của nút Gửi lại
    var resendCooldown by remember { mutableStateOf(60) }

    // Bộ chuyển đổi số giây thành định dạng mm:ss
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    // CHẶN NÚT BACK VẬT LÝ: Xóa tài khoản nếu thoát ra ngoài
    BackHandler {
        viewModel.deleteAccountIfExpired {
            onCancelVerification()
        }
    }

    // LUỒNG 1: Vòng lặp đếm ngược 5 phút & Tự động Xóa tài khoản khi hết giờ
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else {
            viewModel.deleteAccountIfExpired {
                Toast.makeText(context, "Hết thời gian xác minh. Tài khoản đã bị hủy, vui lòng đăng ký lại!", Toast.LENGTH_LONG).show()
                onCancelVerification()
            }
        }
    }

    // LUỒNG 2: Giảm thời gian chờ giữa các lần bấm nút Gửi lại (Cooldown)
    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000)
            resendCooldown--
        }
    }

    // LUỒNG 3: Vòng lặp kiểm tra link kích hoạt (Mỗi 4 giây kiểm tra một lần)
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            viewModel.checkEmailVerified(
                onVerified = {
                    Toast.makeText(context, "Xác minh thành công!", Toast.LENGTH_SHORT).show()
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
                text = "Xác nhận Email",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 26.sp, 
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chúng mình đã gửi một liên kết xác minh đến Email của bạn. Vui lòng kích hoạt trong vòng 5 phút.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // HIỂN THỊ ĐỒNG HỒ ĐẾM NGƯỢC
            Text(
                text = "Thời gian xác minh còn lại:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = timeString,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (timeLeft < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // NÚT GỬI LẠI MÃ
            PrimaryButton(
                text = if (resendCooldown > 0) "GỬI LẠI SAU (${resendCooldown}s)" else "GỬI LẠI EMAIL XÁC MINH",
                onClick = {
                    if (resendCooldown > 0) return@PrimaryButton

                    try {
                        viewModel.sendVerifyEmail()
                        resendCooldown = 60 // Kích hoạt đếm ngược 60 giây chờ gửi lại
                        timeLeft = 300      // Reset đồng hồ xác minh về 5 phút
                        Toast.makeText(context, "Đã gửi lại email xác minh thành công!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi kết nối hệ thống, vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // NÚT HỦY (Xóa tài khoản đang đăng ký dở)
            SecondaryButton(
                text = "HỦY VÀ THOÁT",
                onClick = {
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
