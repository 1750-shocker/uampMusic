// MainActivity.kt - Compose版本
package com.wzh.uampmusic

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import android.content.Intent
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wzh.uampmusic.ui.screens.MediaItemListScreen
import com.wzh.uampmusic.ui.screens.NowPlayingScreen
import com.wzh.uampmusic.ui.theme.MyUamp02Theme
import com.wzh.uampmusic.utils.InjectorUtils
import com.wzh.uampmusic.viewModels.MainActivityViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainActivityViewModel> {
        InjectorUtils.provideMainActivityViewModel(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        volumeControlStream = AudioManager.STREAM_MUSIC

        setContent {
            MyUamp02Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MusicApp(
    viewModel: MainActivityViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "media_list/{mediaId}",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(
                route = "media_list/{mediaId}",
                arguments = listOf(
                    navArgument("mediaId") { 
                        type = NavType.StringType
                        defaultValue = uiState.currentMediaId
                    }
                )
            ) { backStackEntry ->
                val mediaId = backStackEntry.arguments?.getString("mediaId") 
                    ?: uiState.currentMediaId
                
                MediaItemListScreen(
                    mediaId = mediaId,
                    onMediaItemClick = { mediaItem ->
                        viewModel.onMediaItemClicked(mediaItem)
                    },
                    onNavigateToNowPlaying = {
                        navController.navigate("now_playing")
                    }
                )
            }

            composable("now_playing") {
                NowPlayingScreen(
                    onBackClick = {
                        if (!navController.popBackStack()) {
                            // 如果返回栈为空，导航到默认媒体列表
                            navController.navigate("media_list/${uiState.currentMediaId}") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onPlayClick = { mediaId ->
                        viewModel.onPlayMediaId(mediaId)
                    },
                    onPrevClick = {
                        viewModel.onPrevMedia()
                    },
                    onNextClick = {
                        viewModel.onNextMedia()
                    }
                )
            }
        }
    }

    // 处理导航事件
    LaunchedEffect(uiState.navigationEvent) {
        uiState.navigationEvent?.let { event ->
            when (event) {
                is MainActivityViewModel.NavigationEvent.NavigateToMediaList -> {
                    // 导航到媒体列表，清除返回栈到根节点
                    navController.navigate("media_list/${uiState.currentMediaId}") {
                        popUpTo("media_list/{mediaId}") { 
                            inclusive = true 
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                is MainActivityViewModel.NavigationEvent.NavigateToNowPlaying -> {
                    // 导航到播放界面，保持返回栈
                    navController.navigate("now_playing") {
                        launchSingleTop = true
                    }
                }
            }
            viewModel.onNavigationEventHandled()
        }
    }
}