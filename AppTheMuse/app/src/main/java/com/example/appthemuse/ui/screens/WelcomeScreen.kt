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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appthemuse.R
import com.example.appthemuse.ui.components.PrimaryButton

@Composable
fun WelcomeScreen(onNavigateToLogin: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. LOGO TRÊN CÙNG
            Image(
                painter = painterResource(id = R.drawable.ic_logo_the_muse),
                contentDescription = "Logo The Muse",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            // 2. TÊN THƯƠNG HIỆU - Gọi displayLarge đã định nghĩa trong Type.kt
            Text(
                text = "THE MUSE",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. SLOGAN - Gọi bodyLarge đã định nghĩa trong Type.kt
            Text(
                text = "Khơi nguồn cảm hứng đọc sách trong bạn",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 4. NÚT BẤM DÙNG CHUNG TỪ COMPONENTS
            PrimaryButton(
                text = "Bắt đầu khám phá",
                onClick = onNavigateToLogin
            )
        }
    }
}