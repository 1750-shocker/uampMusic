package com.wzh.common.media

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.wzh.common.R
import com.wzh.common.media.library.BrowseTree
import com.wzh.common.media.library.JsonSource
import com.wzh.common.media.library.MusicSource
import com.wzh.common.media.library.STATE_INITIALIZED
import com.wzh.common.media.library.UAMP_BROWSABLE_ROOT
import com.wzh.common.media.library.UAMP_RECENT_ROOT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Media3版本的音乐服务（简化版）
 */
@UnstableApi
class MusicService : MediaLibraryService() {

    companion object {
        private const val TAG = "MusicService"
        const val NETWORK_FAILURE = "com.wzh.uampmusic.media.session.NETWORK_FAILURE"
    }

    private lateinit var mediaSource: MusicSource
    private lateinit var mediaSession: MediaLibrarySession
    private lateinit var player: ExoPlayer

    // 协程管理
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    // 懒加载的浏览树
    private val browseTree: BrowseTree by lazy {
        BrowseTree(applicationContext, mediaSource)
    }

    // 远程JSON数据源
    private val remoteJsonSource: Uri =
        Uri.parse("https://storage.googleapis.com/uamp/catalog.json")

    // 音频属性配置
    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    override fun onCreate() {
        super.onCreate()
        initializeSessionAndPlayer()
        initializeMediaSource()
    }

    /**
     * 初始化MediaSession和ExoPlayer
     */
    private fun initializeSessionAndPlayer() {
        // 创建ExoPlayer
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        // 添加播放器监听器
        player.addListener(PlayerEventListener())

        // 创建启动Activity的PendingIntent
        val sessionActivityPendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
            PendingIntent.getActivity(this, 0, sessionIntent, PendingIntent.FLAG_IMMUTABLE)
        }

        // 创建MediaLibrarySession
        mediaSession = MediaLibrarySession.Builder(this, player, MediaLibrarySessionCallback()).apply {
            sessionActivityPendingIntent?.let { setSessionActivity(it) }
        }.build()
    }

    /**
     * 初始化媒体数据源
     */
    private fun initializeMediaSource() {
        mediaSource = JsonSource(source = remoteJsonSource)
        serviceScope.launch {
            mediaSource.load()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        player.stop()
    }

    override fun onDestroy() {
        mediaSession.release()
        serviceJob.cancel()
        player.release()
        super.onDestroy()
    }

    /**
     * MediaLibrarySession回调处理（简化版）
     */
    private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {

        /**
         * 获取库根目录
         */
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootExtras = Bundle().apply {
                putBoolean("android.media.browse.SEARCH_SUPPORTED", false)
                putBoolean("android.media.browse.CONTENT_STYLE_SUPPORTED", true)
                putInt("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT", 2) // GRID
                putInt("android.media.browse.CONTENT_STYLE_PLAYABLE_HINT", 1) // LIST
            }

            val isRecentRequest = params?.extras?.getBoolean("android.media.browse.EXTRA_RECENT") ?: false
            val rootId = if (isRecentRequest) UAMP_RECENT_ROOT else UAMP_BROWSABLE_ROOT

            val rootItem = MediaItem.Builder()
                .setMediaId(rootId)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setExtras(rootExtras)
                        .build()
                )
                .build()

            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        /**
         * 获取子项列表
         */
        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return try {
                val isInitialized = try {
                    // 使用whenReady来检查状态，而不是直接访问state
                    var ready = false
                    mediaSource.whenReady { success -> ready = success }
                    ready
                } catch (e: Exception) {
                    false
                }

                if (isInitialized) {
                    val children = browseTree[parentId] ?: emptyList()
                    Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(children), params))
                } else {
                    Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.of(), params))
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取子项失败", e)
                Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.of(), params))
            }
        }
    }

    /**
     * 播放器事件监听器
     */
    private inner class PlayerEventListener : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    if (playbackState == Player.STATE_READY && !player.isPlaying) {
                        // 播放暂停时，移除前台服务状态
                        stopForeground(STOP_FOREGROUND_DETACH)
                    }
                }
                else -> {}
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            mediaItem?.let {
                Log.d(TAG, "现在播放: ${it.mediaMetadata.title}")
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            var message = R.string.generic_error
            Log.e(TAG, "播放器错误: ${error.errorCodeName} (${error.errorCode})")
            
            when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                    message = R.string.error_media_not_found
                }
            }
            
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }
}