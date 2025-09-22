package com.wzh.uampmusic.integration

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import com.wzh.common.common.MusicServiceConnection

/**
 * Mock实现的MusicServiceConnection用于测试
 */
class MockMusicServiceConnection : IMusicServiceConnection {
    
    override val isConnected = MutableLiveData<Boolean>()
    override val playbackState = MutableLiveData<Int>()
    override val nowPlaying = MutableLiveData<MediaItem>()
    override val isPlaying = MutableLiveData<Boolean>()
    override val playbackPosition = MutableLiveData<Long>()
    override var rootMediaId = "root"
    override val networkFailure = MutableLiveData<Boolean>()
    
    override fun play() {}
    override fun pause() {}
    override fun stop() {}
    override fun skipToNext() {}
    override fun skipToPrevious() {}
    override fun seekTo(position: Long) {}
    override fun playFromMediaId(mediaId: String) {}
    override fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int) {}
    override fun release() {}
    override fun subscribe(parentId: String, callback: MusicServiceConnection.SubscriptionCallback) {}
    override fun unsubscribe(parentId: String, callback: MusicServiceConnection.SubscriptionCallback) {}
    override fun search(query: String, callback: MusicServiceConnection.SearchCallback) {}
    override fun sendCommand(command: String, parameters: Bundle?): Boolean = true
    override fun sendCommand(command: String, parameters: Bundle?, resultCallback: (Int, Bundle?) -> Unit): Boolean = true
}

/**
 * MusicServiceConnection的接口抽象，用于测试
 */
interface IMusicServiceConnection {
    val isConnected: MutableLiveData<Boolean>
    val playbackState: MutableLiveData<Int>
    val nowPlaying: MutableLiveData<MediaItem>
    val isPlaying: MutableLiveData<Boolean>
    val playbackPosition: MutableLiveData<Long>
    var rootMediaId: String
    val networkFailure: MutableLiveData<Boolean>
    
    fun play()
    fun pause()
    fun stop()
    fun skipToNext()
    fun skipToPrevious()
    fun seekTo(position: Long)
    fun playFromMediaId(mediaId: String)
    fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int)
    fun release()
    fun subscribe(parentId: String, callback: MusicServiceConnection.SubscriptionCallback)
    fun unsubscribe(parentId: String, callback: MusicServiceConnection.SubscriptionCallback)
    fun search(query: String, callback: MusicServiceConnection.SearchCallback)
    fun sendCommand(command: String, parameters: Bundle?): Boolean
    fun sendCommand(command: String, parameters: Bundle?, resultCallback: (Int, Bundle?) -> Unit): Boolean
}