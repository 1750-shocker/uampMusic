package com.wzh.common.media.library

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.annotation.IntDef

/**
 * 音乐数据源接口，继承Iterable以支持遍历
 * 实现了这个接口的类就表示"可以被遍历的一系列元素"
 * 集合都实现了这个，就是有个才能用for-in
 */
interface MusicSource : Iterable<MediaItem> {
    /**
     * 加载音乐数据
     */
    suspend fun load()
    
    /**
     * 当数据源准备就绪时执行回调
     * @param performAction 要执行的回调函数，参数表示是否成功准备就绪
     * @return 如果数据源已经准备就绪则返回true，否则返回false
     */
    fun whenReady(performAction: (Boolean) -> Unit): Boolean
    
    /**
     * 搜索音乐（可选实现）
     * @param query 搜索查询字符串
     * @param extras 额外参数
     * @return 搜索结果列表
     */
    fun search(query: String, extras: Bundle): List<MediaItem> = emptyList()
}

/**
 * 定义注解，有限状态集合，替代枚举类
 */
@IntDef(
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
)
// 注释保留策略：SOURCE 表示只保留在源代码中，编译后会被移除不会编译到 class 文件中，运行时不可见
@Retention(AnnotationRetention.SOURCE)
annotation class State

const val STATE_CREATED = 1
const val STATE_INITIALIZING = 2
const val STATE_INITIALIZED = 3
const val STATE_ERROR = 4

/**
 * 音乐数据源的抽象基类
 * 
 * 搞了一个抽象类部分实现接口作为base，
 * 然后管理一个state，根据赋值，决定是否通知监听者，
 * 用一个list管理监听者，其实就是函数类型的回调方法
 */
abstract class AbstractMusicSource : MusicSource {
    
    companion object {
        private const val TAG = "MusicSource"
    }
    
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    /**
     * 数据源状态
     * 当状态变为INITIALIZED或ERROR时，会自动通知所有监听者
     */
    @State
    var state: Int = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    /**
     * 所以这个whenReady其实是一个接收函数的入口
     * 根据状态state返回是否Ready
     * Ready的直接执行函数，
     * 没有准备好就存到集合，好了之后（state赋值）自动通知执行
     */
    override fun whenReady(performAction: (Boolean) -> Unit): Boolean =
        when (state) {
            STATE_CREATED, STATE_INITIALIZING -> {
                onReadyListeners += performAction
                false
            }
            else -> {
                performAction(state != STATE_ERROR)
                true
            }
        }
}