package com.wzh.common.media.library

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream

/**
 * AlbumArtContentProvider使用示例
 */
class AlbumArtContentProviderExample(private val context: Context) {

    /**
     * 示例：将网络图片URI转换为content://形式
     */
    fun mapNetworkImageUri() {
        // 原始网络图片URI
        val networkImageUri = Uri.parse("https://example.com/album/cover.jpg")
        
        // 转换为content://形式的URI
        val contentUri = AlbumArtContentProvider.mapUri(networkImageUri)
        
        println("原始URI: $networkImageUri")
        println("Content URI: $contentUri")
        
        // 这个content URI可以用于：
        // 1. MediaItem的artworkUri
        // 2. 通知系统的图标
        // 3. Android Auto的显示
        // 4. 任何需要稳定URI引用的地方
    }

    /**
     * 示例：通过ContentProvider访问专辑封面
     */
    suspend fun accessAlbumArt(networkImageUri: Uri): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                // 将网络URI映射为content URI
                val contentUri = AlbumArtContentProvider.mapUri(networkImageUri)
                
                // 通过ContentResolver打开文件
                val contentResolver = context.contentResolver
                val parcelFileDescriptor: ParcelFileDescriptor? = 
                    contentResolver.openFileDescriptor(contentUri, "r")
                
                parcelFileDescriptor?.use { pfd ->
                    FileInputStream(pfd.fileDescriptor).use { inputStream ->
                        inputStream.readBytes()
                    }
                }
            } catch (e: Exception) {
                println("访问专辑封面失败: ${e.message}")
                null
            }
        }
    }

    /**
     * 示例：批量处理专辑封面URI
     */
    fun batchProcessAlbumArt(networkUris: List<String>): List<Uri> {
        return networkUris.mapNotNull { uriString ->
            try {
                val networkUri = Uri.parse(uriString)
                AlbumArtContentProvider.mapUri(networkUri)
            } catch (e: Exception) {
                println("处理URI失败: $uriString, 错误: ${e.message}")
                null
            }
        }
    }

    /**
     * 示例：验证content URI的有效性
     */
    suspend fun validateContentUri(contentUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                contentResolver.openFileDescriptor(contentUri, "r")?.use {
                    true
                } ?: false
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * 完整使用示例
     */
    suspend fun completeExample() {
        println("=== AlbumArtContentProvider 使用示例 ===")
        
        // 1. 映射网络URI
        val networkUri = Uri.parse("https://example.com/albums/rock_album.jpg")
        val contentUri = AlbumArtContentProvider.mapUri(networkUri)
        println("1. URI映射:")
        println("   网络URI: $networkUri")
        println("   Content URI: $contentUri")
        
        // 2. 验证URI有效性
        val isValid = validateContentUri(contentUri)
        println("2. URI有效性: $isValid")
        
        // 3. 访问专辑封面数据
        val imageData = accessAlbumArt(networkUri)
        println("3. 图片数据大小: ${imageData?.size ?: 0} bytes")
        
        // 4. 批量处理
        val networkUris = listOf(
            "https://example.com/album1.jpg",
            "https://example.com/album2.jpg",
            "https://example.com/album3.jpg"
        )
        val contentUris = batchProcessAlbumArt(networkUris)
        println("4. 批量处理结果: ${contentUris.size} 个有效URI")
        
        println("=== 示例完成 ===")
    }
}

/**
 * ContentProvider的优势说明：
 * 
 * 1. **抽象化数据源**：
 *    - 隐藏真实的数据来源（本地文件、网络、数据库等）
 *    - 提供统一的访问接口
 * 
 * 2. **权限控制**：
 *    - 可以在ContentProvider中实现访问权限检查
 *    - 控制哪些应用可以访问数据
 * 
 * 3. **缓存机制**：
 *    - 使用Glide自动处理图片缓存
 *    - 避免重复下载相同的图片
 *    - 提供超时机制保证用户体验
 * 
 * 4. **系统集成**：
 *    - 与Android系统媒体框架完美集成
 *    - 支持Android Auto、通知系统等
 *    - 提供稳定的URI引用
 * 
 * 5. **性能优化**：
 *    - 异步下载和缓存
 *    - 避免UI线程阻塞
 *    - 智能的内存和磁盘缓存策略
 */