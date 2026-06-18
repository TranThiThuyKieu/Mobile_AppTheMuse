package com.example.appthemuse

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appthemuse.ui.screens.WelcomeScreen
import com.example.appthemuse.ui.theme.AppTheMuseTheme // IMPORT THEME

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // MaterialTheme mặc định thành Theme của The Muse
            AppTheMuseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    // colorScheme.background mới nhận diện đúng màu BackgroundLight/Dark
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "welcome") {
                        composable("welcome") {
                            WelcomeScreen(
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("login") {
                            Toast.makeText(this@MainActivity, "Chuyển sang màn Đăng Nhập thành công!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}