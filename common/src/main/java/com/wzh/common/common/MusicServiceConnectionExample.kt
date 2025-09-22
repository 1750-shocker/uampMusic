package com.wzh.common.common

import android.content.Context
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * MusicServiceConnection使用示例
 * 展示如何在Repository层使用连接管理器
 */
class MusicServiceConnectionExample(private val context: Context) {
    
    private val serviceConnection = MusicServiceConnection.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Main)
    
    /**
     * 初始化连接并设置观察者
     */
    fun initializeConnection() {
        println("=== 初始化音乐服务连接 ===")
        
        // 观察连接状态
        serviceConnection.isConnected.observeForever { isConnected ->
            println("🔗 连接状态: ${if (isConnected) "已连接" else "未连接"}")
            
            if (isConnected) {
                println("📁 根媒体ID: ${serviceConnection.rootMediaId}")
                demonstrateBasicUsage()
            }
        }
        
        // 观察播放状态
        serviceConnection.playbackState.observeForever { state ->
            val stateName = when (state) {
                Player.STATE_IDLE -> "空闲"
                Player.STATE_BUFFERING -> "缓冲中"
                Player.STATE_READY -> "准备就绪"
                Player.STATE_ENDED -> "播放结束"
                else -> "未知状态"
            }
            println("🎵 播放状态: $stateName")
        }
        
        // 观察当前播放项
        serviceConnection.nowPlaying.observeForever { mediaItem ->
            if (mediaItem.mediaId.isNotEmpty()) {
                println("🎶 正在播放: ${mediaItem.mediaMetadata.title}")
            } else {
                println("⏹️ 没有播放内容")
            }
        }
        
        // 观察播放/暂停状态
        serviceConnection.isPlaying.observeForever { isPlaying ->
            println("▶️ 播放状态: ${if (isPlaying) "播放中" else "已暂停"}")
        }
        
        // 观察播放位置
        serviceConnection.playbackPosition.observeForever { position ->
            if (position > 0) {
                println("⏰ 播放位置: ${position / 1000}秒")
            }
        }
        
        // 观察网络错误
        serviceConnection.networkFailure.observeForever { hasError ->
            if (hasError) {
                println("❌ 网络连接失败")
            }
        }
    }
    
    /**
     * 演示基本使用功能
     */
    private fun demonstrateBasicUsage() {
        println("\n=== 基本功能演示 ===")
        
        // 浏览媒体库
        demonstrateMediaBrowsing()
        
        // 播放控制
        demonstratePlaybackControls()
        
        // 搜索功能
        demonstrateSearch()
    }
    
    /**
     * 演示媒体浏览功能
     */
    private fun demonstrateMediaBrowsing() {
        println("\n📂 媒体浏览演示:")
        
        // 订阅根目录
        serviceConnection.subscribe(serviceConnection.rootMediaId, object : MusicServiceConnection.SubscriptionCallback {
            override fun onChildrenLoaded(parentId: String, children: List<MediaItem>) {
                println("📁 $parentId 包含 ${children.size} 个子项:")
                children.forEachIndexed { index, child ->
                    val metadata = child.mediaMetadata
                    val type = if (metadata.isBrowsable == true) "📁" else "🎵"
                    println("  ${index + 1}. $type ${metadata.title} (ID: ${child.mediaId})")
                }
                
                // 如果有子项，尝试浏览第一个可浏览的项目
                val firstBrowsableItem = children.find { it.mediaMetadata.isBrowsable == true }
                firstBrowsableItem?.let { item ->
                    println("\n📂 浏览子目录: ${item.mediaMetadata.title}")
                    browseSubDirectory(item.mediaId)
                }
            }
            
            override fun onError(parentId: String) {
                println("❌ 浏览失败: $parentId")
            }
        })
    }
    
    /**
     * 浏览子目录
     */
    private fun browseSubDirectory(parentId: String) {
        serviceConnection.subscribe(parentId, object : MusicServiceConnection.SubscriptionCallback {
            override fun onChildrenLoaded(parentId: String, children: List<MediaItem>) {
                println("  📂 子目录包含 ${children.size} 个项目:")
                children.take(3).forEachIndexed { index, child ->
                    println("    ${index + 1}. 🎵 ${child.mediaMetadata.title}")
                }
                if (children.size > 3) {
                    println("    ... 还有 ${children.size - 3} 个项目")
                }
            }
            
            override fun onError(parentId: String) {
                println("  ❌ 浏览子目录失败: $parentId")
            }
        })
    }
    
    /**
     * 演示播放控制功能
     */
    private fun demonstratePlaybackControls() {
        println("\n🎮 播放控制演示:")
        
        scope.launch {
            // 等待一段时间确保连接稳定
            kotlinx.coroutines.delay(2000)
            
            // 播放特定媒体项
            println("▶️ 开始播放特定歌曲")
            serviceConnection.playFromMediaId("wake_up_01")
            
            kotlinx.coroutines.delay(3000)
            
            // 暂停播放
            println("⏸️ 暂停播放")
            serviceConnection.pause()
            
            kotlinx.coroutines.delay(2000)
            
            // 继续播放
            println("▶️ 继续播放")
            serviceConnection.play()
            
            kotlinx.coroutines.delay(2000)
            
            // 跳转到指定位置
            println("⏩ 跳转到30秒位置")
            serviceConnection.seekTo(30000)
            
            kotlinx.coroutines.delay(2000)
            
            // 下一首
            println("⏭️ 下一首")
            serviceConnection.skipToNext()
        }
    }
    
    /**
     * 演示搜索功能
     */
    private fun demonstrateSearch() {
        println("\n🔍 搜索功能演示:")
        
        serviceConnection.search("rock", object : MusicServiceConnection.SearchCallback {
            override fun onSearchResult(query: String, results: List<MediaItem>) {
                println("🔍 搜索 '$query' 结果: ${results.size} 个项目")
                results.take(5).forEachIndexed { index, item ->
                    println("  ${index + 1}. ${item.mediaMetadata.title} - ${item.mediaMetadata.artist}")
                }
                if (results.size > 5) {
                    println("  ... 还有 ${results.size - 5} 个结果")
                }
            }
            
            override fun onError(query: String) {
                println("❌ 搜索失败: $query")
            }
        })
    }
    
    /**
     * 演示播放列表管理
     */
    fun demonstratePlaylistManagement() {
        println("\n📋 播放列表管理演示:")
        
        // 创建示例播放列表
        val playlist = listOf(
            MediaItem.Builder()
                .setMediaId("song1")
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle("歌曲1")
                        .setArtist("艺术家1")
                        .build()
                )
                .build(),
            MediaItem.Builder()
                .setMediaId("song2")
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle("歌曲2")
                        .setArtist("艺术家2")
                        .build()
                )
                .build(),
            MediaItem.Builder()
                .setMediaId("song3")
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle("歌曲3")
                        .setArtist("艺术家3")
                        .build()
                )
                .build()
        )
        
        // 设置播放列表
        serviceConnection.setMediaItems(playlist, 1) // 从第2首开始播放
        serviceConnection.play()
        
        println("📋 设置播放列表: ${playlist.size} 首歌曲，从第2首开始播放")
    }
    
    /**
     * 演示自定义命令
     */
    fun demonstrateCustomCommands() {
        println("\n🔧 自定义命令演示:")
        
        val success = serviceConnection.sendCommand("custom_command", null) { resultCode, resultData ->
            println("📨 命令执行结果: $resultCode")
        }
        
        if (success) {
            println("✅ 自定义命令发送成功")
        } else {
            println("❌ 自定义命令发送失败")
        }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        println("\n🧹 清理资源")
        serviceConnection.release()
    }
    
    /**
     * 运行完整示例
     */
    fun runCompleteExample() {
        println("🚀 开始MusicServiceConnection完整示例")
        
        // 初始化连接
        initializeConnection()
        
        // 等待连接建立后执行其他操作
        scope.launch {
            kotlinx.coroutines.delay(3000) // 等待连接建立
            
            demonstratePlaylistManagement()
            
            kotlinx.coroutines.delay(5000)
            
            demonstrateCustomCommands()
            
            kotlinx.coroutines.delay(10000)
            
            cleanup()
        }
    }
}

/**
 * MusicServiceConnection的优势说明：
 * 
 * 1. **Repository模式**：
 *    - 作为数据层和UI层之间的桥梁
 *    - 封装复杂的服务连接逻辑
 *    - 提供简洁的API给上层使用
 * 
 * 2. **LiveData集成**：
 *    - 响应式的状态管理
 *    - 自动的生命周期感知
 *    - 线程安全的数据更新
 * 
 * 3. **单例模式**：
 *    - 全局唯一的服务连接
 *    - 避免重复连接开销
 *    - 状态一致性保证
 * 
 * 4. **Media3兼容**：
 *    - 使用最新的Media3 API
 *    - 更好的性能和稳定性
 *    - 现代化的异步处理
 * 
 * 5. **功能完整**：
 *    - 播放控制
 *    - 媒体浏览
 *    - 搜索功能
 *    - 自定义命令支持
 */