package com.example.appthemuse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appthemuse.ui.components.AppBottomBar
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
import com.example.appthemuse.ui.viewmodel.HomeViewModel
import com.example.appthemuse.ui.screens.ExploreScreen
import com.example.appthemuse.ui.screens.ProfileScreen
import com.example.appthemuse.ui.viewmodel.ProfileViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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
            // Lấy chế độ Dark Mode mặc định của hệ thống máy (Android System) để cấu hình ban đầu
            val systemInDarkTheme = isSystemInDarkTheme()

            // Biến trạng thái Theme độc lập ở tầng gốc, giúp Recompose giao diện toàn app lập tức khi đổi trạng thái
            var isDarkTheme by remember { mutableStateOf(systemInDarkTheme) }

            AppTheMuseTheme(darkTheme = isDarkTheme) {
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
                            // Chỉ hiển thị BottomBar ở các màn chính
                            if (currentRoute != "welcome" && currentRoute != "auth_options" &&
                                currentRoute != "login" && currentRoute != "register" && currentRoute != "genre_selection") {
                                AppBottomBar(navController = navController, currentRoute = currentRoute)
                            }
                        }
                    ) { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = "welcome",
                            modifier = Modifier.padding(paddingValues)
                        ) {
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
                        

                            // Trang chủ chính thức
                            composable("home") {
                                com.example.appthemuse.ui.screens.HomeScreen(
                                    viewModel = homeViewModel,
                                    onBookClick = { bookId -> }
                                )
                            }

                            // Trang khám phá
                            composable("explore") {
                                ExploreScreen(viewModel = homeViewModel, onBookClick = {})
                            }

                            // Tủ sách
                            composable("bookshelf") {
                                Surface(modifier = Modifier.fillMaxSize()) {
                                    Text(text = "Màn hình Tủ sách đang phát triển")
                                }
                            }

                            // TÍCH HỢP TRANG HỒ SƠ VỚI VÒNG ĐỜI SCOPED VIEWMODEL CHUẨN
                            composable("profile") {
                                // Khởi tạo ProfileViewModel cô lập đúng nơi, đúng thời điểm!
                                val profileViewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

                                ProfileScreen(
                                    profileViewModel = profileViewModel,
                                    onThemeChanged = { selectedTheme ->
                                        // Cập nhật trạng thái Theme vào chính nó để đồng bộ UI
                                        profileViewModel.updateThemeMode(selectedTheme)
                                        // Đồng thời kích hoạt recompose thay đổi màu nền toàn cục app ngay lập tức
                                        isDarkTheme = (selectedTheme == "Dark")
                                    },
                                    onLogoutClick = {
                                        // Xử lý quay về màn hình đăng nhập và xóa toàn bộ lịch sử trước đó
                                        navController.navigate("welcome") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}