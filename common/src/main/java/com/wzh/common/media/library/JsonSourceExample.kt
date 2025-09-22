package com.wzh.common.media.library

import android.net.Uri
import androidx.media3.common.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * JsonSource使用示例
 */
class JsonSourceExample {
    
    private val repository = MusicRepository()
    
    /**
     * 示例：从JSON URL加载音乐数据
     */
    fun loadMusicExample() {
        // 示例JSON URL（你需要替换为实际的JSON URL）
        val jsonUrl = "https://example.com/music/catalog.json"
        
        CoroutineScope(Dispatchers.Main).launch {
            repository.loadMusicFromJson(jsonUrl)
            
            // 监听加载状态
            repository.isLoading.collect { isLoading ->
                if (isLoading) {
                    println("正在加载音乐数据...")
                } else {
                    println("音乐数据加载完成")
                }
            }
        }
        
        // 监听音乐数据
        CoroutineScope(Dispatchers.Main).launch {
            repository.musicItems.collect { musicItems ->
                println("加载了 ${musicItems.size} 首音乐")
                musicItems.forEach { item ->
                    println("音乐: ${item.mediaMetadata.title} - ${item.mediaMetadata.artist}")
                }
            }
        }
        
        // 监听错误
        CoroutineScope(Dispatchers.Main).launch {
            repository.error.collect { error ->
                error?.let {
                    println("加载错误: $it")
                }
            }
        }
    }
    
    /**
     * 示例：直接使用JsonSource
     */
    suspend fun directJsonSourceExample() {
        val jsonUri = Uri.parse("https://example.com/music/catalog.json")
        val jsonSource = JsonSource(jsonUri)
        
        // 加载数据
        jsonSource.load()
        
        // 检查状态
        when (jsonSource.state) {
            STATE_INITIALIZED -> {
                println("JsonSource加载成功")
                // 遍历音乐项
                for (mediaItem in jsonSource) {
                    println("音乐ID: ${mediaItem.mediaId}")
                    println("标题: ${mediaItem.mediaMetadata.title}")
                    println("艺术家: ${mediaItem.mediaMetadata.artist}")
                    println("专辑: ${mediaItem.mediaMetadata.albumTitle}")
                    println("时长: ${mediaItem.mediaMetadata.durationMs}ms")
                    println("---")
                }
            }
            STATE_ERROR -> {
                println("JsonSource加载失败")
            }
            else -> {
                println("JsonSource状态: ${jsonSource.state}")
            }
        }
    }
}

/**
 * 示例JSON格式：
 * 
 * {
 *   "music": [
 *     {
 *       "id": "1",
 *       "title": "歌曲标题",
 *       "artist": "艺术家",
 *       "album": "专辑名称",
 *       "genre": "流行",
 *       "source": "https://example.com/music/song1.mp3",
 *       "image": "https://example.com/images/album1.jpg",
 *       "trackNumber": 1,
 *       "totalTrackCount": 12,
 *       "duration": 240,
 *       "site": "example.com"
 *     }
 *   ]
 * }
 */