package com.wzh.common.media.library

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.wzh.common.R


/**
 * 媒体浏览树，用于组织音乐内容的层次结构
 * 支持推荐、专辑、最近播放等分类浏览
 */
class BrowseTree(
    val context: Context,
    musicSource: MusicSource,
    val recentMediaId: String? = null
) {
    // 核心资产：媒体ID到子项列表的映射
    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaItem>>()

    /**
     * 是否允许未知调用者使用搜索功能
     */
    val searchableByUnknownCaller = true

    /**
     * 构建媒体浏览树结构
     * 根节点包含推荐和专辑两个分类
     * 每个专辑包含该专辑的所有歌曲
     */
    init {
        // 构建根节点列表
        val rootList = mediaIdToChildren[UAMP_BROWSABLE_ROOT] ?: mutableListOf()

        // 创建推荐分类节点
        val recommendedMetadata = createCategoryMediaItem(
            id = UAMP_RECOMMENDED_ROOT,
            title = context.getString(R.string.recommended_title),
            iconResource = R.drawable.ic_recommended
        )

        // 创建专辑分类节点
        val albumsMetadata = createCategoryMediaItem(
            id = UAMP_ALBUMS_ROOT,
            title = context.getString(R.string.albums_title),
            iconResource = R.drawable.ic_album
        )

        rootList += recommendedMetadata
        rootList += albumsMetadata
        mediaIdToChildren[UAMP_BROWSABLE_ROOT] = rootList

        // 遍历音乐源，构建树结构
        musicSource.forEach { mediaItem ->
            // 每首歌都属于一个专辑，获取专辑ID作为父节点ID
            val albumMediaId = "album_${mediaItem.mediaMetadata.albumTitle?.hashCode() ?: 0}"

            // 如果是第一次遇到这个专辑，创建专辑根节点
            val albumChildren = mediaIdToChildren[albumMediaId] ?: buildAlbumRoot(mediaItem)
            albumChildren += mediaItem

            // 将每个专辑的第一首歌加入推荐列表
            val trackNumber = mediaItem.mediaMetadata.trackNumber ?: 0
            if (trackNumber == 1) {
                val recommendedChildren = mediaIdToChildren[UAMP_RECOMMENDED_ROOT] ?: mutableListOf()
                recommendedChildren += mediaItem
                mediaIdToChildren[UAMP_RECOMMENDED_ROOT] = recommendedChildren
            }

            // 如果这是最近播放的歌曲，加入最近播放列表
            if (mediaItem.mediaId == recentMediaId) {
                mediaIdToChildren[UAMP_RECENT_ROOT] = mutableListOf(mediaItem)
            }
        }
    }

    /**
     * 提供访问子项列表的操作符重载
     * 使用方式: browseTree[UAMP_BROWSABLE_ROOT]
     */
    operator fun get(mediaId: String) = mediaIdToChildren[mediaId]

    /**
     * 创建分类媒体项（如推荐、专辑等分类节点）
     */
    private fun createCategoryMediaItem(
        id: String,
        title: String,
        iconResource: Int
    ): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtworkUri(android.net.Uri.parse(RESOURCE_ROOT_URI + context.resources.getResourceEntryName(iconResource)))
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .build()

        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(metadata)
            .build()
    }

    /**
     * 构建专辑根节点
     * 给定专辑中的一首歌，创建该专辑的根节点
     */
    private fun buildAlbumRoot(mediaItem: MediaItem): MutableList<MediaItem> {
        val albumMetadata = MediaMetadata.Builder()
            .setTitle(mediaItem.mediaMetadata.albumTitle)
            .setArtist(mediaItem.mediaMetadata.albumArtist ?: mediaItem.mediaMetadata.artist)
            .setArtworkUri(mediaItem.mediaMetadata.artworkUri)
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .build()

        val albumMediaItem = MediaItem.Builder()
            .setMediaId("album_${mediaItem.mediaMetadata.albumTitle?.hashCode() ?: 0}")
            .setMediaMetadata(albumMetadata)
            .build()

        // 将此专辑添加到"专辑"分类中
        val rootList = mediaIdToChildren[UAMP_ALBUMS_ROOT] ?: mutableListOf()
        rootList += albumMediaItem
        mediaIdToChildren[UAMP_ALBUMS_ROOT] = rootList

        // 为专辑创建空的子项列表并返回
        return mutableListOf<MediaItem>().also {
            mediaIdToChildren[albumMediaItem.mediaId] = it
        }
    }
}

// 常量定义
const val UAMP_BROWSABLE_ROOT = "/"
const val UAMP_EMPTY_ROOT = "@empty@"
const val UAMP_RECOMMENDED_ROOT = "__RECOMMENDED__"
const val UAMP_ALBUMS_ROOT = "__ALBUMS__"
const val UAMP_RECENT_ROOT = "__RECENT__"
const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"
const val RESOURCE_ROOT_URI = "android.resource://com.wzh.uampmusic/drawable/"