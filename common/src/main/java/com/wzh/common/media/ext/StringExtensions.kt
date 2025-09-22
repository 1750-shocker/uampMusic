package com.wzh.common.media.ext

import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.Locale

/**
 * String类的扩展方法
 */

/**
 * 不区分大小写的字符串包含检查
 */
fun String?.containsCaseInsensitive(other: String?) =
    if (this != null && other != null) {
        lowercase(Locale.getDefault()).contains(other.lowercase(Locale.getDefault()))
    } else {
        this == other
    }

/**
 * URL编码扩展属性
 * 当字符串为null时返回空字符串
 */
inline val String?.urlEncoded: String
    get() = if (Charset.isSupported("UTF-8")) {
        URLEncoder.encode(this ?: "", "UTF-8")
    } else {
        // 如果不支持UTF-8，使用默认字符集
        @Suppress("deprecation")
        URLEncoder.encode(this ?: "")
    }

/**
 * 将可能为null的String转换为Uri，失败时返回Uri.EMPTY
 */
fun String?.toUri(): Uri = this?.let { Uri.parse(it) } ?: Uri.EMPTY