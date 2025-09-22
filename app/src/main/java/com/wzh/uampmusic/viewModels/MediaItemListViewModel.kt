package com.wzh.uampmusic.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.wzh.common.common.MusicServiceConnection
import com.wzh.common.common.NOTHING_PLAYING
import com.wzh.uampmusic.R
import com.wzh.uampmusic.data.MediaItemData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaItemListViewModel(
    private val musicServiceConnection: MusicServiceConnection,
    private val mediaId: String
) : ViewModel() {

    data class UiState(
        val mediaItems: List<MediaItemData> = emptyList(),
        val isLoading: Boolean = true,
        val hasNetworkError: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val subscriptionCallback = object : MusicServiceConnection.SubscriptionCallback {
        override fun onChildrenLoaded(
            parentId: String,
            children: List<MediaItem>
        ) {
            val itemsList = children.map { child ->
                MediaItemData(
                    mediaId = child.mediaId,
                    title = child.mediaMetadata.title?.toString() ?: "",
                    subtitle = child.mediaMetadata.artist?.toString() ?: "",
                    albumArtUri = child.mediaMetadata.artworkUri ?: android.net.Uri.EMPTY,
                    browsable = child.mediaMetadata.isBrowsable ?: false,
                    playbackRes = 0
                )
            }
            _uiState.value = _uiState.value.copy(
                mediaItems = itemsList,
                isLoading = false,
                hasNetworkError = false
            )
        }

        override fun onError(parentId: String) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasNetworkError = true
            )
        }
    }

    init {
        // 订阅媒体项目
        musicServiceConnection.subscribe(mediaId, subscriptionCallback)

        // 监听播放状态变化，更新播放图标
        musicServiceConnection.nowPlaying.observeForever { nowPlaying ->
            val playbackState = musicServiceConnection.playbackState.value
            val isPlaying = musicServiceConnection.isPlaying.value ?: false
            updatePlaybackState(nowPlaying, playbackState, isPlaying)
        }
        
        musicServiceConnection.playbackState.observeForever { playbackState ->
            val nowPlaying = musicServiceConnection.nowPlaying.value
            val isPlaying = musicServiceConnection.isPlaying.value ?: false
            updatePlaybackState(nowPlaying, playbackState, isPlaying)
        }
        
        musicServiceConnection.isPlaying.observeForever { isPlaying ->
            val nowPlaying = musicServiceConnection.nowPlaying.value
            val playbackState = musicServiceConnection.playbackState.value
            updatePlaybackState(nowPlaying, playbackState, isPlaying)
        }
    }

    private fun updatePlaybackState(
        nowPlaying: MediaItem?,
        playbackState: Int?,
        isPlaying: Boolean
    ) {
        val currentItems = _uiState.value.mediaItems
        if (currentItems.isEmpty()) return

        val updatedItems = currentItems.map { mediaItem ->
            val playbackRes = when {
                mediaItem.mediaId == nowPlaying?.mediaId -> {
                    when {
                        isPlaying -> R.drawable.ic_pause_black_24dp
                        playbackState == Player.STATE_BUFFERING -> R.drawable.ic_pause_black_24dp
                        else -> R.drawable.ic_play_arrow_black_24dp
                    }
                }
                else -> 0
            }
            mediaItem.copy(playbackRes = playbackRes)
        }

        _uiState.value = _uiState.value.copy(mediaItems = updatedItems)
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(mediaId, subscriptionCallback)
        // Clean up LiveData observers
        musicServiceConnection.nowPlaying.removeObserver { }
        musicServiceConnection.playbackState.removeObserver { }
        musicServiceConnection.isPlaying.removeObserver { }
    }
}