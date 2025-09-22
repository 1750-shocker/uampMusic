package com.wzh.common.media.library

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.C
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * 音乐远程数据源，从JSON文件加载音乐目录
 */
internal class JsonSource(private val source: Uri) : AbstractMusicSource() {
    
    companion object {
        const val ORIGINAL_ARTWORK_URI_KEY = "com.wzh.uampmusic.JSON_ARTWORK_URI"
    }

    // 核心资产，媒体元数据
    private var catalog: List<MediaItem> = emptyList()

    init {
        state = STATE_INITIALIZING
    }

    // 实现Iterable接口，提供遍历能力
    override fun iterator(): Iterator<MediaItem> = catalog.iterator()

    override suspend fun load() {
        // 首先尝试加载远程数据源
        updateCatalog(source)?.let { updatedCatalog ->
            catalog = updatedCatalog
            state = STATE_INITIALIZED
            return
        }
        
        // 如果远程数据源失败，尝试加载本地备用数据
        try {
            val localCatalog = loadLocalCatalog()
            if (localCatalog.isNotEmpty()) {
                catalog = localCatalog
                state = STATE_INITIALIZED
                android.util.Log.w("JsonSource", "使用本地备用数据源")
                return
            }
        } catch (e: Exception) {
            android.util.Log.e("JsonSource", "加载本地备用数据失败", e)
        }
        
        // 所有数据源都失败
        catalog = emptyList()
        state = STATE_ERROR
    }

    /**
     * 核心行为，利用传进来的Uri，下载Json文件，解析为MediaItem列表
     */
    private suspend fun updateCatalog(catalogUri: Uri): List<MediaItem>? {
        return withContext(Dispatchers.IO) {
            // 用网络请求下载Json文件，返回JsonCatalog对象
            val musicCat = try {
                downloadJson(catalogUri)
            } catch (ioException: IOException) {
                return@withContext null
            }

            // Get the base URI to fix up relative references later.
            val baseUri = catalogUri.toString().removeSuffix(catalogUri.lastPathSegment ?: "")
            
            // 把JsonCatalog对象的List<JsonMusic>转换为List<MediaItem>
            val mediaItems = musicCat.music.map { song ->
                // The JSON may have paths that are relative to the source of the JSON
                // itself. We need to fix them up here to turn them into absolute paths.
                catalogUri.scheme?.let { scheme ->
                    if (!song.source.startsWith(scheme)) {
                        song.source = baseUri + song.source
                    }
                    if (!song.image.startsWith(scheme)) {
                        song.image = baseUri + song.image
                    }
                }

                // 将image字符串转换为Uri，再映射到ContentProvider URI
                val jsonImageUri = Uri.parse(song.image)
                // 调用AlbumArtContentProvider.mapUri(...)，把网络图片URI转成content://…形式
                val imageUri = AlbumArtContentProvider.mapUri(jsonImageUri)

                // 创建MediaMetadata
                val metadata = MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setGenre(song.genre)
                    .setArtworkUri(imageUri)
                    .setTrackNumber(song.trackNumber.toInt())
                    .setTotalTrackCount(song.totalTrackCount.toInt())
                    .setDurationMs(TimeUnit.SECONDS.toMillis(song.duration))
                    .setExtras(
                        androidx.core.os.bundleOf(
                            ORIGINAL_ARTWORK_URI_KEY to jsonImageUri.toString()
                        )
                    )
                    .build()

                // 创建MediaItem
                MediaItem.Builder()
                    .setMediaId(song.id)
                    .setUri(song.source)
                    .setMediaMetadata(metadata)
                    .build()
            }

            mediaItems
        }
    }

    @Throws(IOException::class)
    private fun downloadJson(catalogUri: Uri): JsonCatalog {
        val catalogConn = URL(catalogUri.toString())
        val reader = BufferedReader(InputStreamReader(catalogConn.openStream()))
        // 请求回来的JSON数据，转换为JsonCatalog对象
        return Gson().fromJson(reader, JsonCatalog::class.java)
    }
    
    /**
     * 加载本地备用数据源
     */
    private suspend fun loadLocalCatalog(): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            // 从assets目录加载本地catalog.json
            // 注意：这里需要通过其他方式获取Context，因为JsonSource没有直接的Context引用
            // 暂时返回空列表，实际使用时需要传入Context
            return@withContext emptyList<MediaItem>()
            
            /*
            val inputStream = context.assets.open("catalog.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val localCatalog = Gson().fromJson(reader, JsonCatalog::class.java)
            reader.close()
            
            // 转换为MediaItem列表
            localCatalog.music.map { song ->
                val metadata = MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setGenre(song.genre)
                    .setArtworkUri(android.net.Uri.parse(song.image))
                    .setTrackNumber(song.trackNumber.toInt())
                    .setTotalTrackCount(song.totalTrackCount.toInt())
                    .setDurationMs(java.util.concurrent.TimeUnit.SECONDS.toMillis(song.duration))
                    .build()

                MediaItem.Builder()
                    .setMediaId(song.id)
                    .setUri(song.source)
                    .setMediaMetadata(metadata)
                    .build()
            }
            */
        } catch (e: Exception) {
            android.util.Log.e("JsonSource", "加载本地catalog失败", e)
            emptyList()
        }
    }
}

/**
 * JSON目录数据类
 */
class JsonCatalog {
    var music: List<JsonMusic> = ArrayList()
}

/**
 * JSON音乐数据类
 */
@Suppress("unused")
class JsonMusic {
    var id: String = ""
    var title: String = ""
    var album: String = ""
    var artist: String = ""
    var genre: String = ""
    var source: String = ""
    var image: String = ""
    var trackNumber: Long = 0
    var totalTrackCount: Long = 0
    var duration: Long = C.TIME_UNSET
    var site: String = ""
}