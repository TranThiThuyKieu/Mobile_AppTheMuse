package com.example.appthemuse.ui.screens.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.appthemuse.ui.model.BookUi
import com.example.appthemuse.ui.model.ChapterUi
import com.example.appthemuse.ui.model.ReviewUi
import com.example.appthemuse.ui.viewmodel.BookDetailState
import com.example.appthemuse.ui.viewmodel.BookDetailViewModel
import com.example.appthemuse.ui.components.ReviewItem
import com.example.appthemuse.ui.components.AddReviewDialog
import com.example.appthemuse.ui.components.ChapterItem
import com.example.appthemuse.ui.theme.GoldStar
import com.example.appthemuse.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    viewModel: BookDetailViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var showReviewDialog by remember { mutableStateOf(false) }

    // Tự động tải lại chi tiết sách khi màn hình được hiển thị lại
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadBookDetail(bookId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val successState = uiState as? BookDetailState.Success
                    val isFavorite = successState?.isFavorite ?: false
                    val isOnline = successState?.isOnline ?: true
                    if (isOnline) {
                        IconButton(onClick = { viewModel.toggleFavorite(bookId) }) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is BookDetailState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is BookDetailState.Success -> {
                BookDetailContent(
                    modifier = Modifier.padding(paddingValues),
                    book = state.book,
                    chapters = state.chapters,
                    reviews = state.reviews,
                    isDownloaded = state.isDownloaded,
                    progressPercent = state.book.progressPercent,
                    lastReadChapterNumber = state.lastReadChapterNumber,
                    isFinished = state.isFinished,
                    isOnline = state.isOnline,
                    onReadClick = { chapterNum ->
                        navController.navigate("reading/$bookId/$chapterNum")
                    },
                    onDownloadClick = { viewModel.downloadBook(state.book) },
                    onDeleteClick = { viewModel.deleteBook(bookId) },
                    onWriteReviewClick = { showReviewDialog = true }
                )

                if (showReviewDialog) {
                    AddReviewDialog(
                        onDismiss = { showReviewDialog = false },
                        onSubmit = { rating, comment ->
                            viewModel.addReview(bookId, rating, comment)
                            showReviewDialog = false
                        }
                    )
                }
            }
            is BookDetailState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun BookDetailContent(
    modifier: Modifier = Modifier,
    book: BookUi,
    chapters: List<ChapterUi>,
    reviews: List<ReviewUi>,
    isDownloaded: Boolean,
    progressPercent: Int,
    lastReadChapterNumber: Int,
    isFinished: Boolean,
    isOnline: Boolean,
    onReadClick: (Int) -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onWriteReviewClick: () -> Unit
) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberAsyncImagePainter(book.cover_url),
                    contentDescription = null,
                    modifier = Modifier
                        .size(160.dp, 240.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = book.author_name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldStar, modifier = Modifier.size(18.dp))
                    Text(text = " ${String.format("%.1f", book.rating)}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.RemoveRedEye, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(text = " ${formatViewCount(book.view_count)}", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isDescriptionExpanded) "Thu gọn" else "Xem thêm",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            if (isFinished) onReadClick(1)
                            else onReadClick(lastReadChapterNumber)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val buttonText = when {
                            isFinished -> "Đọc lại"
                            progressPercent > 0 -> "Đọc tiếp ($progressPercent%)"
                            else -> "Đọc ngay"
                        }
                        Text(text = buttonText, fontWeight = FontWeight.Bold)
                    }

                    if (progressPercent > 0 && !isFinished) {
                        OutlinedButton(
                            onClick = { onReadClick(1) },
                            modifier = Modifier
                                .weight(0.6f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = "Đọc lại", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!isDownloaded) {
                    if (isOnline) {
                        TextButton(onClick = onDownloadClick) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tải xuống để đọc offline")
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đã tải xuống", color = SuccessGreen, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Xóa", color = Color.Red)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Danh sach chuong
        item {
            Text(
                text = "Danh sách chương",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(chapters) { chapter ->
            val readingStatus = when {
                isFinished -> "Đã đọc"
                chapter.chapter_number < lastReadChapterNumber -> "Đã đọc"
                chapter.chapter_number == lastReadChapterNumber && progressPercent > 0 -> "Đang đọc"
                else -> ""
            }

            ChapterItem(
                chapter = chapter,
                statusText = readingStatus,
                onClick = { onReadClick(chapter.chapter_number) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
        }

        // Danh gia
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đánh giá (${reviews.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isOnline) {
                    TextButton(onClick = onWriteReviewClick) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Viết đánh giá")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (reviews.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isOnline) "Chưa có đánh giá nào." else "Không thể tải bình luận khi ngoại tuyến.",
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(reviews) { review ->
                ReviewItem(review = review)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
// luot xem
fun formatViewCount(count: Long): String {
    return when {
        count >= 1_000_000 -> "${String.format("%.1f", count / 1_000_000.0)}M"
        count >= 1000 -> "${String.format("%.1f", count / 1000.0)}K"
        else -> count.toString()
    }
}