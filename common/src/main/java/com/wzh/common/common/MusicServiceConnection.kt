package com.wzh.common.common

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.wzh.common.media.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Media3版本的音乐服务连接管理器
 * 
 * 这是Repository层的实现，负责管理与MusicService的连接，
 * 提供播放控制和媒体浏览功能，并通过LiveData向UI层暴露状态。
 */
class MusicServiceConnection private constructor(
    private val context: Context,
    serviceComponent: ComponentName
) {
    
    // 协程作用域
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // 会话令牌
    private val sessionToken = SessionToken(context, serviceComponent)
    
    /**
     * 表示与MediaLibraryService的连接状态
     */
    val isConnected = MutableLiveData<Boolean>().apply { postValue(false) }
    
    /**
     * 网络故障状态，当服务发出网络错误事件时会置为true
     */
    val networkFailure = MutableLiveData<Boolean>().apply { postValue(false) }
    
    /**
     * 当前播放状态
     */
    val playbackState = MutableLiveData<Int>().apply { postValue(Player.STATE_IDLE) }
    
    /**
     * 当前播放的媒体项
     */
    val nowPlaying = MutableLiveData<MediaItem>().apply { postValue(NOTHING_PLAYING) }
    
    /**
     * 是否正在播放
     */
    val isPlaying = MutableLiveData<Boolean>().apply { postValue(false) }
    
    /**
     * 当前播放位置（毫秒）
     */
    val playbackPosition = MutableLiveData<Long>().apply { postValue(0L) }
    
    // MediaController和MediaBrowser
    private var mediaController: MediaController? = null
    private var mediaBrowser: MediaBrowser? = null
    
    // 根媒体ID
    var rootMediaId: String = ""
        private set
    
    init {
        connectToService()
    }
    
    /**
     * 连接到音乐服务
     */
    private fun connectToService() {
        // 创建MediaController
        val controllerFuture: ListenableFuture<MediaController> = 
            MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                mediaController?.addListener(MediaControllerListener())
                isConnected.postValue(true)
                
                // 初始化状态
                updatePlaybackState()
                updateNowPlaying()
            } catch (e: Exception) {
                isConnected.postValue(false)
            }
        }, context.mainExecutor)
        
        // 创建MediaBrowser
        val browserFuture: ListenableFuture<MediaBrowser> = 
            MediaBrowser.Builder(context, sessionToken).buildAsync()
        
        browserFuture.addListener({
            try {
                mediaBrowser = browserFuture.get()
                
                // 获取根媒体ID
                serviceScope.launch {
                    try {
                        val rootResult = mediaBrowser?.getLibraryRoot(null)?.get()
                        rootResult?.value?.let { rootItem ->
                            rootMediaId = rootItem.mediaId
                        }
                    } catch (e: Exception) {
                        // 处理获取根ID失败的情况
                    }
                }
            } catch (e: Exception) {
                // 处理MediaBrowser连接失败
            }
        }, context.mainExecutor)
    }
    
    /**
     * MediaController事件监听器
     */
    private inner class MediaControllerListener : Player.Listener {
        
        override fun onPlaybackStateChanged(playbackState: Int) {
            this@MusicServiceConnection.playbackState.postValue(playbackState)
            updatePlaybackState()
        }
        
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            this@MusicServiceConnection.isPlaying.postValue(isPlaying)
        }
        
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            nowPlaying.postValue(mediaItem ?: NOTHING_PLAYING)
            updateNowPlaying()
        }
        
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updatePlaybackPosition()
        }
        
        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            // 可以根据错误类型设置networkFailure
            if (error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
                networkFailure.postValue(true)
            }
        }
    }
    
    /**
     * 更新播放状态
     */
    private fun updatePlaybackState() {
        mediaController?.let { controller ->
            playbackState.postValue(controller.playbackState)
            isPlaying.postValue(controller.isPlaying)
            updatePlaybackPosition()
        }
    }
    
    /**
     * 更新当前播放项
     */
    private fun updateNowPlaying() {
        mediaController?.let { controller ->
            val currentItem = controller.currentMediaItem
            nowPlaying.postValue(currentItem ?: NOTHING_PLAYING)
        }
    }
    
    /**
     * 更新播放位置
     */
    private fun updatePlaybackPosition() {
        mediaController?.let { controller ->
            playbackPosition.postValue(controller.currentPosition)
        }
    }
    
    /**
     * 播放控制接口
     */
    val transportControls = object {
        fun play() = mediaController?.play()
        fun pause() = mediaController?.pause()
        fun stop() = mediaController?.stop()
        fun skipToNext() = mediaController?.seekToNextMediaItem()
        fun skipToPrevious() = mediaController?.seekToPreviousMediaItem()
        fun seekTo(position: Long) = mediaController?.seekTo(position)
        fun playFromMediaId(mediaId: String) {
            val mediaItem = MediaItem.Builder().setMediaId(mediaId).build()
            mediaController?.setMediaItem(mediaItem)
            mediaController?.prepare()
            mediaController?.play()
        }
        fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int = 0) {
            mediaController?.setMediaItems(mediaItems, startIndex, 0)
            mediaController?.prepare()
        }
    }
    
    /**
     * 订阅指定父ID的子项
     */
    fun subscribe(parentId: String, callback: SubscriptionCallback) {
        serviceScope.launch {
            try {
                val result = mediaBrowser?.getChildren(parentId, 0, 50, null)?.get()
                result?.value?.let { children ->
                    val childrenList = mutableListOf<MediaItem>()
                    for (i in 0 until children.size) {
                        childrenList.add(children[i])
                    }
                    callback.onChildrenLoaded(parentId, childrenList)
                } ?: callback.onError(parentId)
            } catch (e: Exception) {
                callback.onError(parentId)
            }
        }
    }
    
    /**
     * 取消订阅（在Media3中不需要显式取消订阅）
     */
    fun unsubscribe(parentId: String, callback: SubscriptionCallback) {
        // Media3中不需要显式取消订阅
    }
    
    /**
     * 搜索媒体项
     */
    fun search(query: String, callback: SearchCallback) {
        serviceScope.launch {
            try {
                // 简化搜索实现，返回空列表
                // 实际实现需要根据具体的Media3搜索API调整
                callback.onSearchResult(query, emptyList())
            } catch (e: Exception) {
                callback.onError(query)
            }
        }
    }
    
    /**
     * 发送自定义命令
     */
    fun sendCommand(command: String, parameters: Bundle?): Boolean {
        return try {
            mediaController?.let { controller ->
                // Media3中可以通过SessionCommand发送自定义命令
                // 这里简化处理，实际使用时需要根据具体命令实现
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 发送自定义命令并接收结果
     */
    fun sendCommand(
        command: String,
        parameters: Bundle?,
        resultCallback: (Int, Bundle?) -> Unit
    ): Boolean {
        return sendCommand(command, parameters).also { success ->
            if (success) {
                // 简化处理，实际应该等待命令执行结果
                resultCallback(0, null)
            }
        }
    }
    
    // ========== 直接播放控制方法 ==========
    
    /**
     * 播放
     */
    fun play() = mediaController?.play()
    
    /**
     * 暂停
     */
    fun pause() = mediaController?.pause()
    
    /**
     * 停止
     */
    fun stop() = mediaController?.stop()
    
    /**
     * 下一首
     */
    fun skipToNext() = mediaController?.seekToNextMediaItem()
    
    /**
     * 上一首
     */
    fun skipToPrevious() = mediaController?.seekToPreviousMediaItem()
    
    /**
     * 跳转到指定位置
     */
    fun seekTo(position: Long) = mediaController?.seekTo(position)
    
    /**
     * 播放指定媒体项
     */
    fun playFromMediaId(mediaId: String) {
        val mediaItem = MediaItem.Builder().setMediaId(mediaId).build()
        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
        mediaController?.play()
    }
    
    /**
     * 设置播放列表
     */
    fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int = 0) {
        mediaController?.setMediaItems(mediaItems, startIndex, 0)
        mediaController?.prepare()
    }
    
    /**
     * 释放资源
     */
    fun release() {
        mediaController?.release()
        mediaBrowser?.release()
        mediaController = null
        mediaBrowser = null
        isConnected.postValue(false)
    }
    
    /**
     * 订阅回调接口
     */
    interface SubscriptionCallback {
        fun onChildrenLoaded(parentId: String, children: List<MediaItem>)
        fun onError(parentId: String)
    }
    
    /**
     * 搜索回调接口
     */
    interface SearchCallback {
        fun onSearchResult(query: String, results: List<MediaItem>)
        fun onError(query: String)
    }
    
    companion object {
        // 单例实现
        @Volatile
        private var instance: MusicServiceConnection? = null
        
        fun getInstance(context: Context, serviceComponent: ComponentName): MusicServiceConnection {
            return instance ?: synchronized(this) {
                instance ?: MusicServiceConnection(context, serviceComponent).also { instance = it }
            }
        }
        
        /**
         * 便捷方法：获取与MusicService的连接
         */
        @OptIn(UnstableApi::class)
        fun getInstance(context: Context): MusicServiceConnection {
            val serviceComponent = ComponentName(context, MusicService::class.java)
            return getInstance(context, serviceComponent)
        }
    }
}

/**
 * 空播放状态常量
 */
val EMPTY_PLAYBACK_STATE: Int = Player.STATE_IDLE

/**
 * 无播放内容常量
 */
val NOTHING_PLAYING: MediaItem = MediaItem.Builder()
    .setMediaId("")
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle("")
            .setDurationMs(0)
            .build()
    )
    .build()