package com.example.appthemuse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appthemuse.data.remote.AuthService
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.data.repository.AuthRepository
import com.example.appthemuse.ui.screens.*
import com.example.appthemuse.ui.theme.AppTheMuseTheme
import com.example.appthemuse.ui.viewmodel.AuthViewModel
import com.example.appthemuse.ui.viewmodel.GenreViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Khởi tạo Service (Tầng Remote)
        val authService = AuthService()
        val firestoreService = FirestoreService()

        // 2. Khởi tạo Repository duy nhất quản lý Auth/User
        val authRepository = AuthRepository(authService)

        // 3. Sử dụng ViewModelProvider.Factory để tạo 2 ViewModel riêng biệt chuẩn Jetpack
        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                        AuthViewModel(authRepository) as T
                    modelClass.isAssignableFrom(GenreViewModel::class.java) ->
                        GenreViewModel(authRepository, firestoreService) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        val authViewModel by viewModels<AuthViewModel> { viewModelFactory }
        val genreViewModel by viewModels<GenreViewModel> { viewModelFactory }

        setContent {
            AppTheMuseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "welcome") {
                        // Màn hình chào mừng
                        composable("welcome") {
                            WelcomeScreen(onNavigateToLogin = { navController.navigate("auth_options") })
                        }

                        // Màn hình các tùy chọn Đăng nhập/Đăng ký tổng quan
                        composable("auth_options") {
                            AuthOptionScreen(
                                viewModel = authViewModel,
                                onNavigateToHome = { hasGenres ->
                                    if (hasGenres) {
                                        navController.navigate("home") { popUpTo("welcome") { inclusive = true } }
                                    } else {
                                        navController.navigate("genre_selection") { popUpTo("welcome") { inclusive = true } }
                                    }
                                },
                                onNavigateToLoginEmail = { navController.navigate("login") },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }

                        // Màn hình Đăng nhập bằng Email
                        composable("login") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onNavigateToHome = { hasGenres ->
                                    if (hasGenres) {
                                        navController.navigate("home") { popUpTo("welcome") { inclusive = true } }
                                    } else {
                                        navController.navigate("genre_selection") { popUpTo("welcome") { inclusive = true } }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }

                        // Màn hình Đăng ký tài khoản mới
                        composable("register") {
                            RegisterScreen(
                                viewModel = authViewModel,
                                onRegisterSuccess = {
                                    navController.navigate("genre_selection") { popUpTo("welcome") { inclusive = true } }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login") { popUpTo("auth_options") }
                                }
                            )
                        }

                        // Màn hình chọn Thể loại yêu thích (Đã chuyển sang dùng GenreViewModel mới)
                        composable("genre_selection") {
                            GenreSelectionScreen(
                                viewModel = genreViewModel, //Truyền đúng GenreViewModel chuyên trách
                                onNavigateToHome = {
                                    navController.navigate("home") { popUpTo("genre_selection") { inclusive = true } }
                                }
                            )
                        }

                        // Màn hình chính (Home)
                        composable("home") {
                            Surface(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Chào mừng bạn đến với trang chủ The Muse!",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}