package com.wzh.common.media.library

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

// 专辑封面文件下载超时时间（秒）
const val DOWNLOAD_TIMEOUT_SECONDS = 30L

/**
 * 专辑封面内容提供者
 * 
 * 作为一个读取专辑封面文件的服务，通过自定义的URI获取专辑封面文件，
 * 并且具备从远程下载图像和缓存图像的能力，同时实现超时机制以保证用户体验。
 * 
 * 主要功能：
 * 1. 向Android系统其他部分（如Android Auto、Google Assistant或应用内部其他模块）隐藏媒体的真实来源
 * 2. 系统无需知道音乐是存储在设备本地、SD卡还是从服务器流式传输，只需通过稳定的唯一标识符（content://...）引用媒体内容
 * 3. 当需要变更音乐来源时，只需修改ContentProvider内部逻辑，内容URI可保持不变，其他应用或组件不会受到影响
 * 4. 通过强制要求经由内容URI访问，可在ContentProvider中检查权限并控制内容访问
 * 5. 为MediaItem对象提供稳定唯一的mediaId，这种内容URI格式恰好适合作为mediaId
 */
internal class AlbumArtContentProvider : ContentProvider() {

    companion object {
        private const val AUTHORITY = "com.wzh.uampmusic.albumart"
        private val uriMap = mutableMapOf<Uri, Uri>()

        /**
         * 将输入的URI映射到内部对应的URI
         * 将网络图片URI转换为content://形式的URI
         */
        fun mapUri(uri: Uri): Uri {
            // 字符串处理，去掉前面的斜杠并将斜杠替换为冒号
            val path = uri.encodedPath?.substring(1)?.replace('/', ':') ?: return Uri.EMPTY
            val contentUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT) // 协议：内容URI
                .authority(AUTHORITY) // 内容提供者
                .path(path)
                .build()
            uriMap[contentUri] = uri
            return contentUri
        }
    }

    // 表示ContentProvider成功创建
    override fun onCreate() = true

    /**
     * 打开一个文件并返回其文件描述符（ParcelFileDescriptor）
     * 接受一个URI和文件模式作为参数
     */
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val context = this.context ?: return null
        
        // 从uriMap中获取与传入URI对应的远程URI，如果没有找到映射关系，则抛出FileNotFoundException
        val remoteUri = uriMap[uri] ?: throw FileNotFoundException(uri.path ?: "Unknown path")
        
        // 通过context.cacheDir创建一个本地文件对象，用于缓存下载的专辑封面图像
        var file = File(context.cacheDir, uri.path ?: "unknown")
        
        // 如果文件不存在，则使用Glide下载远程URI指向的专辑封面图像，并将其作为文件保存
        if (!file.exists()) {
            try {
                // 使用Glide下载专辑封面
                val cacheFile = Glide.with(context)
                    .asFile()
                    .load(remoteUri)
                    .submit() // submit()返回一个Future，调用get()方法来等待下载结果
                    .get(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS) // 设置了超时为DOWNLOAD_TIMEOUT_SECONDS秒

                // 一旦下载完成，Glide会将文件存储到临时缓存中，通过renameTo()方法
                // 将缓存文件重命名为符合我们本地路径要求的文件
                cacheFile.renameTo(file)
                // 并将file变量指向这个缓存文件
                file = cacheFile
            } catch (e: Exception) {
                throw FileNotFoundException("Could not download album art from $remoteUri: ${e.message}")
            }
        }

        // 通过ParcelFileDescriptor.open()打开文件，并指定为只读模式
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    // 不涉及数据库的增删改查操作
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ) = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0

    /**
     * getType()方法返回URI对应的数据类型，返回null，表示我们没有定义MIME类型，
     * 因为ContentProvider主要是提供文件访问功能，而不是传统的数据类型。
     */
    override fun getType(uri: Uri): String? = null
}