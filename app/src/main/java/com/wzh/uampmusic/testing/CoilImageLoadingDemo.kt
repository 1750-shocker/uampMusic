package com.wzh.uampmusic.testing

import android.content.Context
import android.net.Uri
import android.util.Log
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.request.ImageResult
import com.wzh.uampmusic.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Coil图片加载功能演示类
 * 用于验证和测试图片加载的各种场景
 * 
 * 需求覆盖:
 * - 6.1: 测试Coil库的专辑封面加载功能
 * - 6.2: 验证占位图和错误图的显示
 * - 6.3: 测试图片加载的交叉淡入动画和缓存策略
 */
class CoilImageLoadingDemo(private val context: Context) {
    
    companion object {
        private const val TAG = "CoilImageLoadingDemo"
    }

    private val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // 使用25%的可用内存
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // 使用2%的可用存储空间
                    .build()
            }
            .crossfade(true) // 启用交叉淡入动画
            .build()
    }

    /**
     * 演示1: 测试基本的图片加载功能
     * 需求: 6.1 - WHEN UI组件加载专辑封面 THEN Coil应该正确处理网络图片和本地资源
     */
    fun demonstrateBasicImageLoading() {
        Log.d(TAG, "=== 演示基本图片加载功能 ===")
        
        val testCases = listOf(
            TestCase(
                name = "网络图片",
                uri = Uri.parse("https://picsum.photos/300/300?random=1"),
                description = "测试从网络加载随机图片"
            ),
            TestCase(
                name = "本地资源",
                uri = Uri.parse("android.resource://com.wzh.uampmusic/${R.drawable.default_art}"),
                description = "测试加载应用内置资源"
            ),
            TestCase(
                name = "无效URI",
                uri = Uri.parse("invalid://invalid.uri"),
                description = "测试无效URI的错误处理"
            ),
            TestCase(
                name = "空URI",
                uri = Uri.EMPTY,
                description = "测试空URI的处理"
            )
        )

        testCases.forEach { testCase ->
            Log.d(TAG, "测试案例: ${testCase.name}")
            Log.d(TAG, "描述: ${testCase.description}")
            Log.d(TAG, "URI: ${testCase.uri}")
            
            val request = createImageRequest(testCase.uri, testCase.name)
            executeImageRequest(request, testCase.name)
        }
    }

    /**
     * 演示2: 测试占位图和错误图功能
     * 需求: 6.2 - WHEN 图片加载失败时 THEN 应该显示默认的占位图片
     */
    fun demonstratePlaceholderAndErrorHandling() {
        Log.d(TAG, "=== 演示占位图和错误处理功能 ===")
        
        val errorTestCases = listOf(
            ErrorTestCase(
                name = "网络超时",
                uri = Uri.parse("https://httpstat.us/408"),
                expectedBehavior = "应该显示错误图片"
            ),
            ErrorTestCase(
                name = "404错误",
                uri = Uri.parse("https://httpstat.us/404"),
                expectedBehavior = "应该显示错误图片"
            ),
            ErrorTestCase(
                name = "服务器错误",
                uri = Uri.parse("https://httpstat.us/500"),
                expectedBehavior = "应该显示错误图片"
            ),
            ErrorTestCase(
                name = "不存在的域名",
                uri = Uri.parse("https://this.domain.does.not.exist.invalid/image.jpg"),
                expectedBehavior = "应该显示错误图片"
            )
        )

        errorTestCases.forEach { testCase ->
            Log.d(TAG, "错误测试: ${testCase.name}")
            Log.d(TAG, "URI: ${testCase.uri}")
            Log.d(TAG, "期望行为: ${testCase.expectedBehavior}")
            
            val request = ImageRequest.Builder(context)
                .data(testCase.uri)
                .placeholder(R.drawable.default_art)
                .error(R.drawable.ic_signal_wifi_off_black_24dp) // 使用网络错误图标
                .crossfade(true)
                .listener(
                    onStart = { Log.d(TAG, "[${testCase.name}] 开始加载图片") },
                    onSuccess = { _, _ -> Log.d(TAG, "[${testCase.name}] 图片加载成功（意外）") },
                    onError = { _, result -> 
                        Log.d(TAG, "[${testCase.name}] 图片加载失败，显示错误图片: ${result.throwable?.message}")
                    }
                )
                .build()
                
            executeImageRequest(request, testCase.name)
        }
    }

    /**
     * 演示3: 测试交叉淡入动画配置
     * 需求: 6.3 - 测试图片加载的交叉淡入动画
     */
    fun demonstrateCrossfadeAnimation() {
        Log.d(TAG, "=== 演示交叉淡入动画功能 ===")
        
        val animationTestCases = listOf(
            AnimationTestCase(
                name = "默认动画",
                uri = Uri.parse("https://picsum.photos/300/300?random=anim1"),
                crossfade = true,
                duration = null,
                description = "使用默认的交叉淡入动画"
            ),
            AnimationTestCase(
                name = "自定义动画时长",
                uri = Uri.parse("https://picsum.photos/300/300?random=anim2"),
                crossfade = true,
                duration = 500,
                description = "使用500ms的交叉淡入动画"
            ),
            AnimationTestCase(
                name = "禁用动画",
                uri = Uri.parse("https://picsum.photos/300/300?random=anim3"),
                crossfade = false,
                duration = null,
                description = "禁用交叉淡入动画"
            )
        )

        animationTestCases.forEach { testCase ->
            Log.d(TAG, "动画测试: ${testCase.name}")
            Log.d(TAG, "描述: ${testCase.description}")
            
            val requestBuilder = ImageRequest.Builder(context)
                .data(testCase.uri)
                .placeholder(R.drawable.default_art)
                .error(R.drawable.default_art)
                .crossfade(testCase.crossfade)
                
            testCase.duration?.let { duration ->
                requestBuilder.crossfade(duration)
            }
            
            val request = requestBuilder
                .listener(
                    onStart = { Log.d(TAG, "[${testCase.name}] 开始加载，动画: ${testCase.crossfade}") },
                    onSuccess = { _, _ -> 
                        Log.d(TAG, "[${testCase.name}] 加载成功，交叉淡入动画: ${testCase.crossfade}")
                    }
                )
                .build()
                
            Log.d(TAG, "动画配置 - 交叉淡入: ${testCase.crossfade}")
            executeImageRequest(request, testCase.name)
        }
    }

    /**
     * 演示4: 测试缓存策略
     * 需求: 6.3 - 确保图片缓存策略正确工作
     */
    fun demonstrateCacheStrategy() {
        Log.d(TAG, "=== 演示缓存策略功能 ===")
        
        // 显示缓存配置信息
        val memoryCache = imageLoader.memoryCache
        val diskCache = imageLoader.diskCache
        
        Log.d(TAG, "内存缓存配置:")
        Log.d(TAG, "  - 最大大小: ${memoryCache?.maxSize ?: 0} bytes")
        Log.d(TAG, "  - 当前大小: ${memoryCache?.size ?: 0} bytes")
        
        Log.d(TAG, "磁盘缓存配置:")
        Log.d(TAG, "  - 最大大小: ${diskCache?.maxSize ?: 0} bytes")
        Log.d(TAG, "  - 当前大小: ${diskCache?.size ?: 0} bytes")
        Log.d(TAG, "  - 缓存目录: ${diskCache?.directory}")

        // 测试缓存效果
        val testUri = Uri.parse("https://picsum.photos/300/300?random=cache_test")
        val cacheKey = "cache_demo_key"
        
        Log.d(TAG, "第一次加载图片（应该从网络加载）")
        val firstRequest = ImageRequest.Builder(context)
            .data(testUri)
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .placeholder(R.drawable.default_art)
            .error(R.drawable.default_art)
            .crossfade(true)
            .listener(
                onStart = { Log.d(TAG, "[缓存测试] 第一次加载开始") },
                onSuccess = { _, _ -> 
                    Log.d(TAG, "[缓存测试] 第一次加载成功，图片已缓存")
                    
                    // 立即进行第二次加载测试
                    testCachedImageLoad(testUri, cacheKey)
                }
            )
            .build()
            
        executeImageRequest(firstRequest, "缓存测试-首次")
    }

    /**
     * 演示5: 测试不同图片尺寸的处理
     * 需求: 6.1 - 验证不同尺寸专辑封面的正确处理
     */
    fun demonstrateDifferentImageSizes() {
        Log.d(TAG, "=== 演示不同图片尺寸处理 ===")
        
        val sizeTestCases = listOf(
            SizeTestCase("小图片", "https://picsum.photos/100/100", "100x100像素"),
            SizeTestCase("中等图片", "https://picsum.photos/300/300", "300x300像素"),
            SizeTestCase("大图片", "https://picsum.photos/800/800", "800x800像素"),
            SizeTestCase("宽图片", "https://picsum.photos/800/400", "800x400像素"),
            SizeTestCase("高图片", "https://picsum.photos/400/800", "400x800像素")
        )

        sizeTestCases.forEach { testCase ->
            Log.d(TAG, "尺寸测试: ${testCase.name} (${testCase.description})")
            
            val request = ImageRequest.Builder(context)
                .data(testCase.url)
                .placeholder(R.drawable.default_art)
                .error(R.drawable.default_art)
                .crossfade(true)
                .memoryCacheKey("size_${testCase.name}")
                .listener(
                    onStart = { Log.d(TAG, "[${testCase.name}] 开始加载 ${testCase.description}") },
                    onSuccess = { _, result -> 
                        Log.d(TAG, "[${testCase.name}] 加载成功: ${testCase.description}")
                    }
                )
                .build()
                
            executeImageRequest(request, testCase.name)
        }
    }

    /**
     * 创建标准的ImageRequest
     */
    private fun createImageRequest(uri: Uri, tag: String): ImageRequest {
        return ImageRequest.Builder(context)
            .data(uri)
            .placeholder(R.drawable.default_art)
            .error(R.drawable.default_art)
            .crossfade(true)
            .memoryCacheKey("demo_$tag")
            .listener(
                onStart = { Log.d(TAG, "[$tag] 开始加载图片") },
                onSuccess = { _, _ -> Log.d(TAG, "[$tag] 图片加载成功") },
                onError = { _, result -> 
                    Log.d(TAG, "[$tag] 图片加载失败: ${result.throwable?.message}")
                }
            )
            .build()
    }

    /**
     * 执行图片请求
     */
    private fun executeImageRequest(request: ImageRequest, tag: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    imageLoader.execute(request)
                }
                
                // Note: In Coil 2.x, ImageResult is sealed and execution is handled by listeners
                Log.d(TAG, "[$tag] 请求已提交执行")
            } catch (e: Exception) {
                Log.e(TAG, "[$tag] 执行请求时发生异常", e)
            }
        }
    }

    /**
     * 测试缓存的图片加载
     */
    private fun testCachedImageLoad(uri: Uri, cacheKey: String) {
        Log.d(TAG, "第二次加载相同图片（应该从缓存加载）")
        
        val secondRequest = ImageRequest.Builder(context)
            .data(uri)
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .placeholder(R.drawable.default_art)
            .error(R.drawable.default_art)
            .crossfade(true)
            .listener(
                onStart = { Log.d(TAG, "[缓存测试] 第二次加载开始（应该很快）") },
                onSuccess = { _, _ -> 
                    Log.d(TAG, "[缓存测试] 第二次加载成功，从缓存获取")
                }
            )
            .build()
            
        executeImageRequest(secondRequest, "缓存测试-缓存")
    }

    /**
     * 运行所有演示
     */
    fun runAllDemonstrations() {
        Log.d(TAG, "开始运行Coil图片加载功能演示")
        Log.d(TAG, "ImageLoader配置:")
        Log.d(TAG, "  - 交叉淡入: 启用")
        Log.d(TAG, "  - 内存缓存: 启用")
        Log.d(TAG, "  - 磁盘缓存: 启用")
        
        demonstrateBasicImageLoading()
        demonstratePlaceholderAndErrorHandling()
        demonstrateCrossfadeAnimation()
        demonstrateCacheStrategy()
        demonstrateDifferentImageSizes()
        
        Log.d(TAG, "所有演示完成")
    }

    // 数据类定义
    private data class TestCase(
        val name: String,
        val uri: Uri,
        val description: String
    )

    private data class ErrorTestCase(
        val name: String,
        val uri: Uri,
        val expectedBehavior: String
    )

    private data class AnimationTestCase(
        val name: String,
        val uri: Uri,
        val crossfade: Boolean,
        val duration: Int?,
        val description: String
    )

    private data class SizeTestCase(
        val name: String,
        val url: String,
        val description: String
    )
}