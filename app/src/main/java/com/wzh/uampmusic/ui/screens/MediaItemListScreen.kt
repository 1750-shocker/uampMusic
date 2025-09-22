package com.wzh.uampmusic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wzh.uampmusic.R
import com.wzh.uampmusic.data.MediaItemData
import com.wzh.uampmusic.ui.components.MediaItemCard
import com.wzh.uampmusic.utils.InjectorUtils
import com.wzh.uampmusic.viewModels.MediaItemListViewModel

@Composable
fun MediaItemListScreen(
    mediaId: String,
    onMediaItemClick: (MediaItemData) -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: MediaItemListViewModel = viewModel(
        factory = InjectorUtils.provideMediaItemListViewModel(context, mediaId)
    )

    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.hasNetworkError -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_signal_wifi_off_black_24dp),
                        contentDescription = "Network Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Network Error",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            uiState.mediaItems.isEmpty() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No media items found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = uiState.mediaItems,
                        key = { it.mediaId }
                    ) { mediaItem ->
                        MediaItemCard(
                            mediaItem = mediaItem,
                            onClick = { onMediaItemClick(mediaItem) }
                        )
                    }
                }
            }
        }
    }
}