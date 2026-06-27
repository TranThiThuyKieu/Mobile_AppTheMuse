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
import com.example.appthemuse.data.remote.FirebaseUserService
import com.example.appthemuse.data.repository.AuthRepositoryImpl
import com.example.appthemuse.data.repository.BookRepositoryImpl
import com.example.appthemuse.data.repository.UserRepositoryImpl
import com.example.appthemuse.ui.screens.auth.*
import com.example.appthemuse.ui.screens.user.*
import com.example.appthemuse.ui.theme.AppTheMuseTheme
import com.example.appthemuse.ui.viewmodel.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.appthemuse.data.local.database.AppDatabase
import com.example.appthemuse.data.repository.DownloadedRepositoryImpl
import com.example.appthemuse.data.repository.LibraryRepositoryImpl
import com.example.appthemuse.ui.screens.user.creator_studio.CreateBookScreen
import com.example.appthemuse.ui.screens.user.creator_studio.CreatorStudioScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.appthemuse.ui.screens.user.creator_studio.CreatorBookDetailScreen
import com.example.appthemuse.ui.screens.user.creator_studio.AddChapterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authService = AuthService()
        val firestoreService = FirestoreService()
        val firebaseUserService = FirebaseUserService()

        val authRepository = AuthRepositoryImpl(authService, firestoreService)
        val bookRepository = BookRepositoryImpl(firestoreService)
        val libraryRepository = LibraryRepositoryImpl(firestoreService)
        val database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()
        val downloadRepository = DownloadedRepositoryImpl(database.downloadedBookDao())
        val userRepository = UserRepositoryImpl(firebaseUserService)

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
                    modelClass.isAssignableFrom(LibraryViewModel::class.java) ->
                        LibraryViewModel(libraryRepository, downloadRepository, bookRepository) as T
                    modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                        ProfileViewModel(userRepository) as T
                    modelClass.isAssignableFrom(EditProfileViewModel::class.java) ->
                        EditProfileViewModel(userRepository) as T
                    modelClass.isAssignableFrom(SecurityViewModel::class.java) ->
                        SecurityViewModel() as T
                    modelClass.isAssignableFrom(CreatorStudioViewModel::class.java) ->
                        CreatorStudioViewModel(bookRepository, userRepository) as T
                    modelClass.isAssignableFrom(CreateBookViewModel::class.java) ->
                        CreateBookViewModel(bookRepository, userRepository) as T
                    modelClass.isAssignableFrom(CreatorBookDetailViewModel::class.java) ->
                        CreatorBookDetailViewModel(bookRepository) as T
                    modelClass.isAssignableFrom(AddChapterViewModel::class.java) ->
                        AddChapterViewModel(bookRepository) as T
                    modelClass.isAssignableFrom(BookDetailViewModel::class.java) ->
                        BookDetailViewModel(bookRepository, libraryRepository, downloadRepository) as T
                    modelClass.isAssignableFrom(ReadingViewModel::class.java) ->
                        ReadingViewModel(bookRepository, downloadRepository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        val authViewModel by viewModels<AuthViewModel> { viewModelFactory }
        val genreViewModel by viewModels<GenreViewModel> { viewModelFactory }
        val homeViewModel by viewModels<HomeViewModel> { viewModelFactory }
        val libraryViewModel by viewModels<LibraryViewModel> { viewModelFactory }
        val profileViewModel by viewModels<ProfileViewModel> { viewModelFactory }
        val editProfileViewModel by viewModels<EditProfileViewModel> { viewModelFactory }
        val securityViewModel by viewModels<SecurityViewModel> { viewModelFactory }
        val creatorStudioViewModel by viewModels<CreatorStudioViewModel> { viewModelFactory }
        val createBookViewModel by viewModels<CreateBookViewModel> { viewModelFactory }
        val creatorBookDetailViewModel by viewModels<CreatorBookDetailViewModel> { viewModelFactory }
        val addChapterViewModel by viewModels<AddChapterViewModel> { viewModelFactory }
        val bookDetailViewModel by viewModels<BookDetailViewModel> { viewModelFactory }
        val readingViewModel by viewModels<ReadingViewModel> { viewModelFactory }

        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemInDarkTheme) }

            AppTheMuseTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val currentUser = FirebaseAuth.getInstance().currentUser
                
                // Logic Offline: Nếu có mạng thì vào Home, không mạng/lỗi mà có login thì vào Bookshelf
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
                        startDestination = startDestination,
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
                                    navController.navigate("verify_email") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login") { popUpTo("auth_options") }
                                }
                            )
                        }

                        composable("verify_email") {
                            VerificationWaitScreen(
                                viewModel = authViewModel,
                                onNavigateToGenres = {
                                    navController.navigate("genre_selection") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onCancelVerification = {
                                    navController.navigate("auth_options") {
                                        popUpTo(0) { inclusive = true }
                                    }
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
                            HomeScreen(
                                viewModel = homeViewModel,
                                navController = navController,
                                onBookClick = { bookId -> 
                                    navController.navigate("book_detail/$bookId")
                                }
                            )
                        }

                        composable("explore") {
                            ExploreScreen(
                                viewModel = homeViewModel,
                                navController = navController, 
                                onBookClick = { bookId -> 
                                    navController.navigate("book_detail/$bookId")
                                }
                            )
                        }

                        composable("bookshelf") {
                            LibraryScreen(
                                viewModel = libraryViewModel,
                                homeViewModel = homeViewModel,
                                navController = navController,
                                userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                onBookClick = { bookId -> 
                                    navController.navigate("book_detail/$bookId")
                                }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                viewModel = profileViewModel,
                                onThemeChanged = { themeName ->
                                    isDarkTheme = (themeName == "Dark")
                                },
                                onEditProfileClick = {
                                    navController.navigate("edit_profile")
                                },
                                onSecurityClick = { navController.navigate("security_route") },
                                onCreatorStudioClick = {
                                    navController.navigate("creator_studio")
                                },
                                onLogout = {
                                    navController.navigate("welcome") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("book_detail/{bookId}") { backStack ->
                            val bookId = backStack.arguments?.getString("bookId") ?: ""
                            BookDetailScreen(
                                bookId = bookId,
                                viewModel = bookDetailViewModel,
                                navController = navController
                            )
                        }

                        composable(
                            "reading/{bookId}/{chapterNumber}",
                            arguments = listOf(
                                navArgument("bookId") { type = NavType.StringType },
                                navArgument("chapterNumber") { type = NavType.IntType }
                            )
                        ) { backStack ->
                            val bookId = backStack.arguments?.getString("bookId") ?: ""
                            val chapterNumber = backStack.arguments?.getInt("chapterNumber") ?: 1
                            ReadingScreen(
                                bookId = bookId,
                                initialChapterNumber = chapterNumber,
                                viewModel = readingViewModel,
                                navController = navController
                            )
                        }

                        // Creator Studio Routes
                        composable("creator_studio") {
                            CreatorStudioScreen(
                                viewModel = creatorStudioViewModel,
                                navController = navController,
                                onBackClick = { navController.popBackStack() },
                                onCreateBookClick = { navController.navigate("create_book") }
                            )
                        }
                        composable("create_book") {
                            CreateBookScreen(
                                viewModel = createBookViewModel,
                                onBackClick = { navController.popBackStack() },
                                onSuccess = { navController.popBackStack() }
                            )
                        }
                        composable("creator_book_detail/{bookId}") { backStack ->
                            val bookId = backStack.arguments?.getString("bookId") ?: ""
                            CreatorBookDetailScreen(
                                bookId = bookId,
                                viewModel = creatorBookDetailViewModel,
                                onBackClick = { navController.popBackStack() },
                                onPostChapterClick = { id, nextChapterNum ->
                                    navController.navigate("add_chapter/$id/$nextChapterNum")
                                }
                            )
                        }
                        composable(
                            "add_chapter/{bookId}/{chapterNumber}",
                            arguments = listOf(
                                navArgument("bookId") { type = NavType.StringType },
                                navArgument("chapterNumber") { type = NavType.IntType }
                            )
                        ) { backStack ->
                            val bookId = backStack.arguments?.getString("bookId") ?: ""
                            val chapterNumber = backStack.arguments?.getInt("chapterNumber") ?: 1
                            AddChapterScreen(
                                bookId = bookId,
                                chapterNumber = chapterNumber,
                                viewModel = addChapterViewModel,
                                onBackClick = { navController.popBackStack() },
                                onSuccess = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
