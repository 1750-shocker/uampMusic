package com.wzh.uampmusic.testing

import android.content.Context
import android.util.Log
import com.wzh.common.common.MusicServiceConnection
import com.wzh.uampmusic.utils.InjectorUtils
import kotlinx.coroutines.*

/**
 * Media3测试运行器
 * 
 * 提供简单的接口来运行Media3集成测试
 * 可以在MainActivity或其他地方调用来验证功能
 */
object Media3TestRunner {
    
    private const val TAG = "Media3TestRunner"
    
    /**
     * 运行完整的Media3集成测试
     */
    suspend fun runIntegrationTests(context: Context): TestSummary {
        Log.i(TAG, "开始运行Media3集成测试...")
        
        return try {
            // 获取MusicServiceConnection实例
            val musicServiceConnection = MusicServiceConnection.getInstance(context)
            
            // 创建验证器
            val validator = Media3IntegrationValidator(context, musicServiceConnection)
            
            // 运行验证
            val results = validator.validateMedia3Integration()
            
            // 清理资源
            validator.cleanup()
            
            // 生成测试摘要
            TestSummary.fromResults(results)
            
        } catch (e: Exception) {
            Log.e(TAG, "测试运行失败", e)
            TestSummary(
                totalTests = 0,
                passedTests = 0,
                failedTests = 1,
                successRate = 0.0,
                overallSuccess = false,
                errorMessage = "测试运行失败: ${e.message}",
                results = emptyList()
            )
        }
    }
    
    /**
     * 运行快速连接测试
     */
    suspend fun runQuickConnectionTest(context: Context): Boolean {
        return try {
            val musicServiceConnection = MusicServiceConnection.getInstance(context)
            
            // 等待连接或超时
            withTimeoutOrNull(3000) {
                while (musicServiceConnection.isConnected.value != true) {
                    delay(100)
                }
                true
            } ?: false
            
        } catch (e: Exception) {
            Log.e(TAG, "快速连接测试失败", e)
            false
        }
    }
    
    /**
     * 测试摘要数据类
     */
    data class TestSummary(
        val totalTests: Int,
        val passedTests: Int,
        val failedTests: Int,
        val successRate: Double,
        val overallSuccess: Boolean,
        val errorMessage: String? = null,
        val results: List<Media3IntegrationValidator.ValidationResult>
    ) {
        companion object {
            fun fromResults(results: List<Media3IntegrationValidator.ValidationResult>): TestSummary {
                val totalTests = results.size
                val passedTests = results.count { it.success }
                val failedTests = totalTests - passedTests
                val successRate = if (totalTests > 0) (passedTests.toDouble() / totalTests) * 100 else 0.0
                val overallSuccess = failedTests == 0 && totalTests > 0
                
                return TestSummary(
                    totalTests = totalTests,
                    passedTests = passedTests,
                    failedTests = failedTests,
                    successRate = successRate,
                    overallSuccess = overallSuccess,
                    results = results
                )
            }
        }
        
        /**
         * 获取格式化的摘要字符串
         */
        fun getFormattedSummary(): String {
            return buildString {
                appendLine("=== Media3集成测试摘要 ===")
                appendLine("总测试数: $totalTests")
                appendLine("通过: $passedTests")
                appendLine("失败: $failedTests")
                appendLine("成功率: ${"%.1f".format(successRate)}%")
                appendLine("整体结果: ${if (overallSuccess) "成功" else "失败"}")
                
                errorMessage?.let {
                    appendLine("错误信息: $it")
                }
                
                if (failedTests > 0) {
                    appendLine("\n失败的测试:")
                    results.filter { !it.success }.forEach { result ->
                        appendLine("- ${result.testName}: ${result.message}")
                    }
                }
                
                appendLine("========================")
            }
        }
        
        /**
         * 获取详细结果
         */
        fun getDetailedResults(): String {
            return buildString {
                appendLine("=== 详细测试结果 ===")
                results.forEach { result ->
                    val status = if (result.success) "✓" else "✗"
                    appendLine("$status ${result.testName}: ${result.message}")
                    result.details?.let { details ->
                        appendLine("  详细信息: $details")
                    }
                }
                appendLine("==================")
            }
        }
    }
}

/**
 * 扩展函数：在MainActivity中运行测试
 */
fun Context.runMedia3IntegrationTest(
    onComplete: (Media3TestRunner.TestSummary) -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val summary = Media3TestRunner.runIntegrationTests(this@runMedia3IntegrationTest)
            onComplete(summary)
        } catch (e: Exception) {
            Log.e("Media3TestRunner", "测试执行失败", e)
            onComplete(
                Media3TestRunner.TestSummary(
                    totalTests = 0,
                    passedTests = 0,
                    failedTests = 1,
                    successRate = 0.0,
                    overallSuccess = false,
                    errorMessage = e.message,
                    results = emptyList()
                )
            )
        }
    }
}

/**
 * 扩展函数：快速连接测试
 */
fun Context.testMedia3Connection(
    onResult: (Boolean) -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val isConnected = Media3TestRunner.runQuickConnectionTest(this@testMedia3Connection)
            onResult(isConnected)
        } catch (e: Exception) {
            Log.e("Media3TestRunner", "连接测试失败", e)
            onResult(false)
        }
    }
}