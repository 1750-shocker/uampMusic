package com.wzh.uampmusic.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wzh.uampmusic.R
import com.wzh.uampmusic.utils.InjectorUtils
import com.wzh.uampmusic.viewModels.NowPlayingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    onPlayClick: (String) -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: NowPlayingViewModel = viewModel(
        factory = InjectorUtils.provideNowPlayingViewModel(context)
    )

    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // 背景专辑封面
        uiState.mediaMetadata?.let { metadata ->
            if (metadata.albumArtUri != Uri.EMPTY) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(metadata.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // 渐变遮罩
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.8f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            } else {
                // 默认背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }

        // 顶部应用栏
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // 主要内容区域
        uiState.mediaMetadata?.let { metadata ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 专辑封面
                Card(
                    modifier = Modifier.size(280.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(metadata.albumArtUri)
                            .placeholder(R.drawable.ic_album_black_24dp)
                            .error(R.drawable.ic_album_black_24dp)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 歌曲信息
                Text(
                    text = metadata.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = metadata.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 时间信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = NowPlayingViewModel.NowPlayingMetadata
                            .timestampToMSS(context, uiState.mediaPosition),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Text(
                        text = metadata.duration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 控制按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPrevClick,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_skip_previous_black_24dp),
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = { onPlayClick(metadata.id) },
                        modifier = Modifier.size(80.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            painter = painterResource(uiState.mediaButtonRes),
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(
                        onClick = onNextClick,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_skip_next_black_24dp),
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        } ?: run {
            // 没有播放内容时的占位符
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_album_black_24dp),
                    contentDescription = "No Music",
                    modifier = Modifier.size(120.dp),
                    tint = Color.White.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No music playing",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
