package com.wzh.uampmusic.utils

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.wzh.common.common.MusicServiceConnection
import com.wzh.common.media.MusicService
import com.wzh.uampmusic.viewModels.MainActivityViewModel
import com.wzh.uampmusic.viewModels.MediaItemListViewModel
import com.wzh.uampmusic.viewModels.NowPlayingViewModel

/**
 * 依赖注入工具类，提供ViewModel工厂
 */
object InjectorUtils {

    private fun provideMusicServiceConnection(context: Context): MusicServiceConnection {
        return MusicServiceConnection.getInstance(
            context,
            ComponentName(context, MusicService::class.java)
        )
    }

    fun provideMainActivityViewModel(context: Context): ViewModelProvider.Factory {
        val applicationContext = context.applicationContext
        val musicServiceConnection = provideMusicServiceConnection(applicationContext)
        return MainActivityViewModelFactory(musicServiceConnection)
    }

    fun provideMediaItemListViewModel(
        context: Context,
        mediaId: String
    ): ViewModelProvider.Factory {
        val applicationContext = context.applicationContext
        val musicServiceConnection = provideMusicServiceConnection(applicationContext)
        return MediaItemListViewModelFactory(musicServiceConnection, mediaId)
    }

    fun provideNowPlayingViewModel(context: Context): ViewModelProvider.Factory {
        val applicationContext = context.applicationContext
        val musicServiceConnection = provideMusicServiceConnection(applicationContext)
        return NowPlayingViewModelFactory(musicServiceConnection)
    }
}

class MainActivityViewModelFactory(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(musicServiceConnection) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MediaItemListViewModelFactory(
    private val musicServiceConnection: MusicServiceConnection,
    private val mediaId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaItemListViewModel::class.java)) {
            return MediaItemListViewModel(musicServiceConnection, mediaId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NowPlayingViewModelFactory(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NowPlayingViewModel::class.java)) {
            return NowPlayingViewModel(musicServiceConnection) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}