package com.wzh.common.media.library

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem

/**
 * BrowseTree使用示例
 */
class BrowseTreeExample(private val context: Context) {
    
    private var browseTree: BrowseTree? = null
    private val repository = MusicRepository()
    
    /**
     * 初始化浏览树
     */
    suspend fun initializeBrowseTree(jsonUrl: String, recentMediaId: String? = null) {
        // 首先加载音乐数据
        repository.loadMusicFromJson(jsonUrl)
        
        // 等待数据加载完成
        repository.musicItems.collect { musicItems ->
            if (musicItems.isNotEmpty()) {
                // 创建音乐源
                val musicSource = SimpleMusicSource(musicItems)
                
                // 创建浏览树
                browseTree = BrowseTree(
                    context = context,
                    musicSource = musicSource,
                    recentMediaId = recentMediaId
                )
                
                println("浏览树初始化完成")
                demonstrateBrowsing()
            }
        }
    }
    
    /**
     * 演示浏览功能
     */
    private fun demonstrateBrowsing() {
        val tree = browseTree ?: return
        
        println("=== 媒体浏览树演示 ===")
        
        // 浏览根节点
        println("\n1. 根节点内容:")
        val rootItems = tree[UAMP_BROWSABLE_ROOT]
        rootItems?.forEach { item ->
            println("  - ${item.mediaMetadata.title} (ID: ${item.mediaId})")
        }
        
        // 浏览推荐内容
        println("\n2. 推荐内容:")
        val recommendedItems = tree[UAMP_RECOMMENDED_ROOT]
        recommendedItems?.forEach { item ->
            println("  - ${item.mediaMetadata.title} by ${item.mediaMetadata.artist}")
        }
        
        // 浏览专辑列表
        println("\n3. 专辑列表:")
        val albumItems = tree[UAMP_ALBUMS_ROOT]
        albumItems?.forEach { album ->
            println("  - 专辑: ${album.mediaMetadata.title} by ${album.mediaMetadata.artist}")
            
            // 浏览专辑内的歌曲
            val albumSongs = tree[album.mediaId]
            albumSongs?.forEach { song ->
                println("    * ${song.mediaMetadata.title}")
            }
        }
        
        // 浏览最近播放
        println("\n4. 最近播放:")
        val recentItems = tree[UAMP_RECENT_ROOT]
        if (recentItems?.isNotEmpty() == true) {
            recentItems.forEach { item ->
                println("  - ${item.mediaMetadata.title} by ${item.mediaMetadata.artist}")
            }
        } else {
            println("  - 暂无最近播放记录")
        }
    }
    
    /**
     * 根据媒体ID获取子项
     */
    fun getChildren(mediaId: String): List<MediaItem>? {
        return browseTree?.get(mediaId)
    }
    
    /**
     * 检查是否支持搜索
     */
    fun isSearchSupported(): Boolean {
        return browseTree?.searchableByUnknownCaller == true
    }
}

/**
 * 简单的音乐源实现，用于BrowseTree
 */
private class SimpleMusicSource(private val items: List<MediaItem>) : AbstractMusicSource() {
    override fun iterator(): Iterator<MediaItem> = items.iterator()
    
    override suspend fun load() {
        state = STATE_INITIALIZING
        // 模拟加载过程
        kotlinx.coroutines.delay(100)
        state = STATE_INITIALIZED
    }
}