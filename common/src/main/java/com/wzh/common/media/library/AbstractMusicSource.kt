package com.wzh.common.media.library

import androidx.media3.common.MediaItem

/**
 * 音乐数据源的抽象基类
 */
abstract class AbstractMusicSource : MusicSource {
    
    companion object {
        const val STATE_CREATED = 1
        const val STATE_INITIALIZING = 2
        const val STATE_INITIALIZED = 3
        const val STATE_ERROR = 4
    }

    @Volatile
    var state: Int = STATE_CREATED
        protected set

    /**
     * 加载音乐数据
     */
    abstract suspend fun load()
}

/**
 * 音乐数据源接口，继承Iterable以支持遍历
 */
interface MusicSource : Iterable<MediaItem> {
    // 继承Iterable的iterator方法
}