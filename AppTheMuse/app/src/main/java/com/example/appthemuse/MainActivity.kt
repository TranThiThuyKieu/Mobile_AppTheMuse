package com.example.appthemuse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appthemuse.ui.components.AppBottomBar
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appthemuse.data.remote.AuthService
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.data.repository.AuthRepository
import com.example.appthemuse.ui.screens.AuthOptionScreen
import com.example.appthemuse.ui.screens.GenreSelectionScreen
import com.example.appthemuse.ui.screens.LoginScreen
import com.example.appthemuse.ui.screens.RegisterScreen
import com.example.appthemuse.ui.screens.WelcomeScreen
import com.example.appthemuse.ui.theme.AppTheMuseTheme
import com.example.appthemuse.ui.viewmodel.AuthViewModel
import com.example.appthemuse.ui.viewmodel.HomeViewModel
import com.example.appthemuse.ui.screens.ExploreScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. KHỞI TẠO CÁC DEPENDENCY CHUẨN KIẾN TRÚC MỚI
        val authService = AuthService()
        val firestoreService =
            FirestoreService() // Khởi tạo thêm service quản lý danh mục sách/thể loại

        val authRepository = AuthRepository(authService)

        // Truyền Repository và FirestoreService vào ViewModel theo cấu trúc Clean Architecture mới
        val authViewModel = AuthViewModel(
            authRepository = authRepository,
            firestoreService = firestoreService
        )

        setContent {
            AppTheMuseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val homeViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                        HomeViewModel(firestoreService = firestoreService)
                    }
                    // Lấy route hiện tại của Navigation Stack
                    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                    Scaffold(
                        bottomBar = {
                            // Chỉ hiển thị BottomBar ở các màn chính,
                            if (currentRoute != "welcome" && currentRoute != "auth_options" &&
                                currentRoute != "login" && currentRoute != "register" && currentRoute != "genre_selection") {
                                AppBottomBar(navController = navController, currentRoute = currentRoute)
                            }
                        }
                    ) { paddingValues ->
                        NavHost(navController = navController, startDestination = "welcome", modifier = Modifier.padding(paddingValues)) {
                        // Màn hình chào mừng
                        composable("welcome") {
                            WelcomeScreen(onNavigateToLogin = { navController.navigate("auth_options") })
                        }
                        // Màn hình chọn Đăng nhập / Đăng ký tổng quan
                        composable("auth_options") {
                            AuthOptionScreen(
                                onNavigateToGoogleLogin = { /* Sẽ xử lý SDK Google sau */ },
                                onNavigateToLoginEmail = { navController.navigate("login") },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }

                        // Nhánh đăng nhập
                        composable("login") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onNavigateToHome = { hasGenres ->
                                    if (hasGenres) {
                                        navController.navigate("home") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("genre_selection") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }

                        // Nhánh đăng ký tài khoản mới
                        composable("register") {
                            RegisterScreen(
                                viewModel = authViewModel,
                                onRegisterSuccess = {
                                    navController.navigate("genre_selection") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("auth_options")
                                    }
                                }
                            )
                        }
                        // Màn hình chọn thể loại (Chỉ xuất hiện 1 lần đầu)
                        composable("genre_selection") {
                            GenreSelectionScreen(
                                viewModel = authViewModel,
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("genre_selection") { inclusive = true }
                                    }
                                }
                            )
                        }
                        // Trang chủ chính thức ( Jan trang chủ)
                        composable("home") {
                            com.example.appthemuse.ui.screens.HomeScreen(
                                viewModel = homeViewModel,
                                onBookClick = { bookId ->

                                }
                            )
                        }
                         // Trang khám phá
                         composable("explore") {
                                ExploreScreen(viewModel = homeViewModel, onBookClick = {})
                            }
                        }
                    }
                }
            }
        }
    }
}

