package com.example.appthemuse.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appthemuse.ui.model.ChapterUi
import com.example.appthemuse.ui.viewmodel.ReadingState
import com.example.appthemuse.ui.viewmodel.ReadingViewModel
import com.example.appthemuse.ui.components.ReadingBottomBar
import com.example.appthemuse.ui.components.ChapterListModal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    bookId: String,
    initialChapterNumber: Int,
    viewModel: ReadingViewModel,
    navController: NavController,
    fontSizeValue: Float = 0.5f,
    lineSpacing: String = "Vừa"
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(bookId, initialChapterNumber) {
        viewModel.loadChapter(bookId, initialChapterNumber)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false, // Chỉ cho phép hiện khi nhấn nút 3 gạch
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                when (val state = uiState) {
                    is ReadingState.Success -> {
                        ChapterListModal(
                            chapters = state.allChapters,
                            currentChapterNumber = state.currentChapter.chapter_number,
                            onChapterClick = { chapterNum ->
                                scope.launch {
                                    drawerState.close()
                                    viewModel.loadChapter(bookId, chapterNum)
                                }
                            },
                            onClose = { scope.launch { drawerState.close() } }
                        )
                    }
                    is ReadingState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {}
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val state = uiState as? ReadingState.Success
                        if (state != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = state.currentChapter.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Chương ${state.currentChapter.chapter_number} • ${state.progressPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            Text("Đang tải...")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Chapters")
                        }
                    }
                )
            },
            bottomBar = {
                if (uiState is ReadingState.Success) {
                    val state = uiState as ReadingState.Success
                    ReadingBottomBar(
                        hasPrevious = state.currentChapter.chapter_number > 1,
                        hasNext = state.currentChapter.chapter_number < state.allChapters.size,
                        onPrevious = { 
                            scope.launch { drawerState.close() }
                            viewModel.loadChapter(bookId, state.currentChapter.chapter_number - 1) 
                        },
                        onNext = { 
                            scope.launch { drawerState.close() }
                            viewModel.loadChapter(bookId, state.currentChapter.chapter_number + 1) 
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (val state = uiState) {
                    is ReadingState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ReadingState.Success -> {
                        ReadingContent(
                            chapter = state.currentChapter,
                            fontSizeValue = fontSizeValue,
                            lineSpacing = lineSpacing
                        )
                    }
                    is ReadingState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingContent(
    modifier: Modifier = Modifier,
    chapter: ChapterUi,
    fontSizeValue: Float = 0.5f,
    lineSpacing: String = "Vừa"
) {
    val scrollState = rememberScrollState()

    val fontSize = when {
        fontSizeValue < 0.33f -> 14.sp
        fontSizeValue < 0.66f -> 18.sp
        else -> 22.sp
    }

    val lineHeightSp = when (lineSpacing) {
        "Dày" -> (fontSize.value * 1.4f).sp
        "Thưa" -> (fontSize.value * 2.2f).sp
        else -> (fontSize.value * 1.8f).sp
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Chương ${chapter.chapter_number}: ${chapter.title}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Chương ${chapter.chapter_number}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = chapter.content,
            fontSize = fontSize,
            lineHeight = lineHeightSp,
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Hết chương ${chapter.chapter_number}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}


