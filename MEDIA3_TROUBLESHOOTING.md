# Media3 故障排除指南

本文档提供了Media3音乐播放器集成过程中常见问题的解决方案。

## 📋 目录

- [依赖问题](#依赖问题)
- [编译错误](#编译错误)
- [运行时问题](#运行时问题)
- [播放问题](#播放问题)
- [权限问题](#权限问题)
- [性能问题](#性能问题)
- [调试技巧](#调试技巧)

## 🔧 依赖问题

### 问题1: 依赖解析失败

**症状**: 
```
Could not resolve androidx.media3:media3-exoplayer:1.8.0
```

**可能原因**:
- 网络连接问题
- 镜像源配置错误
- Gradle缓存损坏

**解决方案**:
```bash
# 1. 清理Gradle缓存
./gradlew clean
./gradlew --refresh-dependencies

# 2. 检查网络连接
ping maven.aliyun.com
ping repo1.maven.org

# 3. 重新同步项目
./gradlew --refresh-dependencies
```

### 问题1.1: AAR元数据兼容性错误

**症状**:
```
Dependency 'androidx.activity:activity-ktx:1.11.0' requires libraries and applications that
depend on it to compile against version 36 or later of the Android APIs.
:app is currently compiled against android-35.
```

**解决方案**:
```kotlin
// 1. 更新Android Gradle Plugin版本
// gradle/libs.versions.toml
[versions]
agp = "8.9.1"  // 从8.9.0升级到8.9.1+

// 2. 更新compileSdk版本
// app/build.gradle.kts 和 common/build.gradle.kts
android {
    compileSdk = 36  // 从35升级到36
}

// 3. 降级兼容的依赖版本
[versions]
coreKtx = "1.15.0"           // 从1.17.0降级
activityCompose = "1.9.3"    // 从1.11.0降级
lifecycleRuntimeKtx = "2.8.7" // 从2.9.4降级
composeBom = "2024.12.01"    // 从2025.09.00降级
```

### 问题2: 版本冲突

**症状**:
```
Dependency 'androidx.media3:media3-common' has different version for the compile (1.8.0) and runtime (1.7.0) classpaths
```

**解决方案**:
```kotlin
// 在build.gradle.kts中强制使用统一版本
configurations.all {
    resolutionStrategy {
        force("androidx.media3:media3-common:1.8.0")
        force("androidx.media3:media3-exoplayer:1.8.0")
        force("androidx.media3:media3-session:1.8.0")
        force("androidx.media3:media3-ui:1.8.0")
    }
}
```

### 问题3: 镜像源访问慢

**症状**: 依赖下载速度极慢

**解决方案**:
```kotlin
// 调整settings.gradle.kts中的镜像源顺序
repositories {
    // 优先使用阿里云镜像
    maven {
        name = "Aliyun Google"
        url = uri("https://maven.aliyun.com/repository/google")
    }
    maven {
        name = "Aliyun Public"  
        url = uri("https://maven.aliyun.com/repository/public")
    }
    // 官方源作为备选
    google()
    mavenCentral()
}
```

## ⚠️ 编译错误

### 问题1: Media3类找不到

**症状**:
```
Unresolved reference: androidx.media3.exoplayer.ExoPlayer
```

**解决方案**:
```kotlin
// 确保在build.gradle.kts中添加了正确的依赖
dependencies {
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.common)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)
}
```

### 问题4: API级别不兼容

**症状**:
```
Call requires API level 28 (current min is 24): android.content.Context#getMainExecutor
```

**解决方案**:
```kotlin
// 在build.gradle.kts中更新minSdk到28
android {
    defaultConfig {
        minSdk = 28  // 更新到API 28
        targetSdk = 35
    }
}
```

### 问题5: Kotlin版本不兼容

**症状**:
```
This version (1.2.0) of the Compose Compiler requires Kotlin version 1.5.10 but you appear to be using Kotlin version 2.0.21
```

**解决方案**:
```kotlin
// 在build.gradle.kts中指定兼容的Kotlin版本
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```

### 问题6: ProGuard混淆问题

**症状**: Release版本崩溃，Debug版本正常

**解决方案**:
确保在`proguard-rules.pro`中添加了完整的Media3规则：
```proguard
# Media3 ProGuard Rules
-keep class androidx.media3.** { *; }
-keep class com.wzh.common.media.** { *; }
-keepclassmembers class * {
    @androidx.media3.common.util.UnstableApi <methods>;
}
```

## 🚨 运行时问题

### 问题1: ContentProvider类找不到

**症状**:
```
java.lang.ClassNotFoundException: Didn't find class "com.wzh.common.media.library.AlbumArtContentProvider"
```

**可能原因**:
- app模块没有依赖common模块
- ProGuard混淆规则不完整
- 模块间依赖配置错误

**解决方案**:
```kotlin
// 1. 确保app模块依赖common模块
// app/build.gradle.kts
dependencies {
    implementation(project(":common"))
    // 其他依赖...
}

// 2. 检查ProGuard规则
// app/proguard-rules.pro
-keep class com.wzh.common.media.** { *; }

// 3. 验证AndroidManifest.xml中的声明
<provider
    android:name="com.wzh.common.media.library.AlbumArtContentProvider"
    android:authorities="${applicationId}.albumart"
    android:exported="false"
    android:grantUriPermissions="true" />
```

### 问题2: 服务连接失败

**症状**:
```
MusicServiceConnection: Failed to connect to service
```

**解决方案**:
```xml
<!-- 确保在AndroidManifest.xml中正确声明服务 -->
<service
    android:name="com.wzh.common.media.MusicService"
    android:exported="true"
    android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaLibraryService" />
    </intent-filter>
</service>
```

### 问题3: 权限被拒绝

**症状**:
```
SecurityException: Permission denied
```

**解决方案**:
```kotlin
// 运行时请求权限
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) 
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
            REQUEST_CODE_AUDIO_PERMISSION
        )
    }
}
```

### 问题4: 内存泄漏

**症状**: 应用内存持续增长

**解决方案**:
```kotlin
// 确保在适当的时机释放资源
override fun onDestroy() {
    super.onDestroy()
    musicServiceConnection.release()
    exoPlayer?.release()
}
```

## 🎵 播放问题

### 问题1: 播放无声音

**症状**: 播放器显示正在播放，但听不到声音

**可能原因**:
- 音频焦点未获取
- 音量设置为0
- 音频格式不支持
- 设备静音

**解决方案**:
```kotlin
// 1. 检查音频焦点
val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
val result = audioManager.requestAudioFocus(
    focusChangeListener,
    AudioManager.STREAM_MUSIC,
    AudioManager.AUDIOFOCUS_GAIN
)

// 2. 检查音量设置
val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
if (currentVolume == 0) {
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0)
}

// 3. 检查播放器状态
if (player.playbackState == Player.STATE_READY && !player.isPlaying) {
    player.play()
}
```

### 问题2: 播放卡顿

**症状**: 音频播放断断续续

**解决方案**:
```kotlin
// 增加缓冲区大小
val loadControl = DefaultLoadControl.Builder()
    .setBufferDurationsMs(
        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
    )
    .build()

val exoPlayer = ExoPlayer.Builder(context)
    .setLoadControl(loadControl)
    .build()
```

### 问题3: 网络音频加载失败

**症状**:
```
ExoPlaybackException: Source error
```

**解决方案**:
```kotlin
// 添加网络状态检查
private fun isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
           capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
}

// 在播放前检查网络
if (!isNetworkAvailable()) {
    showNetworkErrorDialog()
    return
}
```

## 🔐 权限问题

### 问题1: Android 13+ 媒体权限

**症状**: 在Android 13+设备上无法访问媒体文件

**解决方案**:
```xml
<!-- 在AndroidManifest.xml中添加新的媒体权限 -->
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />

<!-- 保持向后兼容 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
```

### 问题2: 前台服务权限

**症状**: 
```
SecurityException: Starting FGS without permission
```

**解决方案**:
```xml
<!-- 确保声明了前台服务权限 -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
```

## ⚡ 性能问题

### 问题1: 启动速度慢

**症状**: 应用启动时间过长

**解决方案**:
```kotlin
// 延迟初始化非关键组件
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 在后台线程初始化Media3组件
        CoroutineScope(Dispatchers.IO).launch {
            initializeMediaComponents()
        }
    }
}
```

### 问题2: 内存使用过高

**症状**: 应用内存占用持续增长

**解决方案**:
```kotlin
// 优化Glide配置
val glideOptions = RequestOptions()
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .skipMemoryCache(false)
    .override(300, 300) // 限制图片尺寸

// 及时清理缓存
Glide.get(context).clearMemory()
```

## 🔍 调试技巧

### 1. 启用详细日志

```kotlin
// 在Application中启用Media3日志
if (BuildConfig.DEBUG) {
    // Media3会自动输出详细日志到Logcat
    Log.d("Media3Debug", "Debug mode enabled")
}
```

### 2. 使用集成测试

```kotlin
// 运行集成测试验证配置
val testResult = Media3IntegrationTest(context).runAllTests()
if (!testResult.success) {
    Log.e("Media3", "Integration test failed: ${testResult.passedTests}/${testResult.totalTests}")
}
```

### 3. 监控播放器状态

```kotlin
player.addListener(object : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        Log.d("Player", "State changed to: ${getStateString(playbackState)}")
    }
    
    override fun onPlayerError(error: PlaybackException) {
        Log.e("Player", "Playback error: ${error.message}", error)
    }
})

private fun getStateString(state: Int): String {
    return when (state) {
        Player.STATE_IDLE -> "IDLE"
        Player.STATE_BUFFERING -> "BUFFERING"
        Player.STATE_READY -> "READY"
        Player.STATE_ENDED -> "ENDED"
        else -> "UNKNOWN"
    }
}
```

### 4. 检查依赖树

```bash
# 查看依赖关系
./gradlew :common:dependencies --configuration debugCompileClasspath

# 查找特定依赖
./gradlew :common:dependencies --configuration debugCompileClasspath | grep media3
```

### 5. 网络调试

```kotlin
// 添加网络拦截器
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .build()
```

## 📞 获取帮助

如果以上解决方案都无法解决问题，请：

1. **检查日志**: 查看Logcat中的详细错误信息
2. **运行测试**: 使用`Media3IntegrationTest`验证配置
3. **查看文档**: 参考`MEDIA3_SETUP_README.md`
4. **检查版本**: 确认使用的是兼容的版本组合
5. **清理重建**: 尝试`./gradlew clean build`

## 🔗 相关资源

- [Media3官方文档](https://developer.android.com/guide/topics/media/media3)
- [ExoPlayer故障排除](https://exoplayer.dev/troubleshooting.html)
- [Android媒体指南](https://developer.android.com/guide/topics/media)

---

**最后更新**: 2025年1月  
**适用版本**: Media3 1.8.0  
**维护状态**: 活跃维护