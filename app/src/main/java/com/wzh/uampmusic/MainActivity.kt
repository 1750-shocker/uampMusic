package com.wzh.uampmusic

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wzh.uampmusic.ui.theme.UampMusicTheme
import com.wzh.common.media.Media3IntegrationTestExample

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 测试Media3集成
        try {
            Log.d("MainActivity", "开始测试Media3集成...")
            Media3IntegrationTestExample.validateOnAppStart(this)
            Log.d("MainActivity", "Media3集成测试完成")
        } catch (e: Exception) {
            Log.e("MainActivity", "Media3集成测试失败", e)
        }
        
        setContent {
            UampMusicTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Media3 Music App",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UampMusicTheme {
        Greeting("Android")
    }
}