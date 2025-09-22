package com.wzh.uampmusic.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.media3.common.Player
import com.wzh.common.common.MusicServiceConnection
import com.wzh.uampmusic.data.MediaItemData

class MainActivityViewModel(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    data class UiState(
        val currentMediaId: String = "",
        val isConnected: Boolean = false,
        val navigationEvent: NavigationEvent? = null
    )

    sealed class NavigationEvent {
        object NavigateToMediaList : NavigationEvent()
        object NavigateToNowPlaying : NavigationEvent()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // 监听音乐服务连接状态
        musicServiceConnection.isConnected.observeForever { isConnected ->
            _uiState.value = _uiState.value.copy(isConnected = isConnected)
            if (isConnected) {
                // 连接成功后获取根媒体ID
                val rootMediaId = musicServiceConnection.rootMediaId
                _uiState.value = _uiState.value.copy(currentMediaId = rootMediaId)
            }
        }
    }

    fun onMediaItemClicked(mediaItem: MediaItemData) {
        if (mediaItem.browsable) {
            // 如果是可浏览的项目，导航到媒体列表
            _uiState.value = _uiState.value.copy(
                currentMediaId = mediaItem.mediaId,
                navigationEvent = NavigationEvent.NavigateToMediaList
            )
        } else {
            // 如果是可播放的项目，开始播放并导航到播放界面
            playMediaId(mediaItem.mediaId)
            _uiState.value = _uiState.value.copy(
                navigationEvent = NavigationEvent.NavigateToNowPlaying
            )
        }
    }

    fun onPlayMediaId(mediaId: String) {
        playMediaId(mediaId)
    }

    fun onPrevMedia() {
        musicServiceConnection.skipToPrevious()
    }

    fun onNextMedia() {
        musicServiceConnection.skipToNext()
    }

    fun onNavigationEventHandled() {
        _uiState.value = _uiState.value.copy(navigationEvent = null)
    }

    private fun playMediaId(mediaId: String) {
        val nowPlaying = musicServiceConnection.nowPlaying.value

        val playbackState = musicServiceConnection.playbackState.value
        val isPrepared = playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING
        if (isPrepared && mediaId == nowPlaying?.mediaId) {
            val isPlaying = musicServiceConnection.isPlaying.value ?: false
            when {
                isPlaying -> {
                    musicServiceConnection.pause()
                }
                else -> {
                    musicServiceConnection.play()
                }
            }
        } else {
            musicServiceConnection.playFromMediaId(mediaId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up LiveData observers
        musicServiceConnection.isConnected.removeObserver { }
    }
}