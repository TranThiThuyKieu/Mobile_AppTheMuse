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
import com.example.appthemuse.data.repository.AuthRepositoryImpl
import com.example.appthemuse.data.repository.BookRepositoryImpl
import com.example.appthemuse.data.repository.UserRepositoryImpl
import com.example.appthemuse.ui.screens.auth.*
import com.example.appthemuse.ui.screens.user.*
import com.example.appthemuse.ui.theme.AppTheMuseTheme
import com.example.appthemuse.ui.viewmodel.AuthViewModel
import com.example.appthemuse.ui.viewmodel.GenreViewModel
import com.example.appthemuse.ui.viewmodel.HomeViewModel
import com.example.appthemuse.ui.viewmodel.ProfileViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authService = AuthService()
        val firestoreService = FirestoreService()

        // 👉 ĐÃ SỬA: Truyền đúng và đủ các Service theo cấu trúc mới cấu hình ở bước trước
        val authRepository = AuthRepositoryImpl(authService, firestoreService)
        val bookRepository = BookRepositoryImpl(firestoreService)
        val userRepository = UserRepositoryImpl(firestoreService) // Dùng firestoreService lấy profile

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                        AuthViewModel(authRepository) as T
                    modelClass.isAssignableFrom(GenreViewModel::class.java) ->
                        GenreViewModel(authRepository, bookRepository) as T
                    modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                        HomeViewModel(bookRepository) as T
                    modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                        ProfileViewModel(userRepository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        val authViewModel by viewModels<AuthViewModel> { viewModelFactory }
        val genreViewModel by viewModels<GenreViewModel> { viewModelFactory }
        val homeViewModel by viewModels<HomeViewModel> { viewModelFactory }
        val profileViewModel by viewModels<ProfileViewModel> { viewModelFactory }

        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemInDarkTheme) }

            AppTheMuseTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val currentUser = FirebaseAuth.getInstance().currentUser

                // 👉 ĐÃ SỬA: Điều hướng động dựa trên việc user đã login hay chưa
                val startDestination = if (currentUser != null) "home" else "welcome"

                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        if (currentRoute in listOf("home", "explore", "bookshelf", "profile")) {
                            AppBottomBar(navController = navController, currentRoute = currentRoute)
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination, // Sửa lại biến động ở đây thay vì fix cứng "welcome"
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("welcome") {
                            WelcomeScreen(onNavigateToLogin = { navController.navigate("auth_options") })
                        }

                        composable("auth_options") {
                            AuthOptionsScreen(
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

                        composable("register") {
                            RegisterScreen(
                                viewModel = authViewModel,
                                onRegisterSuccess = {
                                    // Sau khi đăng ký xong, chuyển sang màn chọn thể loại
                                    navController.navigate("genre_selection") { popUpTo("register") { inclusive = true } }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login") { popUpTo("auth_options") }
                                }
                            )
                        }

                        composable("genre_selection") {
                            GenreSelectionScreen(
                                viewModel = genreViewModel,
                                onNavigateToHome = {
                                    navController.navigate("home") { popUpTo("genre_selection") { inclusive = true } }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(viewModel = homeViewModel, onBookClick = { })
                        }

                        composable("explore") {
                            ExploreScreen(viewModel = homeViewModel, onBookClick = { })
                        }

                        composable("bookshelf") {
                            Surface(modifier = Modifier.fillMaxSize()) {
                                Text(text = "Màn hình Tủ sách đang phát triển")
                            }
                        }

                        composable("profile") {
                            ProfileScreen(
                                viewModel = profileViewModel,
                                onThemeChanged = { themeName -> isDarkTheme = (themeName == "Dark") },
                                onLogout = {
                                    navController.navigate("welcome") { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}