package com.example.appthemuse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appthemuse.data.remote.AuthService
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.data.repository.AuthRepository
import com.example.appthemuse.ui.components.AppBottomBar
import com.example.appthemuse.ui.screens.*
import com.example.appthemuse.ui.theme.AppTheMuseTheme
import com.example.appthemuse.ui.viewmodel.AuthViewModel
import com.example.appthemuse.ui.viewmodel.GenreSelectionViewModel
import com.example.appthemuse.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authService = AuthService()
        val firestoreService = FirestoreService()
        val authRepository = AuthRepository(authService)

        // Cấu hình Factory để khởi tạo ViewModel có tham số chuẩn xác
        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(authRepository) as T
                    modelClass.isAssignableFrom(GenreSelectionViewModel::class.java) -> GenreSelectionViewModel() as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        val authViewModel by viewModels<AuthViewModel> { viewModelFactory }

        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemInDarkTheme) }

            AppTheMuseTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                    Scaffold(
                        bottomBar = {
                            // Ẩn thanh BottomBar ở toàn bộ luồng Welcome và Auth để tránh lỗi giao diện
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
                            // 1. Màn hình Welcome (Sửa đổi theo hàm nhận gốc của bạn: onNavigateToLogin)
                            composable("welcome") {
                                WelcomeScreen(
                                    onNavigateToLogin = {
                                        navController.navigate("auth_options") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // 2. Màn hình Lựa chọn Đăng nhập/Đăng ký
                            composable("auth_options") {
                                AuthOptionsScreen(
                                    onNavigateToLogin = { navController.navigate("login") },
                                    onNavigateToRegister = { navController.navigate("register") }
                                )
                            }

                            // 3. Màn hình Đăng nhập
                            composable("login") {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onNavigateToHome = { hasGenres ->
                                        if (hasGenres) {
                                            navController.navigate("home") { popUpTo("auth_options") { inclusive = true } }
                                        } else {
                                            navController.navigate("genre_selection") { popUpTo("auth_options") { inclusive = true } }
                                        }
                                    },
                                    onNavigateToRegister = {
                                        navController.navigate("register") { launchSingleTop = true }
                                    }
                                )
                            }

                            // 4. Màn hình Đăng ký (Đồng bộ chính xác Event callback thành công)
                            composable("register") {
                                RegisterScreen(
                                    viewModel = authViewModel,
                                    onRegisterSuccess = {
                                        navController.navigate("genre_selection") { popUpTo("auth_options") { inclusive = true } }
                                    },
                                    onNavigateToLogin = {
                                        navController.navigate("login") { launchSingleTop = true }
                                    }
                                )
                            }

                            // 5. Màn hình Chọn thể loại yêu thích
                            composable("genre_selection") {
                                GenreSelectionScreen(
                                    onNavigateToHome = {
                                        navController.navigate("home") { popUpTo("genre_selection") { inclusive = true } }
                                    }
                                )
                            }

                            // 6. Màn hình Trang chủ (Bổ sung tham số chuyển thể loại bị thiếu)
                            composable("home") {
                                HomeScreen(
                                    viewModel = homeViewModel,
                                    onBookClick = { bookId ->
                                        navController.navigate("book_detail/$bookId")
                                    },
                                    onSeeMoreCategoriesClick = {
                                        navController.navigate("explore") { launchSingleTop = true }
                                    }
                                )
                            }

                            // 7. Màn hình Khám phá
                            composable("explore") {
                                ExploreScreen(
                                    viewModel = homeViewModel,
                                    onBookClick = { bookId ->
                                        navController.navigate("book_detail/$bookId")
                                    },
                                    onCategoryClick = { categoryId ->
                                        navController.navigate("category_books/$categoryId")
                                    }
                                )
                            }

                            // 8. Màn hình Tủ sách cá nhân
                            composable("bookshelf") {
                                BookshelfScreen(
                                    onBookClick = { bookId ->
                                        navController.navigate("book_detail/$bookId")
                                    }
                                )
                            }

                            // 9. Màn hình Hồ sơ cá nhân (Đồng bộ xử lý Logout an toàn qua State)
                            composable("profile") {
                                ProfileScreen(
                                    onThemeChanged = { selectedTheme ->
                                        isDarkTheme = (selectedTheme == "Dark")
                                    },
                                    onLogoutClick = {
                                        authViewModel.resetState() // Reset AuthState về Idle để tránh lặp Token
                                        navController.navigate("welcome") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // 10. Màn hình Chi tiết sách
                            composable("book_detail/{bookId}") { backStackEntry ->
                                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                                BookDetailScreen(
                                    bookId = bookId,
                                    onBackClick = { navController.popBackStack() },
                                    onReadClick = { chapterId ->
                                        navController.navigate("reader/$bookId/$chapterId")
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