package com.wzh.common.media.ext

import androidx.media3.common.Player
import androidx.media3.session.MediaSession

/**
 * Media3 Player和MediaSession的扩展方法
 * 替代旧的PlaybackStateCompat扩展
 */

/**
 * Player状态扩展属性
 */
inline val Player.isPrepared: Boolean
    get() = playbackState == Player.STATE_BUFFERING ||
            playbackState == Player.STATE_READY

inline val Player.isPlaying: Boolean
    get() = playWhenReady && playbackState == Player.STATE_READY

inline val Player.isPlayEnabled: Boolean
    get() = playbackState != Player.STATE_ENDED

inline val Player.isPauseEnabled: Boolean
    get() = isPlaying

inline val Player.isSkipToNextEnabled: Boolean
    get() = hasNextMediaItem()

inline val Player.isSkipToPreviousEnabled: Boolean
    get() = hasPreviousMediaItem()

/**
 * 获取播放状态名称
 */
inline val Player.stateName: String
    get() = when (playbackState) {
        Player.STATE_IDLE -> "STATE_IDLE"
        Player.STATE_BUFFERING -> "STATE_BUFFERING"
        Player.STATE_READY -> "STATE_READY"
        Player.STATE_ENDED -> "STATE_ENDED"
        else -> "UNKNOWN_STATE"
    }

/**
 * 获取当前播放位置（毫秒）
 */
inline val Player.currentPlayBackPosition: Long
    get() = currentPosition

/**
 * 播放控制扩展方法
 */
fun Player.playPause() {
    if (isPlaying) {
        pause()
    } else {
        play()
    }
}

/**
 * 安全的跳转到下一首
 */
fun Player.safeSeekToNext() {
    if (isSkipToNextEnabled) {
        seekToNextMediaItem()
    }
}

/**
 * 安全的跳转到上一首
 */
fun Player.safeSeekToPrevious() {
    if (isSkipToPreviousEnabled) {
        seekToPreviousMediaItem()
    }
}

/**
 * 获取播放进度百分比 (0.0 - 1.0)
 */
inline val Player.playbackProgress: Float
    get() {
        val duration = this.duration
        return if (duration > 0) {
            currentPosition.toFloat() / duration.toFloat()
        } else {
            0f
        }
    }

/**
 * 检查是否有媒体项在播放列表中
 */
inline val Player.hasMediaItems: Boolean
    get() = mediaItemCount > 0

/**
 * 获取当前媒体项的标题
 */
inline val Player.currentMediaTitle: String?
    get() = currentMediaItem?.mediaMetadata?.title?.toString()

/**
 * 获取当前媒体项的艺术家
 */
inline val Player.currentMediaArtist: String?
    get() = currentMediaItem?.mediaMetadata?.artist?.toString()

/**
 * MediaSession扩展方法
 */
fun MediaSession.updatePlaybackState() {
    // Media3的MediaSession会自动更新播放状态
    // 这个方法保留用于兼容性，实际上不需要手动更新
}

/**
 * 播放状态监听器的便捷扩展
 */
class SimplePlayerListener(
    private val onPlaybackStateChanged: ((playbackState: Int) -> Unit)? = null,
    private val onIsPlayingChanged: ((isPlaying: Boolean) -> Unit)? = null,
    private val onMediaItemTransition: ((mediaItem: androidx.media3.common.MediaItem?) -> Unit)? = null
) : Player.Listener {
    
    override fun onPlaybackStateChanged(playbackState: Int) {
        onPlaybackStateChanged?.invoke(playbackState)
    }
    
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        onIsPlayingChanged?.invoke(isPlaying)
    }
    
    override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
        onMediaItemTransition?.invoke(mediaItem)
    }
}