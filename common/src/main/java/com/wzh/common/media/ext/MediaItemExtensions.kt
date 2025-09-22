package com.wzh.common.media.ext

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import com.wzh.common.media.library.JsonSource

/**
 * Media3 MediaItem和MediaMetadata的扩展方法
 */

/**
 * MediaMetadata的便捷访问扩展属性
 */
inline val MediaMetadata.id: String?
    get() = extras?.getString("media_id")

inline val MediaMetadata.duration: Long
    get() = durationMs ?: 0L

inline val MediaMetadata.trackNumber: Long
    get() = (this.trackNumber ?: 0).toLong()

inline val MediaMetadata.trackCount: Long
    get() = (totalTrackCount ?: 0).toLong()

inline val MediaMetadata.albumArt: Bitmap?
    get() = null // Media3不直接支持Bitmap，通过artworkUri获取

inline val MediaMetadata.albumArtUri: Uri
    get() = artworkUri ?: Uri.EMPTY

inline val MediaMetadata.mediaUri: Uri
    get() = extras?.getString("media_uri")?.toUri() ?: Uri.EMPTY

inline val MediaMetadata.displayIconUri: Uri
    get() = artworkUri ?: Uri.EMPTY

/**
 * MediaMetadata.Builder的便捷设置扩展属性
 */
const val NO_GET = "Property does not have a 'get'"

inline var MediaMetadata.Builder.id: String
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadata.Builder")
    set(value) {
        setExtras(Bundle().apply { putString("media_id", value) })
    }

inline var MediaMetadata.Builder.duration: Long
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadata.Builder")
    set(value) {
        setDurationMs(value)
    }

inline var MediaMetadata.Builder.mediaUri: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadata.Builder")
    set(value) {
        val currentExtras = Bundle()
        value?.let { currentExtras.putString("media_uri", it) }
        setExtras(currentExtras)
    }

inline var MediaMetadata.Builder.albumArtUri: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadata.Builder")
    set(value) {
        value?.let { setArtworkUri(Uri.parse(it)) }
    }

inline var MediaMetadata.Builder.trackNumber: Long
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadata.Builder")
    set(value) {
        setTrackNumber(value.toInt())
    }

inline var MediaMetadata.Builder.trackCount: Long
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadata.Builder")
    set(value) {
        setTotalTrackCount(value.toInt())
    }

inline var MediaMetadata.Builder.displayTitle: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadata.Builder")
    set(value) {
        setDisplayTitle(value)
    }

inline var MediaMetadata.Builder.displaySubtitle: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadata.Builder")
    set(value) {
        setSubtitle(value)
    }

inline var MediaMetadata.Builder.displayDescription: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadata.Builder")
    set(value) {
        setDescription(value)
    }

inline var MediaMetadata.Builder.displayIconUri: String?
    @Deprecated(NO_GET, level = DeprecationLevel.ERROR)
    get() = throw IllegalAccessException("Cannot get from MediaMetadata.Builder")
    set(value) {
        value?.let { setArtworkUri(Uri.parse(it)) }
    }

/**
 * 自定义标志常量，用于标识MediaItem的类型
 */
const val METADATA_KEY_UAMP_FLAGS = "com.wzh.uampmusic.media.METADATA_KEY_UAMP_FLAGS"

/**
 * MediaItem的便捷访问扩展属性
 */
inline val MediaItem.flag: Int
    get() = mediaMetadata.extras?.getInt(METADATA_KEY_UAMP_FLAGS) ?: 0

/**
 * 创建可浏览的MediaItem
 */
fun createBrowsableMediaItem(
    mediaId: String,
    title: String,
    subtitle: String? = null,
    artworkUri: Uri? = null
): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setArtworkUri(artworkUri)
        .setIsBrowsable(true)
        .setIsPlayable(false)
        .build()

    return MediaItem.Builder()
        .setMediaId(mediaId)
        .setMediaMetadata(metadata)
        .build()
}

/**
 * 创建可播放的MediaItem
 */
fun createPlayableMediaItem(
    mediaId: String,
    uri: Uri,
    title: String,
    artist: String? = null,
    album: String? = null,
    artworkUri: Uri? = null,
    durationMs: Long? = null
): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setArtworkUri(artworkUri)
        .setDurationMs(durationMs)
        .setIsBrowsable(false)
        .setIsPlayable(true)
        .build()

    return MediaItem.Builder()
        .setMediaId(mediaId)
        .setUri(uri)
        .setMimeType(MimeTypes.AUDIO_MPEG)
        .setMediaMetadata(metadata)
        .build()
}