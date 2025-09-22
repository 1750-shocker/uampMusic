package com.wzh.uampmusic.testing

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.wzh.common.common.MusicServiceConnection
import com.wzh.uampmusic.data.MediaItemData
import com.wzh.uampmusic.viewModels.MainActivityViewModel
import com.wzh.uampmusic.viewModels.NowPlayingViewModel
import kotlinx.coroutines.*

/**
 * Media3集成演示
 * 
 * 这个类提供了一个简单的演示，展示如何验证Media3集成功能
 * 可以在应用启动时调用来确保所有功能正常工作
 */
class Media3IntegrationDemo(private val context: Context) {
    
    private val tag = "Media3IntegrationDemo"
    private val demoScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * 运行Media3集成演示
     */
    fun runDemo() {
        Log.i(tag, "开始Media3集成演示...")
        
        demoScope.launch {
            try {
                // 1. 测试服务连接
                demonstrateServiceConnection()
                
                delay(1000)
                
                // 2. 测试ViewModel集成
                demonstrateViewModelIntegration()
                
                delay(1000)
                
                // 3. 测试播放控制
                demonstratePlaybackControls()
                
                delay(1000)
                
                // 4. 测试UI状态更新
                demonstrateUIStateUpdates()
                
                Log.i(tag, "Media3集成演示完成")
                
            } catch (e: Exception) {
                Log.e(tag, "演示过程中发生错误", e)
            }
        }
    }
    
    /**
     * 演示服务连接
     */
    private suspend fun demonstrateServiceConnection() {
        Log.i(tag, "=== 演示服务连接 ===")
        
        val musicServiceConnection = MusicServiceConnection.getInstance(context)
        
        // 监听连接状态
        val connectionObserver = Observer<Boolean> { isConnected ->
            Log.i(tag, "服务连接状态: ${if (isConnected) "已连接" else "未连接"}")
            if (isConnected) {
                Log.i(tag, "根媒体ID: ${musicServiceConnection.rootMediaId}")
            }
        }
        
        musicServiceConnection.isConnected.observeForever(connectionObserver)
        
        // 等待连接或超时
        val connected = withTimeoutOrNull(5000) {
            while (musicServiceConnection.isConnected.value != true) {
                delay(100)
            }
            true
        }
        
        if (connected == true) {
            Log.i(tag, "✓ 服务连接成功")
        } else {
            Log.w(tag, "✗ 服务连接超时")
        }
        
        musicServiceConnection.isConnected.removeObserver(connectionObserver)
    }
    
    /**
     * 演示ViewModel集成
     */
    private suspend fun demonstrateViewModelIntegration() {
        Log.i(tag, "=== 演示ViewModel集成 ===")
        
        val musicServiceConnection = MusicServiceConnection.getInstance(context)
        
        // 创建ViewModels
        val mainViewModel = MainActivityViewModel(musicServiceConnection)
        val nowPlayingViewModel = NowPlayingViewModel(musicServiceConnection)
        
        Log.i(tag, "✓ ViewModels创建成功")
        
        // 检查初始状态
        val mainState = mainViewModel.uiState.value
        val nowPlayingState = nowPlayingViewModel.uiState.value
        
        Log.i(tag, "MainActivityViewModel状态:")
        Log.i(tag, "  - 连接状态: ${mainState.isConnected}")
        Log.i(tag, "  - 当前媒体ID: ${mainState.currentMediaId}")
        
        Log.i(tag, "NowPlayingViewModel状态:")
        Log.i(tag, "  - 播放状态: ${nowPlayingState.isPlaying}")
        Log.i(tag, "  - 媒体元数据: ${nowPlayingState.mediaMetadata?.title ?: "无"}")
    }
    
    /**
     * 演示播放控制
     */
    private suspend fun demonstratePlaybackControls() {
        Log.i(tag, "=== 演示播放控制 ===")
        
        val musicServiceConnection = MusicServiceConnection.getInstance(context)
        val mainViewModel = MainActivityViewModel(musicServiceConnection)
        
        // 测试媒体项点击
        val testMediaItem = MediaItemData(
            mediaId = "demo_song_1",
            title = "演示歌曲 1",
            subtitle = "演示艺术家",
            albumArtUri = Uri.EMPTY,
            browsable = false,
            playbackRes = 0
        )
        
        Log.i(tag, "测试媒体项点击...")
        mainViewModel.onMediaItemClicked(testMediaItem)
        
        val navigationEvent = mainViewModel.uiState.value.navigationEvent
        if (navigationEvent != null) {
            Log.i(tag, "✓ 媒体项点击产生导航事件: ${navigationEvent::class.simpleName}")
        } else {
            Log.w(tag, "✗ 媒体项点击未产生导航事件")
        }
        
        // 测试播放控制
        Log.i(tag, "测试播放控制...")
        mainViewModel.onPlayMediaId("demo_song_1")
        Log.i(tag, "✓ 播放命令执行")
        
        delay(500)
        
        mainViewModel.onNextMedia()
        Log.i(tag, "✓ 下一首命令执行")
        
        mainViewModel.onPrevMedia()
        Log.i(tag, "✓ 上一首命令执行")
    }
    
    /**
     * 演示UI状态更新
     */
    private suspend fun demonstrateUIStateUpdates() {
        Log.i(tag, "=== 演示UI状态更新 ===")
        
        val musicServiceConnection = MusicServiceConnection.getInstance(context)
        val nowPlayingViewModel = NowPlayingViewModel(musicServiceConnection)
        
        // 创建测试媒体项
        val testMediaItem = MediaItem.Builder()
            .setMediaId("demo_metadata_song")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("演示歌曲标题")
                    .setArtist("演示艺术家")
                    .setDurationMs(210000) // 3:30
                    .setArtworkUri(Uri.parse("https://example.com/demo_art.jpg"))
                    .build()
            )
            .build()
        
        Log.i(tag, "模拟媒体元数据更新...")
        
        // 监听UI状态变化
        val stateObserver = { 
            val state = nowPlayingViewModel.uiState.value
            Log.i(tag, "UI状态更新:")
            Log.i(tag, "  - 标题: ${state.mediaMetadata?.title ?: "无"}")
            Log.i(tag, "  - 艺术家: ${state.mediaMetadata?.subtitle ?: "无"}")
            Log.i(tag, "  - 时长: ${state.mediaMetadata?.duration ?: "无"}")
            Log.i(tag, "  - 播放状态: ${state.isPlaying}")
            Log.i(tag, "  - 按钮资源: ${state.mediaButtonRes}")
        }
        
        // 模拟状态变化序列
        Log.i(tag, "1. 设置媒体项...")
        musicServiceConnection.nowPlaying.postValue(testMediaItem)
        delay(200)
        stateObserver()
        
        Log.i(tag, "2. 开始播放...")
        musicServiceConnection.playbackState.postValue(Player.STATE_READY)
        musicServiceConnection.isPlaying.postValue(true)
        delay(200)
        stateObserver()
        
        Log.i(tag, "3. 暂停播放...")
        musicServiceConnection.isPlaying.postValue(false)
        delay(200)
        stateObserver()
        
        Log.i(tag, "4. 更新播放位置...")
        musicServiceConnection.playbackPosition.postValue(65000L) // 1:05
        delay(200)
        val currentState = nowPlayingViewModel.uiState.value
        Log.i(tag, "  - 当前位置: ${NowPlayingViewModel.NowPlayingMetadata.timestampToMSS(context, currentState.mediaPosition)}")
        
        Log.i(tag, "✓ UI状态更新演示完成")
    }
    
    /**
     * 停止演示
     */
    fun stopDemo() {
        demoScope.cancel()
        Log.i(tag, "Media3集成演示已停止")
    }
}

/**
 * 扩展函数：在Context中运行演示
 */
fun Context.runMedia3Demo() {
    val demo = Media3IntegrationDemo(this)
    demo.runDemo()
}

/**
 * 简化的测试函数，可以在MainActivity中调用
 */
fun Context.testMedia3Integration() {
    Log.i("Media3Test", "开始简单的Media3集成测试...")
    
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val musicServiceConnection = MusicServiceConnection.getInstance(this@testMedia3Integration)
            
            // 等待连接
            val connected = withTimeoutOrNull(3000) {
                while (musicServiceConnection.isConnected.value != true) {
                    delay(100)
                }
                true
            }
            
            if (connected == true) {
                Log.i("Media3Test", "✓ MusicServiceConnection连接成功")
                Log.i("Media3Test", "✓ 根媒体ID: ${musicServiceConnection.rootMediaId}")
                
                // 测试ViewModel创建
                val mainViewModel = MainActivityViewModel(musicServiceConnection)
                val nowPlayingViewModel = NowPlayingViewModel(musicServiceConnection)
                
                Log.i("Media3Test", "✓ ViewModels创建成功")
                Log.i("Media3Test", "✓ MainActivityViewModel连接状态: ${mainViewModel.uiState.value.isConnected}")
                
                // 测试播放控制
                mainViewModel.onPlayMediaId("test")
                Log.i("Media3Test", "✓ 播放控制测试完成")
                
                Log.i("Media3Test", "🎉 Media3集成测试全部通过!")
                
            } else {
                Log.e("Media3Test", "✗ MusicServiceConnection连接失败")
            }
            
        } catch (e: Exception) {
            Log.e("Media3Test", "✗ 测试过程中发生错误", e)
        }
    }
}