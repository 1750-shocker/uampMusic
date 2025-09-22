package com.wzh.common.media

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Media3集成测试使用示例
 * 演示如何使用Media3IntegrationTest类验证集成
 */
object Media3IntegrationTestExample {
    
    /**
     * 在Activity中运行测试的示例
     */
    fun runTestInActivity(context: Context) {
        println("📱 在Activity中运行Media3集成测试")
        
        val testScope = CoroutineScope(Dispatchers.Main)
        testScope.launch {
            val integrationTest = Media3IntegrationTest(context)
            val result = integrationTest.runAllTests()
            
            if (result.success) {
                println("🎉 所有测试通过！Media3集成成功")
            } else {
                println("⚠️ 部分测试失败，请检查配置")
            }
            
            integrationTest.cleanup()
        }
    }
    
    /**
     * 快速验证示例
     */
    fun quickValidation(context: Context) {
        println("⚡ 快速验证Media3集成")
        
        val success = Media3IntegrationTest.quickTest(context)
        
        if (success) {
            println("✅ Media3集成验证通过")
        } else {
            println("❌ Media3集成验证失败")
        }
    }
    
    /**
     * 详细测试示例
     */
    fun detailedTest(context: Context) {
        println("🔍 详细Media3集成测试")
        
        val integrationTest = Media3IntegrationTest(context)
        
        // 逐个运行测试
        println("\n1️⃣ 测试ExoPlayer创建...")
        val exoPlayerTest = integrationTest.testExoPlayerCreation()
        
        println("\n2️⃣ 测试MediaItem创建...")
        val mediaItemTest = integrationTest.testMediaItemCreation()
        
        println("\n3️⃣ 测试基本播放功能...")
        val playbackTest = integrationTest.testBasicPlayback()
        
        println("\n4️⃣ 测试UI组件...")
        val uiTest = integrationTest.testMedia3UIComponents()
        
        println("\n5️⃣ 测试媒体会话功能...")
        val sessionTest = integrationTest.testMediaSessionFunctionality()
        
        // 汇总结果
        val allTests = listOf(exoPlayerTest, mediaItemTest, playbackTest, uiTest, sessionTest)
        val passedCount = allTests.count { it }
        
        println("\n📈 详细测试结果:")
        println("ExoPlayer创建: ${if (exoPlayerTest) "✅" else "❌"}")
        println("MediaItem创建: ${if (mediaItemTest) "✅" else "❌"}")
        println("基本播放功能: ${if (playbackTest) "✅" else "❌"}")
        println("UI组件: ${if (uiTest) "✅" else "❌"}")
        println("媒体会话功能: ${if (sessionTest) "✅" else "❌"}")
        println("总体通过率: $passedCount/${allTests.size}")
        
        integrationTest.cleanup()
    }
    
    /**
     * 在Application启动时验证
     */
    fun validateOnAppStart(context: Context) {
        println("🚀 应用启动时验证Media3集成")
        
        try {
            val success = Media3IntegrationTest.quickTest(context)
            
            if (success) {
                println("✅ Media3集成正常，应用可以正常使用音频功能")
            } else {
                println("⚠️ Media3集成异常，音频功能可能受影响")
            }
        } catch (e: Exception) {
            println("❌ Media3集成验证出错: ${e.message}")
        }
    }
    
    /**
     * 性能测试示例
     */
    fun performanceTest(context: Context) {
        println("⏱️ Media3性能测试")
        
        val startTime = System.currentTimeMillis()
        
        val integrationTest = Media3IntegrationTest(context)
        val result = integrationTest.runAllTests()
        integrationTest.cleanup()
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        println("📊 性能测试结果:")
        println("测试耗时: ${duration}ms")
        println("测试结果: ${if (result.success) "成功" else "失败"}")
        println("平均每个测试耗时: ${duration / result.totalTests}ms")
    }
}

/**
 * 在MainActivity中使用的示例代码
 * 
 * class MainActivity : ComponentActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         
 *         // 验证Media3集成
 *         Media3IntegrationTestExample.quickValidation(this)
 *         
 *         // 或者运行详细测试
 *         // Media3IntegrationTestExample.detailedTest(this)
 *         
 *         setContent {
 *             // 你的Compose UI
 *         }
 *     }
 * }
 */

/**
 * 在Application中使用的示例代码
 * 
 * class MyApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         
 *         // 应用启动时验证Media3集成
 *         Media3IntegrationTestExample.validateOnAppStart(this)
 *     }
 * }
 */