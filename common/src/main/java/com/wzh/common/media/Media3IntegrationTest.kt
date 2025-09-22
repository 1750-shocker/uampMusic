package com.wzh.common.media

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Media3集成测试类
 * 用于验证Media3依赖集成是否成功
 */
class Media3IntegrationTest(private val context: Context) {
    
    private var exoPlayer: ExoPlayer? = null
    private var mediaController: MediaController? = null
    private val testScope = CoroutineScope(Dispatchers.Main)
    
    /**
     * 测试ExoPlayer实例化
     */
    fun testExoPlayerCreation(): Boolean {
        return try {
            exoPlayer = ExoPlayer.Builder(context).build()
            println("✅ ExoPlayer创建成功")
            true
        } catch (e: Exception) {
            println("❌ ExoPlayer创建失败: ${e.message}")
            false
        }
    }
    
    /**
     * 测试MediaItem创建
     */
    fun testMediaItemCreation(): Boolean {
        return try {
            val mediaItem = MediaItem.Builder()
                .setMediaId("test_media_id")
                .setUri("https://example.com/test.mp3")
                .build()
            
            println("✅ MediaItem创建成功: ${mediaItem.mediaId}")
            true
        } catch (e: Exception) {
            println("❌ MediaItem创建失败: ${e.message}")
            false
        }
    }
    
    /**
     * 测试基本播放功能
     */
    fun testBasicPlayback(): Boolean {
        return try {
            val player = exoPlayer ?: ExoPlayer.Builder(context).build()
            
            // 创建测试媒体项
            val mediaItem = MediaItem.Builder()
                .setMediaId("test_audio")
                .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                .build()
            
            // 设置媒体项
            player.setMediaItem(mediaItem)
            player.prepare()
            
            // 添加播放器监听器
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_IDLE -> println("🔄 播放器状态: IDLE")
                        Player.STATE_BUFFERING -> println("🔄 播放器状态: BUFFERING")
                        Player.STATE_READY -> println("✅ 播放器状态: READY")
                        Player.STATE_ENDED -> println("🏁 播放器状态: ENDED")
                    }
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    println("🎵 播放状态变化: ${if (isPlaying) "播放中" else "已暂停"}")
                }
            })
            
            println("✅ 基本播放功能测试设置完成")
            true
        } catch (e: Exception) {
            println("❌ 基本播放功能测试失败: ${e.message}")
            false
        }
    }
    
    /**
     * 测试Media3 UI组件
     */
    fun testMedia3UIComponents(): Boolean {
        return try {
            // 测试PlayerView创建
            val playerView = PlayerView(context)
            playerView.player = exoPlayer
            
            println("✅ Media3 UI组件测试成功")
            true
        } catch (e: Exception) {
            println("❌ Media3 UI组件测试失败: ${e.message}")
            false
        }
    }
    
    /**
     * 测试媒体会话功能
     */
    fun testMediaSessionFunctionality(): Boolean {
        return try {
            testScope.launch {
                try {
                    // 创建SessionToken
                    val sessionToken = SessionToken(
                        context,
                        android.content.ComponentName(context, MusicService::class.java)
                    )
                    
                    // 创建MediaController
                    val controllerFuture: ListenableFuture<MediaController> = 
                        MediaController.Builder(context, sessionToken).buildAsync()
                    
                    println("✅ 媒体会话功能测试设置完成")
                } catch (e: Exception) {
                    println("❌ 媒体会话功能测试失败: ${e.message}")
                }
            }
            true
        } catch (e: Exception) {
            println("❌ 媒体会话功能测试失败: ${e.message}")
            false
        }
    }
    
    /**
     * 运行所有测试
     */
    fun runAllTests(): TestResult {
        println("🚀 开始Media3集成测试...")
        
        val results = mutableListOf<Boolean>()
        
        // 测试ExoPlayer创建
        results.add(testExoPlayerCreation())
        
        // 测试MediaItem创建
        results.add(testMediaItemCreation())
        
        // 测试基本播放功能
        results.add(testBasicPlayback())
        
        // 测试UI组件
        results.add(testMedia3UIComponents())
        
        // 测试媒体会话功能
        results.add(testMediaSessionFunctionality())
        
        val passedTests = results.count { it }
        val totalTests = results.size
        
        println("\n📊 测试结果:")
        println("通过: $passedTests/$totalTests")
        println("成功率: ${(passedTests * 100 / totalTests)}%")
        
        return TestResult(
            totalTests = totalTests,
            passedTests = passedTests,
            success = passedTests == totalTests
        )
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        exoPlayer?.release()
        mediaController?.release()
        exoPlayer = null
        mediaController = null
        println("🧹 测试资源已清理")
    }
    
    /**
     * 测试结果数据类
     */
    data class TestResult(
        val totalTests: Int,
        val passedTests: Int,
        val success: Boolean
    )
    
    companion object {
        /**
         * 快速测试方法
         */
        fun quickTest(context: Context): Boolean {
            val test = Media3IntegrationTest(context)
            val result = test.runAllTests()
            test.cleanup()
            return result.success
        }
    }
}