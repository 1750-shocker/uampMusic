package com.wzh.common.media.library

import android.net.Uri
import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 音乐仓库类，管理音乐数据源
 */
class MusicRepository {
    
    private val _musicItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val musicItems: StateFlow<List<MediaItem>> = _musicItems.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var musicSource: JsonSource? = null
    
    /**
     * 从JSON URL加载音乐数据
     */
    suspend fun loadMusicFromJson(jsonUrl: String) {
        _isLoading.value = true
        _error.value = null
        
        try {
            val uri = Uri.parse(jsonUrl)
            musicSource = JsonSource(uri)
            musicSource?.load()
            
            when (musicSource?.state) {
                AbstractMusicSource.STATE_INITIALIZED -> {
                    _musicItems.value = musicSource?.toList() ?: emptyList()
                }
                AbstractMusicSource.STATE_ERROR -> {
                    _error.value = "Failed to load music from $jsonUrl"
                }
            }
        } catch (e: Exception) {
            _error.value = "Error loading music: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * 根据ID获取音乐项
     */
    fun getMusicItemById(id: String): MediaItem? {
        return _musicItems.value.find { it.mediaId == id }
    }
    
    /**
     * 清除数据
     */
    fun clear() {
        _musicItems.value = emptyList()
        _error.value = null
    }
}