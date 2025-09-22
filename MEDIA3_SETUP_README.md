# Media3 音乐播放器集成指南

本文档详细说明了如何在Android项目中集成和配置Media3音乐播放功能。

## 📋 目录

- [概述](#概述)
- [依赖配置](#依赖配置)
- [镜像源配置](#镜像源配置)
- [权限配置](#权限配置)
- [ProGuard配置](#proguard配置)
- [核心组件](#核心组件)
- [使用示例](#使用示例)
- [测试验证](#测试验证)
- [故障排除](#故障排除)
- [版本更新](#版本更新)

## 🎯 概述

Media3是Google推出的新一代媒体播放框架，用于替代已弃用的ExoPlayer 2.x和MediaBrowserServiceCompat。本项目集成了完整的Media3音乐播放功能，包括：

- **ExoPlayer**: 高性能音频播放引擎
- **MediaSession**: 媒体会话管理
- **MediaLibraryService**: 媒体库服务
- **UI组件**: 播放器界面组件

## 📦 依赖配置

### 版本目录配置 (gradle/libs.versions.toml)

```toml
[versions]
media3 = "1.8.0"

[libraries]
media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
media3-common = { group = "androidx.media3", name = "media3-common", version.ref = "media3" }
media3-session = { group = "androidx.media3", name = "media3-session", version.ref = "media3" }
```

### 应用模块依赖 (app/build.gradle.kts)

```kotlin
dependencies {
    // Media3核心依赖
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)
    implementation(libs.media3.session)
}
```

### 公共模块依赖 (common/build.gradle.kts)

```kotlin
dependencies {
    // Media3依赖
    api(libs.media3.exoplayer)
    api(libs.media3.ui)
    api(libs.media3.common)
    api(libs.media3.session)
    
    // 支持库
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)
    implementation(libs.glide)
    implementation(libs.guava)
    implementation(libs.kotlinx.coroutines.guava)
}
```

## 🌐 镜像源配置

### settings.gradle.kts配置

为了提高国内用户的依赖下载速度，同时确保Android工具依赖的完整性，采用以下配置策略：

```kotlin
pluginManagement {
    repositories {
        // 官方源优先 - 确保Android工具依赖完整性
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        
        // 国内镜像源 - 用于其他依赖，提升下载速度
        maven {
            name = "Aliyun Public"
            url = uri("https://maven.aliyun.com/repository/public")
            content {
                excludeGroupByRegex("com\\.android.*")
                excludeGroupByRegex("com\\.google.*")
                excludeGroupByRegex("androidx.*")
            }
        }
        maven {
            name = "Aliyun Google"
            url = uri("https://maven.aliyun.com/repository/google")
        }
        
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 官方源优先 - 确保Android和Google依赖完整性
        google()
        mavenCentral()
        
        // 国内镜像源 - 用于其他依赖，提升下载速度
        maven {
            name = "Aliyun Public"
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            name = "Aliyun Google"
            url = uri("https://maven.aliyun.com/repository/google")
        }
    }
}
```

### 镜像源说明

- **Google官方源**: 优先处理Android和Google相关依赖，确保完整性
- **阿里云镜像**: 加速其他第三方依赖下载
- **Maven Central**: 标准Maven仓库，作为补充

## 🔐 权限配置

### AndroidManifest.xml权限声明

```xml
<!-- Media3 音频播放所需权限 -->
<!-- 网络权限：用于从网络加载音频文件 -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 网络状态权限：用于检查网络连接状态 -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 前台服务权限：用于音频播放服务 -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<!-- 媒体前台服务权限 (Android 14+) -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

<!-- 唤醒锁权限：防止播放时设备休眠 -->
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- 音频焦点权限：管理音频焦点 -->
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<!-- 读取外部存储权限：访问本地音频文件 (可选) -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />

<!-- 读取媒体音频权限 (Android 13+) -->
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

## 🛡️ ProGuard配置

### app/proguard-rules.pro

```proguard
# ========== Media3 ProGuard Rules ==========

# Keep Media3 ExoPlayer classes
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.ui.** { *; }
-keep class androidx.media3.session.** { *; }

# Keep Media3 data source classes
-keep class androidx.media3.datasource.** { *; }
-keep class androidx.media3.extractor.** { *; }

# Keep Media3 decoder classes
-keep class androidx.media3.decoder.** { *; }

# Keep Media3 transformer classes (if using)
-keep class androidx.media3.transformer.** { *; }

# Keep Media3 cast extension (if using)
-keep class androidx.media3.cast.** { *; }

# Keep Media3 effect classes (if using)
-keep class androidx.media3.effect.** { *; }

# Preserve Media3 annotations
-keepattributes *Annotation*

# Keep Media3 native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Media3 serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Media3 Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep Media3 MediaSession related classes
-keep class androidx.media3.session.MediaSession { *; }
-keep class androidx.media3.session.MediaController { *; }
-keep class androidx.media3.session.MediaLibraryService { *; }
-keep class androidx.media3.session.MediaBrowser { *; }

# Keep custom Media3 service classes (adjust package name as needed)
-keep class com.wzh.common.media.** { *; }

# Prevent obfuscation of Media3 callback methods
-keepclassmembers class * {
    @androidx.media3.common.util.UnstableApi <methods>;
}

# Keep Media3 format classes
-keep class androidx.media3.common.Format { *; }
-keep class androidx.media3.common.MediaItem { *; }
-keep class androidx.media3.common.MediaMetadata { *; }

# ========== End Media3 ProGuard Rules ==========
```

## 🏗️ 核心组件

### 1. MusicService
基于MediaLibraryService的音乐播放服务，提供：
- 媒体会话管理
- 播放控制
- 媒体库浏览

### 2. MusicServiceConnection
连接和控制MusicService的客户端类，提供：
- 服务连接管理
- 播放控制接口
- 状态监听

### 3. JsonSource
从JSON数据源加载音乐信息，支持：
- 网络和本地JSON解析
- 专辑封面缓存
- 媒体项转换

### 4. BrowseTree
分层媒体浏览结构，支持：
- 分类浏览
- 搜索功能
- 动态加载

### 5. 扩展方法
简化Media3 API使用的扩展方法集合

## 💡 使用示例

### 基本播放控制

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var musicServiceConnection: MusicServiceConnection
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化服务连接
        musicServiceConnection = MusicServiceConnection.getInstance(this)
        
        // 监听连接状态
        musicServiceConnection.isConnected.observe(this) { connected ->
            if (connected) {
                // 服务已连接，可以开始播放
                startPlayback()
            }
        }
    }
    
    private fun startPlayback() {
        // 播放指定媒体
        musicServiceConnection.playFromMediaId("song_id")
        
        // 或者设置播放列表
        val mediaItems = listOf(
            MediaItem.Builder().setMediaId("song1").build(),
            MediaItem.Builder().setMediaId("song2").build()
        )
        musicServiceConnection.setMediaItems(mediaItems)
        musicServiceConnection.play()
    }
}
```

### 监听播放状态

```kotlin
// 监听播放状态
musicServiceConnection.playbackState.observe(this) { state ->
    when (state) {
        Player.STATE_IDLE -> println("播放器空闲")
        Player.STATE_BUFFERING -> println("缓冲中")
        Player.STATE_READY -> println("准备就绪")
        Player.STATE_ENDED -> println("播放结束")
    }
}

// 监听当前播放项
musicServiceConnection.nowPlaying.observe(this) { mediaItem ->
    println("正在播放: ${mediaItem.mediaMetadata.title}")
}
```

## 🧪 测试验证

### 快速验证

```kotlin
// 在Activity中快速验证Media3集成
Media3IntegrationTestExample.quickValidation(this)
```

### 详细测试

```kotlin
// 运行详细的集成测试
Media3IntegrationTestExample.detailedTest(this)
```

### 应用启动验证

```kotlin
// 在Application中验证
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Media3IntegrationTestExample.validateOnAppStart(this)
    }
}
```

## 🔧 故障排除

### 常见问题

#### 1. 依赖解析失败
**问题**: 无法下载Media3依赖
**解决方案**: 
- 检查网络连接
- 确认镜像源配置正确
- 清理Gradle缓存: `./gradlew clean`

#### 2. 编译错误
**问题**: Media3相关类无法找到
**解决方案**:
- 确认依赖版本兼容性
- 检查import语句
- 同步项目: `./gradlew --refresh-dependencies`

#### 3. 运行时崩溃
**问题**: 播放时应用崩溃
**解决方案**:
- 检查权限配置
- 确认服务声明正确
- 查看ProGuard规则

#### 4. 播放无声音
**问题**: 播放器运行但无声音
**解决方案**:
- 检查音频焦点权限
- 确认媒体文件格式支持
- 检查设备音量设置

### 调试技巧

1. **启用详细日志**:
```kotlin
// 在Application中启用Media3日志
if (BuildConfig.DEBUG) {
    ExoPlayer.Builder(this)
        .setLogLevel(Log.VERBOSE)
        .build()
}
```

2. **检查依赖树**:
```bash
./gradlew :common:dependencies --configuration debugCompileClasspath
```

3. **验证权限**:
```kotlin
// 检查权限是否已授予
if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) 
    != PackageManager.PERMISSION_GRANTED) {
    // 请求权限
}
```

## 🔄 版本更新

### 更新Media3版本

1. **修改版本目录**:
```toml
# gradle/libs.versions.toml
[versions]
media3 = "1.9.0"  # 更新到新版本
```

2. **同步依赖**:
```bash
./gradlew --refresh-dependencies
```

3. **测试验证**:
```kotlin
Media3IntegrationTest.quickTest(context)
```

### 版本兼容性检查

- **Android API**: 确保compileSdk支持新版本要求，当前使用API 36，最低支持API 28 (Android 9.0)
- **Gradle Plugin**: 当前使用AGP 8.9.1，确保版本兼容性
- **Kotlin**: 当前使用Kotlin 2.0.21，确认版本兼容

### 迁移指南

从ExoPlayer 2.x迁移到Media3时需要注意：

1. **包名变更**: `com.google.android.exoplayer2` → `androidx.media3`
2. **API变更**: 部分API方法名和参数有变化
3. **依赖变更**: 使用新的Media3依赖

## 📚 参考资源

- [Media3官方文档](https://developer.android.com/guide/topics/media/media3)
- [ExoPlayer迁移指南](https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide)
- [MediaSession指南](https://developer.android.com/guide/topics/media/media3/media-session)

## 📝 维护说明

### 定期维护任务

1. **依赖更新**: 每月检查Media3版本更新
2. **安全审查**: 定期检查权限使用合理性
3. **性能监控**: 监控播放性能和内存使用
4. **兼容性测试**: 在新Android版本上测试

### 配置文件维护

- **settings.gradle.kts**: 镜像源可用性检查
- **proguard-rules.pro**: 根据Media3更新调整规则
- **AndroidManifest.xml**: 权限声明随Android版本更新

---

**最后更新**: 2025年1月
**维护者**: 开发团队
**版本**: 1.0.0