# Common Module - Media3音乐播放核心

这是一个通用模块，包含了项目中共享的代码和资源，特别是Media3音乐播放功能的核心实现。

## 🎵 功能特性

### Media3音乐播放
- **ExoPlayer集成**: 高性能音频播放引擎
- **MediaSession管理**: 完整的媒体会话控制
- **MediaLibraryService**: 后台音乐播放服务
- **音频焦点管理**: 智能音频焦点处理
- **播放列表管理**: 支持队列和随机播放

### 核心组件
- **MusicService**: 基于MediaLibraryService的音乐播放服务
- **MusicServiceConnection**: 服务连接和控制客户端
- **JsonSource**: JSON数据源音乐加载器
- **BrowseTree**: 分层媒体浏览结构
- **AlbumArtContentProvider**: 专辑封面缓存提供者

### 通用功能
- 通用工具类和扩展方法
- 共享的数据模型
- 网络请求封装
- 缓存管理
- 日志工具

## 📁 模块结构

```
common/
├── src/main/java/com/wzh/common/
│   ├── media/                    # Media3音乐播放核心
│   │   ├── MusicService.kt       # 音乐播放服务
│   │   ├── Media3IntegrationTest.kt  # 集成测试
│   │   ├── library/              # 媒体库组件
│   │   │   ├── JsonSource.kt     # JSON数据源
│   │   │   ├── BrowseTree.kt     # 浏览树结构
│   │   │   ├── AbstractMusicSource.kt  # 抽象音乐源
│   │   │   └── AlbumArtContentProvider.kt  # 专辑封面提供者
│   │   └── ext/                  # Media3扩展方法
│   │       ├── MediaItemExtensions.kt
│   │       ├── PlayerExtensions.kt
│   │       ├── StringExtensions.kt
│   │       └── FileExtensions.kt
│   ├── common/                   # 通用组件
│   │   ├── MusicServiceConnection.kt  # 服务连接管理
│   │   └── MusicServiceConnectionExample.kt
│   ├── utils/                    # 工具类
│   ├── network/                  # 网络相关
│   ├── cache/                    # 缓存管理
│   ├── model/                    # 数据模型
│   └── extension/                # 扩展方法
├── src/main/res/                 # 资源文件
│   └── drawable/                 # 图标资源
└── build.gradle.kts              # 构建配置
```

## 🚀 使用方法

### 1. 添加模块依赖

在其他模块中添加依赖：

```kotlin
dependencies {
    implementation(project(":common"))
}
```

### 2. 初始化音乐服务

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var musicServiceConnection: MusicServiceConnection
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 获取音乐服务连接
        musicServiceConnection = MusicServiceConnection.getInstance(this)
        
        // 监听连接状态
        musicServiceConnection.isConnected.observe(this) { connected ->
            if (connected) {
                // 服务已连接，可以开始使用
                setupMusicPlayer()
            }
        }
    }
    
    private fun setupMusicPlayer() {
        // 播放音乐
        musicServiceConnection.playFromMediaId("song_id")
        
        // 监听播放状态
        musicServiceConnection.playbackState.observe(this) { state ->
            // 处理播放状态变化
        }
    }
}
```

### 3. 配置音乐数据源

```kotlin
// 使用JsonSource加载音乐数据
val jsonSource = JsonSource(context, "music_catalog.json")
jsonSource.load { success ->
    if (success) {
        println("音乐数据加载成功")
    }
}
```

### 4. 验证集成

```kotlin
// 快速验证Media3集成是否成功
Media3IntegrationTest.quickTest(context)

// 或者运行详细测试
Media3IntegrationTestExample.detailedTest(context)
```

## 🔧 配置要求

### 权限声明 (AndroidManifest.xml)
```xml
<!-- Media3 音频播放所需权限 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
```

### 服务声明
```xml
<!-- Media3 音乐播放服务 -->
<service
    android:name="com.wzh.common.media.MusicService"
    android:exported="true"
    android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaLibraryService" />
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>

<!-- AlbumArt内容提供者 -->
<provider
    android:name="com.wzh.common.media.library.AlbumArtContentProvider"
    android:authorities="${applicationId}.albumart"
    android:exported="false"
    android:grantUriPermissions="true" />
```

## 📚 API文档

### MusicServiceConnection主要方法

```kotlin
// 播放控制
fun play()                              // 播放
fun pause()                             // 暂停
fun stop()                              // 停止
fun skipToNext()                        // 下一首
fun skipToPrevious()                    // 上一首
fun seekTo(position: Long)              // 跳转位置

// 媒体管理
fun playFromMediaId(mediaId: String)    // 播放指定媒体
fun setMediaItems(items: List<MediaItem>) // 设置播放列表

// 状态监听
val isConnected: LiveData<Boolean>      // 连接状态
val playbackState: LiveData<Int>        // 播放状态
val nowPlaying: LiveData<MediaItem>     // 当前播放项
```

### JsonSource配置

```kotlin
// JSON数据格式示例
{
  "music": [
    {
      "id": "song_001",
      "title": "歌曲标题",
      "artist": "艺术家",
      "album": "专辑名称",
      "source": "https://example.com/song.mp3",
      "image": "https://example.com/cover.jpg",
      "duration": 240000
    }
  ]
}
```

### 扩展方法使用

```kotlin
// 字符串扩展
val title = "My Song"
val encoded = title.urlEncoded
val uri = "https://example.com/song.mp3".toUri()

// MediaItem创建
val playableItem = createPlayableMediaItem(
    mediaId = "song1",
    uri = Uri.parse("https://example.com/song.mp3"),
    title = "歌曲标题",
    artist = "艺术家",
    album = "专辑"
)

// Player状态检查
if (player.isPlaying) {
    player.playPause()
}
```

## 🧪 测试和调试

### 集成测试
```kotlin
// 运行完整的Media3集成测试
val testResult = Media3IntegrationTest(context).runAllTests()
println("测试通过率: ${testResult.passedTests}/${testResult.totalTests}")

// 快速验证
val success = Media3IntegrationTest.quickTest(context)
```

### 测试示例
```kotlin
// 在Activity中运行测试
Media3IntegrationTestExample.runTestInActivity(context)

// 详细测试
Media3IntegrationTestExample.detailedTest(context)

// 性能测试
Media3IntegrationTestExample.performanceTest(context)
```

### 调试工具
```kotlin
// 启用详细日志 (仅在Debug模式)
if (BuildConfig.DEBUG) {
    // Media3日志会自动输出到Logcat
}
```

## 📋 依赖信息

### 主要依赖
- **Media3**: 1.8.0 (ExoPlayer, UI, Session)
- **Kotlin Coroutines**: 1.7.3
- **Glide**: 4.16.0 (图片加载)
- **Gson**: 2.10.1 (JSON解析)
- **Guava**: 32.1.3 (工具库)

### 完整依赖列表
参见 `build.gradle.kts` 文件

## 🏗️ 核心组件详解

### MusicService
基于MediaLibraryService的音乐播放服务，提供：
- 媒体会话管理
- 播放控制
- 媒体库浏览
- 后台播放支持

### MusicServiceConnection
连接和控制MusicService的客户端类，提供：
- 服务连接管理
- 播放控制接口
- 状态监听
- 生命周期管理

### JsonSource
从JSON数据源加载音乐信息，支持：
- 网络和本地JSON解析
- 专辑封面缓存
- 媒体项转换
- 异步加载

### BrowseTree
分层媒体浏览结构，支持：
- 分类浏览（推荐、专辑、最近播放）
- 搜索功能
- 动态加载
- 层次结构管理

### AlbumArtContentProvider
专辑封面内容提供者，提供：
- 网络图片缓存
- Content URI转换
- Glide集成
- 超时处理

### 扩展方法集合
简化Media3 API使用的扩展方法：
- **StringExtensions**: 字符串处理工具
- **MediaItemExtensions**: MediaItem便捷操作
- **PlayerExtensions**: 播放器状态管理
- **FileExtensions**: 文件URI转换

## ⚠️ 注意事项

### 开发注意事项
- 保持模块的通用性，避免添加特定业务逻辑
- Media3相关代码需要在主线程中调用
- 及时释放播放器资源，避免内存泄漏
- 定期清理不再使用的代码
- 保持良好的文档和注释

### 性能优化
- 使用Glide缓存专辑封面，减少网络请求
- 合理使用协程处理异步操作
- 避免在UI线程进行耗时操作
- 及时释放不需要的资源

### 兼容性
- 最低支持Android API 28 (Android 9.0)
- 建议目标SDK为最新稳定版本
- 定期更新Media3版本以获得最新功能和修复
- 注意ProGuard规则配置

## 🔄 版本历史

- **v1.0.0**: 初始版本，基础Media3集成
- **v1.1.0**: 添加JsonSource和BrowseTree
- **v1.2.0**: 完善扩展方法和示例代码
- **v1.3.0**: 添加集成测试和文档
- **v1.4.0**: 完善权限配置和ProGuard规则

## 📞 支持

如有问题或建议，请：
1. 查看 `MEDIA3_SETUP_README.md` 详细配置指南
2. 运行集成测试验证配置
3. 检查日志输出定位问题
4. 参考示例代码正确使用API
5. 查看各组件的Example文件

## 📚 参考资源

- [Media3官方文档](https://developer.android.com/guide/topics/media/media3)
- [ExoPlayer迁移指南](https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide)
- [MediaSession指南](https://developer.android.com/guide/topics/media/media3/media-session)

---

**最后更新**: 2025年1月  
**Media3版本**: 1.8.0  
**维护状态**: 活跃维护