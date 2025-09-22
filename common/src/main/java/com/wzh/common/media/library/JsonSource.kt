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
        updateCatalog(source)?.let { updatedCatalog ->
            catalog = updatedCatalog
            state = STATE_INITIALIZED
        } ?: run {
            catalog = emptyList()
            state = STATE_ERROR
        }
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