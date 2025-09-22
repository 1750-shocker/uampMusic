package com.wzh.uampmusic.testing

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.wzh.uampmusic.data.MediaItemData
import com.wzh.uampmusic.utils.InjectorUtils
import kotlinx.coroutines.delay

/**
 * Task 11 验证运行器
 * 验证所有修复的组件是否正常工作
 */
class Task11ValidationRunner(private val context: Context) {
    
    private val tag = "Task11Validation"
    
    /**
     * 执行完整的功能验证
     */
    suspend fun runCompleteValidation(): ValidationResult {
        Log.d(tag, "开始执行 Task 11 完整验证...")
        
        val results = mutableListOf<ValidationStep>()
        
        // 1. 验证包名一致性
        results.add(validatePackageConsistency())
        
        // 2. 验证数据模型
        results.add(validateDataModels())
        
        // 3. 验证依赖注入
        results.add(validateDependencyInjection())
        
        // 4. 验证资源文件
        results.add(validateResources())
        
        // 5. 验证ViewModel创建
        results.add(validateViewModelCreation())
        
        val successCount = results.count { it.success }
        val totalCount = results.size
        
        Log.d(tag, "验证完成: $successCount/$totalCount 项成功")
        
        return ValidationResult(
            success = successCount == totalCount,
            totalSteps = totalCount,
            successfulSteps = successCount,
            steps = results,
            summary = "编译和基础功能验证完成。$successCount/$totalCount 项验证成功。"
        )
    }
    
    /**
     * 验证包名一致性
     */
    private fun validatePackageConsistency(): ValidationStep {
        return try {
            // 验证主要类的包名
            val mainActivityClass = Class.forName("com.wzh.uampmusic.MainActivity")
            val mediaItemDataClass = Class.forName("com.wzh.uampmusic.data.MediaItemData")
            val injectorUtilsClass = Class.forName("com.wzh.uampmusic.utils.InjectorUtils")
            
            Log.d(tag, "包名一致性验证成功")
            ValidationStep(
                name = "包名一致性验证",
                success = true,
                message = "所有主要类使用正确的包名 com.wzh.uampmusic"
            )
        } catch (e: Exception) {
            Log.e(tag, "包名一致性验证失败", e)
            ValidationStep(
                name = "包名一致性验证",
                success = false,
                message = "包名验证失败: ${e.message}"
            )
        }
    }
    
    /**
     * 验证数据模型
     */
    private fun validateDataModels(): ValidationStep {
        return try {
            // 创建测试数据
            val testData = MediaItemData(
                mediaId = "test_id",
                title = "Test Title",
                subtitle = "Test Subtitle",
                albumArtUri = android.net.Uri.parse("https://example.com/art.jpg"),
                browsable = true,
                playbackRes = 0
            )
            
            // 验证数据完整性
            require(testData.mediaId == "test_id")
            require(testData.title == "Test Title")
            require(testData.browsable)
            
            Log.d(tag, "数据模型验证成功")
            ValidationStep(
                name = "数据模型验证",
                success = true,
                message = "MediaItemData 类正常工作，所有字段可访问"
            )
        } catch (e: Exception) {
            Log.e(tag, "数据模型验证失败", e)
            ValidationStep(
                name = "数据模型验证",
                success = false,
                message = "数据模型验证失败: ${e.message}"
            )
        }
    }
    
    /**
     * 验证依赖注入
     */
    private fun validateDependencyInjection(): ValidationStep {
        return try {
            // 验证 InjectorUtils 可以访问
            val mainViewModelFactory = InjectorUtils.provideMainActivityViewModel(context)
            val mediaListViewModelFactory = InjectorUtils.provideMediaItemListViewModel(context, "test_id")
            val nowPlayingViewModelFactory = InjectorUtils.provideNowPlayingViewModel(context)
            
            // 验证工厂不为空
            require(mainViewModelFactory != null)
            require(mediaListViewModelFactory != null)
            require(nowPlayingViewModelFactory != null)
            
            Log.d(tag, "依赖注入验证成功")
            ValidationStep(
                name = "依赖注入验证",
                success = true,
                message = "所有 ViewModel 工厂可以正常创建"
            )
        } catch (e: Exception) {
            Log.e(tag, "依赖注入验证失败", e)
            ValidationStep(
                name = "依赖注入验证",
                success = false,
                message = "依赖注入验证失败: ${e.message}"
            )
        }
    }
    
    /**
     * 验证资源文件
     */
    private fun validateResources(): ValidationStep {
        return try {
            val resources = context.resources
            val packageName = context.packageName
            
            // 验证关键drawable资源
            val drawableResources = listOf(
                "ic_play_arrow_black_24dp",
                "ic_pause_black_24dp",
                "ic_skip_previous_black_24dp",
                "ic_skip_next_black_24dp",
                "ic_album_black_24dp",
                "default_art"
            )
            
            val missingResources = mutableListOf<String>()
            
            for (resourceName in drawableResources) {
                val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
                if (resourceId == 0) {
                    missingResources.add(resourceName)
                }
            }
            
            if (missingResources.isEmpty()) {
                Log.d(tag, "资源文件验证成功")
                ValidationStep(
                    name = "资源文件验证",
                    success = true,
                    message = "所有必需的 drawable 资源都存在"
                )
            } else {
                Log.w(tag, "部分资源文件缺失: $missingResources")
                ValidationStep(
                    name = "资源文件验证",
                    success = false,
                    message = "缺失资源: ${missingResources.joinToString(", ")}"
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "资源文件验证失败", e)
            ValidationStep(
                name = "资源文件验证",
                success = false,
                message = "资源验证失败: ${e.message}"
            )
        }
    }
    
    /**
     * 验证ViewModel创建
     */
    private fun validateViewModelCreation(): ValidationStep {
        return try {
            // 这里只验证工厂创建，不实际创建ViewModel（需要Activity上下文）
            val factories = listOf(
                InjectorUtils.provideMainActivityViewModel(context),
                InjectorUtils.provideMediaItemListViewModel(context, "test"),
                InjectorUtils.provideNowPlayingViewModel(context)
            )
            
            // 验证所有工厂都不为空
            require(factories.all { it != null })
            
            Log.d(tag, "ViewModel创建验证成功")
            ValidationStep(
                name = "ViewModel创建验证",
                success = true,
                message = "所有 ViewModel 工厂创建成功"
            )
        } catch (e: Exception) {
            Log.e(tag, "ViewModel创建验证失败", e)
            ValidationStep(
                name = "ViewModel创建验证",
                success = false,
                message = "ViewModel创建验证失败: ${e.message}"
            )
        }
    }
}

/**
 * 验证结果数据类
 */
data class ValidationResult(
    val success: Boolean,
    val totalSteps: Int,
    val successfulSteps: Int,
    val steps: List<ValidationStep>,
    val summary: String
)

/**
 * 验证步骤数据类
 */
data class ValidationStep(
    val name: String,
    val success: Boolean,
    val message: String
)

/**
 * Compose 验证组件
 */
@Composable
fun Task11ValidationDemo() {
    val context = LocalContext.current
    val validator = remember { Task11ValidationRunner(context) }
    
    LaunchedEffect(Unit) {
        delay(1000) // 等待初始化
        val result = validator.runCompleteValidation()
        Log.i("Task11Demo", "验证结果: ${result.summary}")
        
        // 打印详细结果
        result.steps.forEach { step ->
            val status = if (step.success) "✅" else "❌"
            Log.i("Task11Demo", "$status ${step.name}: ${step.message}")
        }
    }
}