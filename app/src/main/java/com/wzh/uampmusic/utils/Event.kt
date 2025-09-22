package com.wzh.uampmusic.utils


/**
 * 用于处理一次性事件的包装类
 * 防止配置更改时重复处理事件
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * 返回内容并防止再次使用
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 返回内容，即使已经被处理过
     */
    fun peekContent(): T = content
}