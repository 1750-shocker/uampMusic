package com.wzh.common.media.ext

import android.content.ContentResolver
import android.net.Uri
import java.io.File

/**
 * File类的扩展方法
 */

/**
 * 返回AlbumArtContentProvider的Content Uri
 */
fun File.asAlbumArtContentUri(): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_CONTENT)
        .authority(AUTHORITY)
        .appendPath(this.path)
        .build()
}

private const val AUTHORITY = "com.wzh.uampmusic.albumart"