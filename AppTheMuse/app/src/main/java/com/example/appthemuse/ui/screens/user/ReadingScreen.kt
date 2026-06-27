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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appthemuse.ui.model.ChapterUi
import com.example.appthemuse.ui.viewmodel.ReadingState
import com.example.appthemuse.ui.viewmodel.ReadingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    bookId: String,
    initialChapterNumber: Int,
    viewModel: ReadingViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(bookId, initialChapterNumber) {
        viewModel.loadChapter(bookId, initialChapterNumber)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (uiState is ReadingState.Success) {
                val state = uiState as ReadingState.Success
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
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                if (uiState is ReadingState.Success) {
                    val state = uiState as ReadingState.Success
                    TopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = state.currentChapter.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Chương ${state.currentChapter.chapter_number} • ${state.progressPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.toggleBookmark() }) {
                                Icon(
                                    if (state.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (state.isBookmarked) Color(0xFF6C63FF) else LocalContentColor.current
                                )
                            }
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Chapters")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (uiState is ReadingState.Success) {
                    val state = uiState as ReadingState.Success
                    ReadingBottomBar(
                        hasPrevious = state.currentChapter.chapter_number > 1,
                        hasNext = state.currentChapter.chapter_number < state.allChapters.size,
                        onPrevious = { viewModel.loadChapter(bookId, state.currentChapter.chapter_number - 1) },
                        onNext = { viewModel.loadChapter(bookId, state.currentChapter.chapter_number + 1) }
                    )
                }
            }
        ) { paddingValues ->
            when (val state = uiState) {
                is ReadingState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ReadingState.Success -> {
                    ReadingContent(
                        modifier = Modifier.padding(paddingValues),
                        chapter = state.currentChapter
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

@Composable
fun ReadingContent(
    modifier: Modifier = Modifier,
    chapter: ChapterUi
) {
    val scrollState = rememberScrollState()
    
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
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 28.sp,
                fontSize = 18.sp
            ),
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

@Composable
fun ReadingBottomBar(
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = hasPrevious,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6C63FF))
            ) {
                Text(text = "< chương trước", color = if (hasPrevious) Color(0xFF6C63FF) else Color.Gray)
            }
            
            Button(
                onClick = onNext,
                enabled = hasNext,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) {
                Text(text = "chương sau >")
            }
        }
    }
}

@Composable
fun ChapterListModal(
    chapters: List<ChapterUi>,
    currentChapterNumber: Int,
    onChapterClick: (Int) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Danh sách chương",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${chapters.size} CHƯƠNG",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            HorizontalDivider(thickness = 0.5.dp)
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chapters) { chapter ->
                    val isCurrent = chapter.chapter_number == currentChapterNumber
                    ChapterModalItem(
                        chapter = chapter,
                        isCurrent = isCurrent,
                        onClick = { onChapterClick(chapter.chapter_number) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChapterModalItem(
    chapter: ChapterUi,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isCurrent) Color.LightGray.copy(alpha = 0.3f) else Color.Transparent)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chương ${chapter.chapter_number}: ${chapter.title}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (isCurrent) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Đang đọc",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }
    }
}