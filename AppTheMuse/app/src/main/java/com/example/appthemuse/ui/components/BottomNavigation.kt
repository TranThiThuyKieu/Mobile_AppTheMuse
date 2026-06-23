package com.example.appthemuse.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun AppBottomBar(
    navController: NavController,
    currentRoute: String?) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Trang chủ") }
        )
        NavigationBarItem(selected = currentRoute == "explore", onClick = {
                navController.navigate("explore") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Explore, null) },
            label = { Text("Khám phá") }
        )
        NavigationBarItem(selected = currentRoute == "bookshelf", onClick = {
                navController.navigate("bookshelf") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Book, null) },
            label = { Text("Tủ sách") }
        )
        NavigationBarItem(
            selected = currentRoute == "profile", onClick = {
                navController.navigate("profile") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Hồ sơ") }
        )
    }
}