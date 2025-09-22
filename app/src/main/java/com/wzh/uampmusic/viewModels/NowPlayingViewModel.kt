package com.wzh.uampmusic.viewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.wzh.common.common.MusicServiceConnection
import com.wzh.common.common.NOTHING_PLAYING
import com.wzh.uampmusic.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NowPlayingViewModel(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    data class NowPlayingMetadata(
        val id: String,
        val albumArtUri: Uri,
        val title: String,
        val subtitle: String,
        val duration: String
    ) {
        companion object {
            fun timestampToMSS(context: Context?, position: Long): String {
                val totalSeconds = (position / 1000).toInt()
                val minutes = totalSeconds / 60
                val remainingSeconds = totalSeconds % 60
                return String.format("%d:%02d", minutes, remainingSeconds)
            }
        }
    }

    data class UiState(
        val mediaMetadata: NowPlayingMetadata? = null,
        val mediaButtonRes: Int = R.drawable.ic_play_arrow_black_24dp,
        val mediaPosition: Long = 0L,
        val isPlaying: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // 监听播放状态和媒体元数据变化
        musicServiceConnection.nowPlaying.observeForever { nowPlaying ->
            val playbackState = musicServiceConnection.playbackState.value
            val isPlaying = musicServiceConnection.isPlaying.value ?: false
            updateState(nowPlaying, playbackState, isPlaying)
        }
        
        musicServiceConnection.playbackState.observeForever { playbackState ->
            val nowPlaying = musicServiceConnection.nowPlaying.value
            val isPlaying = musicServiceConnection.isPlaying.value ?: false
            updateState(nowPlaying, playbackState, isPlaying)
        }
        
        musicServiceConnection.isPlaying.observeForever { isPlaying ->
            val nowPlaying = musicServiceConnection.nowPlaying.value
            val playbackState = musicServiceConnection.playbackState.value
            updateState(nowPlaying, playbackState, isPlaying)
        }

        // 更新播放位置
        viewModelScope.launch {
            while (true) {
                val isPlaying = musicServiceConnection.isPlaying.value ?: false
                if (isPlaying) {
                    val currentPosition = musicServiceConnection.playbackPosition.value ?: 0L
                    _uiState.value = _uiState.value.copy(mediaPosition = currentPosition)
                }
                delay(1000) // 每秒更新一次
            }
        }
    }

    private fun updateState(
        nowPlaying: MediaItem?,
        playbackState: Int?,
        isPlaying: Boolean
    ) {
        if (nowPlaying == null || nowPlaying == NOTHING_PLAYING) {
            _uiState.value = UiState()
            return
        }

        val metadata = NowPlayingMetadata(
            id = nowPlaying.mediaId,
            albumArtUri = nowPlaying.mediaMetadata.artworkUri ?: Uri.EMPTY,
            title = nowPlaying.mediaMetadata.title?.toString() ?: "",
            subtitle = nowPlaying.mediaMetadata.artist?.toString() ?: "",
            duration = nowPlaying.mediaMetadata.durationMs?.let { duration ->
                if (duration > 0) {
                    NowPlayingMetadata.timestampToMSS(null, duration)
                } else {
                    "0:00"
                }
            } ?: "0:00"
        )

        val buttonRes = when {
            isPlaying -> R.drawable.ic_pause_black_24dp
            playbackState == Player.STATE_BUFFERING -> R.drawable.ic_pause_black_24dp
            else -> R.drawable.ic_play_arrow_black_24dp
        }

        _uiState.value = _uiState.value.copy(
            mediaMetadata = metadata,
            mediaButtonRes = buttonRes,
            isPlaying = isPlaying,
            mediaPosition = musicServiceConnection.playbackPosition.value ?: 0L
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up LiveData observers
        musicServiceConnection.nowPlaying.removeObserver { }
        musicServiceConnection.playbackState.removeObserver { }
        musicServiceConnection.isPlaying.removeObserver { }
    }
}