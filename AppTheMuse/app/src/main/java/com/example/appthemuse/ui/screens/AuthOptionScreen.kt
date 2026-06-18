package com.example.appthemuse.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun AuthOptionScreen(
    onNavigateToGoogleLogin: () -> Unit,
    onNavigateToLoginEmail: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. LOGO CUỐN SÁCH NHỎ
            Image(
                painter = painterResource(id = R.drawable.ic_logo_the_muse),
                contentDescription = "Logo The Muse",
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. TIÊU ĐỀ ĐƯỢC ĐỔI MÀU CHỮ "THE MUSE" CHUẨN FIGMA
            Text(
                text = buildAnnotatedString {
                    append("Chào mừng bạn\nđến với ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("The Muse")
                    }
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. MÔ TẢ PHỤ (SLOGAN)
            Text(
                text = "Hành trình vào thế giới ngôn từ của bạn bắt đầu tại đây.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 4. NÚT TIẾP TỤC VỚI GOOGLE
            GoogleButton(
                text = "Tiếp tục với Google",
                onClick = onNavigateToGoogleLogin
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. NÚT ĐĂNG NHẬP
            PrimaryButton(
                text = "ĐĂNG NHẬP",
                onClick = onNavigateToLoginEmail,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. NÚT TẠO TÀI KHOẢN
            SecondaryButton(
                text = "TẠO TÀI KHOẢN",
                onClick = onNavigateToRegister
            )
        }
    }
}