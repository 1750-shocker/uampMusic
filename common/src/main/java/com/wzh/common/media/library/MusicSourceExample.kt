package com.wzh.common.media.library

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * MusicSource和AbstractMusicSource使用示例
 * 展示状态管理和回调机制的使用
 */
class MusicSourceExample {

    /**
     * 示例：使用JsonSource的状态管理和回调机制
     */
    fun demonstrateStateManagement() {
        val jsonUri = Uri.parse("https://example.com/music/catalog.json")
        val jsonSource = JsonSource(jsonUri)
        
        println("=== MusicSource 状态管理示例 ===")
        
        // 1. 检查初始状态
        println("1. 初始状态: ${getStateString(jsonSource.state)}")
        
        // 2. 注册回调监听器
        val isReady = jsonSource.whenReady { success ->
            if (success) {
                println("✅ 数据源准备就绪！")
                println("   加载了 ${jsonSource.count()} 首音乐")
                
                // 遍历音乐项
                jsonSource.forEachIndexed { index, mediaItem ->
                    println("   ${index + 1}. ${mediaItem.mediaMetadata.title} - ${mediaItem.mediaMetadata.artist}")
                }
            } else {
                println("❌ 数据源加载失败")
            }
        }
        
        if (isReady) {
            println("2. 数据源已经准备就绪")
        } else {
            println("2. 数据源尚未准备就绪，已注册回调")
        }
        
        // 3. 异步加载数据
        CoroutineScope(Dispatchers.Main).launch {
            println("3. 开始加载数据...")
            jsonSource.load()
            println("4. 加载完成，状态: ${getStateString(jsonSource.state)}")
        }
    }

    /**
     * 示例：多个监听器的使用
     */
    fun demonstrateMultipleListeners() {
        val jsonUri = Uri.parse("https://example.com/music/catalog.json")
        val jsonSource = JsonSource(jsonUri)
        
        println("=== 多监听器示例 ===")
        
        // 注册第一个监听器 - 用于UI更新
        jsonSource.whenReady { success ->
            if (success) {
                println("🎵 UI监听器: 更新音乐列表界面")
            } else {
                println("🚫 UI监听器: 显示错误信息")
            }
        }
        
        // 注册第二个监听器 - 用于缓存管理
        jsonSource.whenReady { success ->
            if (success) {
                println("💾 缓存监听器: 开始预缓存专辑封面")
            } else {
                println("💾 缓存监听器: 清理缓存")
            }
        }
        
        // 注册第三个监听器 - 用于分析统计
        jsonSource.whenReady { success ->
            if (success) {
                println("📊 统计监听器: 记录加载成功事件")
            } else {
                println("📊 统计监听器: 记录加载失败事件")
            }
        }
        
        // 开始加载
        CoroutineScope(Dispatchers.Main).launch {
            jsonSource.load()
        }
    }

    /**
     * 示例：搜索功能的使用
     */
    fun demonstrateSearchFunction() {
        val jsonUri = Uri.parse("https://example.com/music/catalog.json")
        val jsonSource = JsonSource(jsonUri)
        
        println("=== 搜索功能示例 ===")
        
        jsonSource.whenReady { success ->
            if (success) {
                // 搜索功能示例
                val searchQuery = "rock"
                val searchExtras = Bundle().apply {
                    putString("genre", "rock")
                    putInt("limit", 10)
                }
                
                val searchResults = jsonSource.search(searchQuery, searchExtras)
                println("搜索 '$searchQuery' 的结果: ${searchResults.size} 首歌曲")
                
                searchResults.forEach { mediaItem ->
                    println("  - ${mediaItem.mediaMetadata.title} by ${mediaItem.mediaMetadata.artist}")
                }
            }
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            jsonSource.load()
        }
    }

    /**
     * 示例：自定义MusicSource实现
     */
    class CustomMusicSource(private val items: List<MediaItem>) : AbstractMusicSource() {
        
        override fun iterator(): Iterator<MediaItem> = items.iterator()
        
        override suspend fun load() {
            state = STATE_INITIALIZING
            
            // 模拟加载过程
            try {
                kotlinx.coroutines.delay(1000) // 模拟网络延迟
                state = STATE_INITIALIZED
            } catch (e: Exception) {
                state = STATE_ERROR
            }
        }
        
        override fun search(query: String, extras: Bundle): List<MediaItem> {
            return items.filter { mediaItem ->
                mediaItem.mediaMetadata.title?.contains(query, ignoreCase = true) == true ||
                mediaItem.mediaMetadata.artist?.contains(query, ignoreCase = true) == true
            }
        }
    }

    /**
     * 示例：使用自定义MusicSource
     */
    fun demonstrateCustomMusicSource() {
        // 创建一些示例MediaItem
        val sampleItems = listOf(
            createSampleMediaItem("1", "Rock Song", "Rock Band", "Rock Album"),
            createSampleMediaItem("2", "Pop Hit", "Pop Star", "Pop Album"),
            createSampleMediaItem("3", "Jazz Classic", "Jazz Master", "Jazz Collection")
        )
        
        val customSource = CustomMusicSource(sampleItems)
        
        println("=== 自定义MusicSource示例 ===")
        
        customSource.whenReady { success ->
            if (success) {
                println("✅ 自定义数据源准备就绪")
                println("   总共 ${customSource.count()} 首歌曲")
                
                // 测试搜索功能
                val rockSongs = customSource.search("rock", Bundle())
                println("   搜索'rock': ${rockSongs.size} 首歌曲")
            }
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            customSource.load()
        }
    }

    /**
     * 创建示例MediaItem
     */
    private fun createSampleMediaItem(id: String, title: String, artist: String, album: String): MediaItem {
        val metadata = androidx.media3.common.MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .build()
        
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(metadata)
            .build()
    }

    /**
     * 获取状态字符串描述
     */
    private fun getStateString(state: Int): String {
        return when (state) {
            STATE_CREATED -> "已创建"
            STATE_INITIALIZING -> "初始化中"
            STATE_INITIALIZED -> "已初始化"
            STATE_ERROR -> "错误"
            else -> "未知状态"
        }
    }

    /**
     * 运行所有示例
     */
    fun runAllExamples() {
        demonstrateStateManagement()
        println("\n" + "=".repeat(50) + "\n")
        
        demonstrateMultipleListeners()
        println("\n" + "=".repeat(50) + "\n")
        
        demonstrateSearchFunction()
        println("\n" + "=".repeat(50) + "\n")
        
        demonstrateCustomMusicSource()
    }
}

/**
 * 状态管理机制说明：
 * 
 * 1. **状态定义**：
 *    - STATE_CREATED: 数据源已创建但未开始加载
 *    - STATE_INITIALIZING: 正在加载数据
 *    - STATE_INITIALIZED: 数据加载成功，可以使用
 *    - STATE_ERROR: 数据加载失败
 * 
 * 2. **回调机制**：
 *    - whenReady()方法用于注册回调函数
 *    - 如果数据源已准备就绪，立即执行回调
 *    - 如果数据源未准备就绪，将回调加入监听列表
 *    - 当状态变为INITIALIZED或ERROR时，自动通知所有监听者
 * 
 * 3. **线程安全**：
 *    - 使用synchronized确保状态变更和监听者通知的线程安全
 *    - 支持多线程环境下的并发访问
 * 
 * 4. **使用场景**：
 *    - UI更新：数据加载完成后更新界面
 *    - 缓存管理：数据准备就绪后开始预缓存
 *    - 错误处理：加载失败时显示错误信息
 *    - 统计分析：记录加载成功/失败事件
 */