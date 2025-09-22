package com.wzh.uampmusic.testing

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.wzh.common.common.MusicServiceConnection
import com.wzh.common.common.NOTHING_PLAYING
import com.wzh.uampmusic.data.MediaItemData
import com.wzh.uampmusic.viewModels.MainActivityViewModel
import com.wzh.uampmusic.viewModels.NowPlayingViewModel
import kotlinx.coroutines.*

/**
 * Media3集成功能验证器
 * 
 * 这个类用于在实际运行时验证Media3集成功能
 * 可以在开发和调试时使用，确保所有组件正确工作
 */
class Media3IntegrationValidator(
    private val context: Context,
    private val musicServiceConnection: MusicServiceConnection
) {
    
    private val tag = "Media3IntegrationValidator"
    private val validationScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // 验证结果
    data class ValidationResult(
        val testName: String,
        val success: Boolean,
        val message: String,
        val details: String? = null
    )
    
    private val validationResults = mutableListOf<ValidationResult>()
    
    /**
     * 执行完整的Media3集成验证
     */
    suspend fun validateMedia3Integration(): List<ValidationResult> {
        Log.i(tag, "开始Media3集成功能验证...")
        
        validationResults.clear()
        
        try {
            // 1. 验证服务连接
            validateServiceConnection()
            
            // 2. 验证ViewModel创建和状态管理
            validateViewModelIntegration()
            
            // 3. 验证播放控制功能
            validatePlaybackControls()
            
            // 4. 验证媒体元数据处理
            validateMediaMetadata()
            
            // 5. 验证UI状态同步
            validateUIStateSync()
            
            // 6. 验证错误处理
            validateErrorHandling()
            
        } catch (e: Exception) {
            addResult("整体验证", false, "验证过程中发生异常: ${e.message}", e.stackTraceToString())
        }
        
        // 输出验证结果摘要
        logValidationSummary()
        
        return validationResults.toList()
    }
    
    /**
     * 验证服务连接
     */
    private suspend fun validateServiceConnection() {
        Log.i(tag, "验证服务连接...")
        
        try {
            // 检查连接状态
            val isConnected = withTimeoutOrNull(5000) {
                suspendCancellableCoroutine<Boolean> { continuation ->
                    val observer = object : Observer<Boolean> {
                        override fun onChanged(connected: Boolean) {
                            if (connected) {
                                musicServiceConnection.isConnected.removeObserver(this)
                                continuation.resume(true) {}
                            }
                        }
                    }
                    musicServiceConnection.isConnected.observeForever(observer)
                    
                    // 如果已经连接，立即返回
                    if (musicServiceConnection.isConnected.value == true) {
                        musicServiceConnection.isConnected.removeObserver(observer)
                        continuation.resume(true) {}
                    }
                }
            }
            
            if (isConnected == true) {
                addResult("服务连接", true, "MusicServiceConnection连接成功")
                
                // 验证根媒体ID
                val rootMediaId = musicServiceConnection.rootMediaId
                if (rootMediaId.isNotEmpty()) {
                    addResult("根媒体ID", true, "根媒体ID获取成功: $rootMediaId")
                } else {
                    addResult("根媒体ID", false, "根媒体ID为空")
                }
            } else {
                addResult("服务连接", false, "MusicServiceConnection连接超时")
            }
            
        } catch (e: Exception) {
            addResult("服务连接", false, "服务连接验证失败: ${e.message}")
        }
    }
    
    /**
     * 验证ViewModel集成
     */
    private suspend fun validateViewModelIntegration() {
        Log.i(tag, "验证ViewModel集成...")
        
        try {
            // 创建MainActivityViewModel
            val mainViewModel = MainActivityViewModel(musicServiceConnection)
            
            // 验证初始状态
            val initialState = mainViewModel.uiState.value
            addResult("MainActivityViewModel创建", true, "ViewModel创建成功")
            
            // 验证状态更新
            delay(1000) // 等待状态更新
            val updatedState = mainViewModel.uiState.value
            
            if (updatedState.isConnected) {
                addResult("MainActivityViewModel状态", true, "ViewModel状态正确更新")
            } else {
                addResult("MainActivityViewModel状态", false, "ViewModel状态未正确更新")
            }
            
            // 创建NowPlayingViewModel
            val nowPlayingViewModel = NowPlayingViewModel(musicServiceConnection)
            addResult("NowPlayingViewModel创建", true, "NowPlayingViewModel创建成功")
            
        } catch (e: Exception) {
            addResult("ViewModel集成", false, "ViewModel集成验证失败: ${e.message}")
        }
    }
    
    /**
     * 验证播放控制功能
     */
    private suspend fun validatePlaybackControls() {
        Log.i(tag, "验证播放控制功能...")
        
        try {
            val mainViewModel = MainActivityViewModel(musicServiceConnection)
            
            // 测试播放控制
            val testMediaId = "test_media_id"
            
            // 记录初始播放状态
            val initialPlaybackState = musicServiceConnection.playbackState.value
            val initialIsPlaying = musicServiceConnection.isPlaying.value
            
            // 测试播放命令
            mainViewModel.onPlayMediaId(testMediaId)
            addResult("播放控制", true, "播放命令执行成功")
            
            // 测试上一首/下一首
            mainViewModel.onPrevMedia()
            addResult("上一首控制", true, "上一首命令执行成功")
            
            mainViewModel.onNextMedia()
            addResult("下一首控制", true, "下一首命令执行成功")
            
            // 测试媒体项点击
            val testMediaItem = MediaItemData(
                mediaId = "test_item",
                title = "Test Item",
                subtitle = "Test Subtitle",
                albumArtUri = Uri.EMPTY,
                browsable = false,
                playbackRes = 0
            )
            
            mainViewModel.onMediaItemClicked(testMediaItem)
            
            // 验证导航事件
            val navigationEvent = mainViewModel.uiState.value.navigationEvent
            if (navigationEvent != null) {
                addResult("媒体项点击", true, "媒体项点击处理成功")
            } else {
                addResult("媒体项点击", false, "媒体项点击未产生导航事件")
            }
            
        } catch (e: Exception) {
            addResult("播放控制", false, "播放控制验证失败: ${e.message}")
        }
    }
    
    /**
     * 验证媒体元数据处理
     */
    private suspend fun validateMediaMetadata() {
        Log.i(tag, "验证媒体元数据处理...")
        
        try {
            val nowPlayingViewModel = NowPlayingViewModel(musicServiceConnection)
            
            // 创建测试媒体项
            val testMediaItem = MediaItem.Builder()
                .setMediaId("metadata_test")
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle("Test Title")
                        .setArtist("Test Artist")
                        .setDurationMs(180000) // 3分钟
                        .setArtworkUri(Uri.parse("https://example.com/test_art.jpg"))
                        .build()
                )
                .build()
            
            // 模拟媒体项更新
            musicServiceConnection.nowPlaying.postValue(testMediaItem)
            musicServiceConnection.playbackState.postValue(Player.STATE_READY)
            musicServiceConnection.isPlaying.postValue(true)
            
            delay(500) // 等待状态更新
            
            // 验证ViewModel状态
            val uiState = nowPlayingViewModel.uiState.value
            val metadata = uiState.mediaMetadata
            
            if (metadata != null) {
                if (metadata.title == "Test Title" && metadata.subtitle == "Test Artist") {
                    addResult("媒体元数据", true, "媒体元数据正确处理")
                } else {
                    addResult("媒体元数据", false, "媒体元数据内容不正确")
                }
            } else {
                addResult("媒体元数据", false, "媒体元数据为空")
            }
            
            // 验证时间格式化
            val formattedTime = NowPlayingViewModel.NowPlayingMetadata.timestampToMSS(context, 125000L)
            if (formattedTime == "2:05") {
                addResult("时间格式化", true, "时间格式化正确")
            } else {
                addResult("时间格式化", false, "时间格式化错误: $formattedTime")
            }
            
        } catch (e: Exception) {
            addResult("媒体元数据", false, "媒体元数据验证失败: ${e.message}")
        }
    }
    
    /**
     * 验证UI状态同步
     */
    private suspend fun validateUIStateSync() {
        Log.i(tag, "验证UI状态同步...")
        
        try {
            val nowPlayingViewModel = NowPlayingViewModel(musicServiceConnection)
            
            // 测试播放状态变化
            val stateChanges = listOf(
                Triple(Player.STATE_READY, true, "播放状态"),
                Triple(Player.STATE_READY, false, "暂停状态"),
                Triple(Player.STATE_BUFFERING, false, "缓冲状态"),
                Triple(Player.STATE_IDLE, false, "空闲状态")
            )
            
            for ((playbackState, isPlaying, stateName) in stateChanges) {
                musicServiceConnection.playbackState.postValue(playbackState)
                musicServiceConnection.isPlaying.postValue(isPlaying)
                
                delay(200) // 等待状态更新
                
                val uiState = nowPlayingViewModel.uiState.value
                if (uiState.isPlaying == isPlaying) {
                    addResult("UI状态同步-$stateName", true, "$stateName 同步正确")
                } else {
                    addResult("UI状态同步-$stateName", false, "$stateName 同步失败")
                }
            }
            
        } catch (e: Exception) {
            addResult("UI状态同步", false, "UI状态同步验证失败: ${e.message}")
        }
    }
    
    /**
     * 验证错误处理
     */
    private suspend fun validateErrorHandling() {
        Log.i(tag, "验证错误处理...")
        
        try {
            val nowPlayingViewModel = NowPlayingViewModel(musicServiceConnection)
            
            // 测试空媒体项处理
            musicServiceConnection.nowPlaying.postValue(null)
            delay(200)
            
            var uiState = nowPlayingViewModel.uiState.value
            if (uiState.mediaMetadata == null) {
                addResult("空媒体项处理", true, "空媒体项正确处理")
            } else {
                addResult("空媒体项处理", false, "空媒体项处理失败")
            }
            
            // 测试NOTHING_PLAYING处理
            musicServiceConnection.nowPlaying.postValue(NOTHING_PLAYING)
            delay(200)
            
            uiState = nowPlayingViewModel.uiState.value
            if (uiState.mediaMetadata == null) {
                addResult("NOTHING_PLAYING处理", true, "NOTHING_PLAYING正确处理")
            } else {
                addResult("NOTHING_PLAYING处理", false, "NOTHING_PLAYING处理失败")
            }
            
            // 测试连接断开处理
            musicServiceConnection.isConnected.postValue(false)
            delay(200)
            
            val mainViewModel = MainActivityViewModel(musicServiceConnection)
            val mainUiState = mainViewModel.uiState.value
            if (!mainUiState.isConnected) {
                addResult("连接断开处理", true, "连接断开正确处理")
            } else {
                addResult("连接断开处理", false, "连接断开处理失败")
            }
            
        } catch (e: Exception) {
            addResult("错误处理", false, "错误处理验证失败: ${e.message}")
        }
    }
    
    /**
     * 添加验证结果
     */
    private fun addResult(testName: String, success: Boolean, message: String, details: String? = null) {
        val result = ValidationResult(testName, success, message, details)
        validationResults.add(result)
        
        val status = if (success) "✓" else "✗"
        Log.i(tag, "$status $testName: $message")
        details?.let { Log.d(tag, "详细信息: $it") }
    }
    
    /**
     * 输出验证结果摘要
     */
    private fun logValidationSummary() {
        val totalTests = validationResults.size
        val passedTests = validationResults.count { it.success }
        val failedTests = totalTests - passedTests
        
        Log.i(tag, "=== Media3集成验证结果摘要 ===")
        Log.i(tag, "总测试数: $totalTests")
        Log.i(tag, "通过: $passedTests")
        Log.i(tag, "失败: $failedTests")
        Log.i(tag, "成功率: ${(passedTests * 100 / totalTests)}%")
        
        if (failedTests > 0) {
            Log.w(tag, "失败的测试:")
            validationResults.filter { !it.success }.forEach { result ->
                Log.w(tag, "- ${result.testName}: ${result.message}")
            }
        }
        
        Log.i(tag, "=== 验证完成 ===")
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        validationScope.cancel()
    }
}