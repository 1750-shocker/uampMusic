package com.wzh.common.media.ext

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

/**
 * 扩展方法使用示例
 */
class ExtensionsExample(private val context: Context) {

    /**
     * 示例：String扩展方法的使用
     */
    fun demonstrateStringExtensions() {
        println("=== String扩展方法示例 ===")
        
        // 不区分大小写的包含检查
        val title1 = "Rock Music"
        val title2 = "ROCK music"
        println("'$title1' contains 'rock': ${title1.containsCaseInsensitive("rock")}")
        println("'$title2' contains 'rock': ${title2.containsCaseInsensitive("rock")}")
        
        // URL编码
        val songTitle = "My Song & More"
        println("原始标题: $songTitle")
        println("URL编码后: ${songTitle.urlEncoded}")
        
        // 字符串转URI
        val uriString = "https://example.com/music.mp3"
        val uri = uriString.toUri()
        println("字符串转URI: $uri")
        
        // null字符串处理
        val nullString: String? = null
        println("null字符串转URI: ${nullString.toUri()}")
    }

    /**
     * 示例：MediaItem扩展方法的使用
     */
    fun demonstrateMediaItemExtensions() {
        println("=== MediaItem扩展方法示例 ===")
        
        // 创建可浏览的MediaItem
        val browsableItem = createBrowsableMediaItem(
            mediaId = "albums",
            title = "专辑",
            subtitle = "浏览所有专辑",
            artworkUri = Uri.parse("https://example.com/albums.jpg")
        )
        
        println("可浏览项目:")
        println("  ID: ${browsableItem.mediaId}")
        println("  标题: ${browsableItem.mediaMetadata.title}")
        println("  可浏览: ${browsableItem.mediaMetadata.isBrowsable}")
        println("  可播放: ${browsableItem.mediaMetadata.isPlayable}")
        
        // 创建可播放的MediaItem
        val playableItem = createPlayableMediaItem(
            mediaId = "song_1",
            uri = Uri.parse("https://example.com/song1.mp3"),
            title = "我的歌曲",
            artist = "艺术家",
            album = "专辑名称",
            artworkUri = Uri.parse("https://example.com/artwork1.jpg"),
            durationMs = 240000L
        )
        
        println("\n可播放项目:")
        println("  ID: ${playableItem.mediaId}")
        println("  标题: ${playableItem.mediaMetadata.title}")
        println("  艺术家: ${playableItem.mediaMetadata.artist}")
        println("  专辑: ${playableItem.mediaMetadata.albumTitle}")
        println("  时长: ${playableItem.mediaMetadata.duration}ms")
        println("  可浏览: ${playableItem.mediaMetadata.isBrowsable}")
        println("  可播放: ${playableItem.mediaMetadata.isPlayable}")
    }

    /**
     * 示例：MediaMetadata.Builder扩展方法的使用
     */
    fun demonstrateMediaMetadataBuilder() {
        println("=== MediaMetadata.Builder扩展方法示例 ===")
        
        val metadata = MediaMetadata.Builder().apply {
            // 使用扩展属性设置值
            duration = 180000L
            trackNumber = 5L
            trackCount = 12L
            displayTitle = "显示标题"
            displaySubtitle = "显示副标题"
            albumArtUri = "https://example.com/art.jpg"
        }.build()
        
        println("构建的元数据:")
        println("  时长: ${metadata.duration}ms")
        println("  曲目号: ${metadata.trackNumber}")
        println("  总曲目数: ${metadata.trackCount}")
        println("  显示标题: ${metadata.displayTitle}")
        println("  显示副标题: ${metadata.subtitle}")
        println("  专辑封面URI: ${metadata.albumArtUri}")
    }

    /**
     * 示例：Player扩展方法的使用
     */
    fun demonstratePlayerExtensions() {
        println("=== Player扩展方法示例 ===")
        
        val player = ExoPlayer.Builder(context).build()
        
        // 添加一些示例媒体项
        val mediaItems = listOf(
            createPlayableMediaItem(
                "song1", 
                Uri.parse("https://example.com/song1.mp3"),
                "歌曲1", "艺术家1", "专辑1"
            ),
            createPlayableMediaItem(
                "song2", 
                Uri.parse("https://example.com/song2.mp3"),
                "歌曲2", "艺术家2", "专辑2"
            )
        )
        
        player.setMediaItems(mediaItems)
        player.prepare()
        
        // 使用扩展属性检查状态
        println("播放器状态:")
        println("  是否准备就绪: ${player.isPrepared}")
        println("  是否正在播放: ${player.isPlaying}")
        println("  状态名称: ${player.stateName}")
        println("  是否可以播放: ${player.isPlayEnabled}")
        println("  是否可以暂停: ${player.isPauseEnabled}")
        println("  是否可以跳到下一首: ${player.isSkipToNextEnabled}")
        println("  是否可以跳到上一首: ${player.isSkipToPreviousEnabled}")
        println("  当前位置: ${player.currentPlayBackPosition}ms")
        println("  播放进度: ${(player.playbackProgress * 100).toInt()}%")
        println("  是否有媒体项: ${player.hasMediaItems}")
        println("  当前媒体标题: ${player.currentMediaTitle}")
        println("  当前媒体艺术家: ${player.currentMediaArtist}")
        
        // 使用扩展方法控制播放
        player.playPause() // 播放/暂停切换
        player.safeSeekToNext() // 安全跳转到下一首
        player.safeSeekToPrevious() // 安全跳转到上一首
        
        // 添加简单的播放器监听器
        val listener = SimplePlayerListener(
            onPlaybackStateChanged = { state ->
                println("播放状态改变: $state")
            },
            onIsPlayingChanged = { isPlaying ->
                println("播放状态: ${if (isPlaying) "播放中" else "已暂停"}")
            },
            onMediaItemTransition = { mediaItem ->
                println("媒体项切换: ${mediaItem?.mediaMetadata?.title}")
            }
        )
        
        player.addListener(listener)
        
        // 清理资源
        player.release()
    }

    /**
     * 示例：File扩展方法的使用
     */
    fun demonstrateFileExtensions() {
        println("=== File扩展方法示例 ===")
        
        val albumArtFile = File(context.cacheDir, "album_art.jpg")
        val contentUri = albumArtFile.asAlbumArtContentUri()
        
        println("文件路径: ${albumArtFile.absolutePath}")
        println("Content URI: $contentUri")
    }

    /**
     * 运行所有示例
     */
    fun runAllExamples() {
        demonstrateStringExtensions()
        println("\n" + "=".repeat(50) + "\n")
        
        demonstrateMediaItemExtensions()
        println("\n" + "=".repeat(50) + "\n")
        
        demonstrateMediaMetadataBuilder()
        println("\n" + "=".repeat(50) + "\n")
        
        demonstratePlayerExtensions()
        println("\n" + "=".repeat(50) + "\n")
        
        demonstrateFileExtensions()
    }
}

/**
 * 扩展方法的优势说明：
 * 
 * 1. **向后兼容性**：
 *    - 保持与旧代码相似的API
 *    - 减少迁移成本
 * 
 * 2. **类型安全**：
 *    - 使用Media3的类型系统
 *    - 编译时检查错误
 * 
 * 3. **便捷性**：
 *    - 简化常用操作
 *    - 减少样板代码
 * 
 * 4. **可读性**：
 *    - 更直观的属性访问
 *    - 更清晰的方法命名
 * 
 * 5. **功能增强**：
 *    - 添加了新的便捷方法
 *    - 支持Media3的新特性
 */