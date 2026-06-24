package com.example.appthemuse.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appthemuse.ui.components.AppBottomBar
import com.example.appthemuse.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(viewModel: HomeViewModel, navController: NavController) {
    var showSearch by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    Scaffold(
        topBar = {
            HomeTopBar(onSearchClick = {
                    showSearch = true
                }
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController, currentRoute = currentRoute)
        }
    ) { padding ->
        // Danh sách thể loại
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Duyệt qua danh sách category
            items(uiState.categories) { category ->
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp)
                        .clickable {
                            navController.navigate("book/${category.name}/${category.id.removePrefix("cate")}")
                        }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(category.name)
                        Spacer(Modifier.height(4.dp))
                        val count = uiState.allBooks.count {
                            it.category_id == category.id.removePrefix("cate")
                        }
                        Text("$count truyện")
                    }
                }
            }
        }
    }
    // Hiển thị màn hình tìm kiếm nếu showSearch = true
    if (showSearch) {
        SearchScreen(viewModel = viewModel, onClose = {
                showSearch = false
            }
        )
    }
}