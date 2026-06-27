package com.example.appthemuse.ui.screens.admin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appthemuse.ui.viewmodel.*

private val AdminPrimary = Color(0xFF6C63FF)

@Composable
fun AdminMainScreen(
    adminDashboardViewModel: AdminDashboardViewModel,
    adminUserViewModel: AdminUserViewModel,
    adminUserDetailViewModel: AdminUserDetailViewModel,
    adminBookManagementViewModel: AdminBookManagementViewModel,
    adminBookDetailViewModel: AdminBookDetailViewModel,
    adminReviewModerationViewModel: AdminReviewModerationViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute !in listOf("admin_user_detail/{userId}")) {
                AdminBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "admin_dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("admin_dashboard") {
                AdminDashboardScreen(
                    viewModel = adminDashboardViewModel,
                    onLogout = onLogout
                )
            }
            composable("admin_users") {
                AdminUserManagementScreen(
                    viewModel = adminUserViewModel,
                    onViewProfile = { userId ->
                        navController.navigate("admin_user_detail/$userId")
                    },
                    onLogout = onLogout
                )
            }
            composable("admin_user_detail/{userId}") { backStack ->
                val userId = backStack.arguments?.getString("userId") ?: ""
                AdminUserDetailScreen(
                    userId = userId,
                    viewModel = adminUserDetailViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("admin_books") {
                AdminBookManagementScreen(
                    viewModel = adminBookManagementViewModel,
                    onBookClick = { bookId ->
                        navController.navigate("admin_book_detail/$bookId")
                    }
                )
            }
            composable("admin_reviews") {
                AdminReviewModerationScreen(
                    bookId = "",
                    viewModel = adminReviewModerationViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("admin_book_detail/{bookId}") { backStack ->
                val bookId = backStack.arguments?.getString("bookId") ?: ""
                AdminBookDetailScreen(
                    bookId = bookId,
                    viewModel = adminBookDetailViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenReviews = { id ->
                        adminReviewModerationViewModel.load(id)
                        navController.navigate("admin_reviews")
                    }
                )
            }
        }
    }
}

@Composable
fun AdminBottomBar(
    navController: NavController,
    currentRoute: String?
) {
    val items = listOf(
        Triple("admin_dashboard", Icons.Default.Dashboard, "Tổng quan"),
        Triple("admin_users", Icons.Default.Person, "Người dùng"),
        Triple("admin_books", Icons.Default.Book, "Sách"),
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AdminPrimary,
                    selectedTextColor = AdminPrimary,
                    indicatorColor = AdminPrimary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
