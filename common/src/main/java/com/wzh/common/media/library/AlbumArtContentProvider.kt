package com.wzh.common.media.library

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.FileNotFoundException
import java.net.URL

/**
 * 专辑封面内容提供者，用于将网络图片URI转换为content://形式
 * 这样可以让ExoPlayer和通知系统正确加载网络图片
 */
class AlbumArtContentProvider : ContentProvider() {

    companion object {
        private const val AUTHORITY = "com.wzh.uampmusic.albumart"
        private const val ALBUM_ART = 1
        
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "albumart/*", ALBUM_ART)
        }

        /**
         * 将网络图片URI映射为content://形式的URI
         */
        fun mapUri(imageUri: Uri): Uri {
            return Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .appendPath("albumart")
                .appendPath(imageUri.toString())
                .build()
        }
    }

    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        return when (uriMatcher.match(uri)) {
            ALBUM_ART -> {
                val imageUrl = uri.lastPathSegment
                if (imageUrl != null) {
                    try {
                        val url = URL(imageUrl)
                        val connection = url.openConnection()
                        val inputStream = connection.getInputStream()
                        
                        // 创建临时文件来存储图片数据
                        val tempFile = java.io.File.createTempFile("album_art", ".tmp")
                        tempFile.outputStream().use { output ->
                            inputStream.copyTo(output)
                        }
                        
                        ParcelFileDescriptor.open(
                            tempFile,
                            ParcelFileDescriptor.MODE_READ_ONLY
                        )
                    } catch (e: Exception) {
                        throw FileNotFoundException("Could not open $uri: ${e.message}")
                    }
                } else {
                    throw FileNotFoundException("Invalid URI: $uri")
                }
            }
            else -> throw FileNotFoundException("Unsupported URI: $uri")
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            ALBUM_ART -> "image/*"
            else -> null
        }
    }
}