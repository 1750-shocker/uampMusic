# 设计文档

## 概述

本设计文档描述了修复app模块UI层与Media3框架和Compose兼容性问题的技术方案。设计采用系统性修复方法，确保包名一致性、依赖完整性和架构正确性，使音乐播放应用能够正常编译和运行。

## 架构

### 包结构架构
```
app/src/main/java/com/wzh/uampmusic/
├── MainActivity.kt
├── ui/
│   ├── components/
│   │   └── MediaItemCard.kt
│   ├── screens/
│   │   ├── MediaItemListScreen.kt
│   │   └── NowPlayingScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewModels/
│   ├── MainActivityViewModel.kt
│   ├── MediaItemListViewModel.kt
│   └── NowPlayingViewModel.kt
├── utils/
│   ├── InjectorUtils.kt
│   └── Event.kt
└── data/
    └── MediaItemData.kt (新增)
```

### 依赖管理架构
```
app/build.gradle.kts
├── Compose BOM (统一版本管理)
├── Media3依赖 (从common模块继承)
├── Navigation Compose
├── Coil Compose
└── ViewModel Compose
```

## 组件和接口

### 1. 包名修复组件

**职责**: 统一所有文件的包名引用

**修复策略**:
```kotlin
// 错误的引用
import com.gta.myuamp02.compose.ui.screens.MediaItemListScreen

// 正确的引用  
import com.wzh.uampmusic.ui.screens.MediaItemListScreen
```

**影响文件**:
- MainActivity.kt
- 所有UI屏幕文件
- 所有ViewModel文件
- InjectorUtils.kt

### 2. 数据模型组件

**位置**: `app/src/main/java/com/wzh/uampmusic/data/MediaItemData.kt`

**职责**: 提供UI层使用的媒体项目数据模型

**接口设计**:
```kotlin
data class MediaItemData(
    val mediaId: String,
    val title: String,
    val subtitle: String,
    val albumArtUri: Uri,
    val browsable: Boolean,
    val playbackRes: Int = 0
)
```

### 3. 资源管理组件

**位置**: `app/src/main/res/drawable/`

**职责**: 提供UI组件所需的图标资源

**必需资源**:
```xml
<!-- 播放控制图标 -->
ic_play_arrow_black_24dp.xml
ic_pause_black_24dp.xml
ic_skip_previous_black_24dp.xml
ic_skip_next_black_24dp.xml

<!-- 导航图标 -->
ic_chevron_right_black_24dp.xml
ic_signal_wifi_off_black_24dp.xml

<!-- 默认图片 -->
ic_album_black_24dp.xml
default_art.xml
```

### 4. 依赖注入组件

**位置**: `app/src/main/java/com/wzh/uampmusic/utils/InjectorUtils.kt`

**职责**: 提供正确的ViewModel工厂和依赖注入

**接口设计**:
```kotlin
object InjectorUtils {
    private fun provideMusicServiceConnection(context: Context): MusicServiceConnection
    
    fun provideMainActivityViewModel(context: Context): ViewModelProvider.Factory
    fun provideMediaItemListViewModel(context: Context, mediaId: String): ViewModelProvider.Factory  
    fun provideNowPlayingViewModel(context: Context): ViewModelProvider.Factory
}
```

### 5. Compose集成组件

**职责**: 确保Compose UI与Media3正确集成

**关键接口**:
```kotlin
// MainActivity中的Compose设置
@Composable
fun MusicApp(
    viewModel: MainActivityViewModel,
    navController: NavHostController = rememberNavController()
)

// 屏幕组件接口
@Composable
fun MediaItemListScreen(
    mediaId: String,
    onMediaItemClick: (MediaItemData) -> Unit,
    onNavigateToNowPlaying: () -> Unit
)

@Composable  
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    onPlayClick: (String) -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit
)
```

## 数据模型

### MediaItemData模型
```kotlin
data class MediaItemData(
    val mediaId: String,           // 媒体项目唯一标识
    val title: String,             // 标题
    val subtitle: String,          // 副标题（艺术家/专辑）
    val albumArtUri: Uri,          // 专辑封面URI
    val browsable: Boolean,        // 是否可浏览（文件夹vs文件）
    val playbackRes: Int = 0       // 播放状态图标资源ID
)
```

### UI状态模型
```kotlin
// MainActivityViewModel.UiState
data class UiState(
    val currentMediaId: String = "",
    val isConnected: Boolean = false,
    val navigationEvent: NavigationEvent? = null
)

// MediaItemListViewModel.UiState  
data class UiState(
    val mediaItems: List<MediaItemData> = emptyList(),
    val isLoading: Boolean = true,
    val hasNetworkError: Boolean = false
)

// NowPlayingViewModel.UiState
data class UiState(
    val mediaMetadata: NowPlayingMetadata? = null,
    val mediaPosition: Long = 0L,
    val mediaButtonRes: Int = R.drawable.ic_play_arrow_black_24dp,
    val isPlaying: Boolean = false
)
```

## 错误处理

### 包名引用错误
- **场景**: import语句使用错误的包名
- **处理**: 系统性替换所有错误的包名引用
- **验证**: 编译时检查确保无包名相关错误

### 资源缺失错误
- **场景**: UI组件引用不存在的drawable资源
- **处理**: 创建所有必需的矢量图标资源
- **回退**: 提供默认资源避免运行时崩溃

### 依赖注入错误
- **场景**: ViewModel工厂类型转换失败
- **处理**: 确保工厂方法返回正确的ViewModel类型
- **验证**: 运行时测试ViewModel创建过程

### Compose集成错误
- **场景**: Compose组件与Media3状态不同步
- **处理**: 正确使用StateFlow和collectAsState
- **监控**: 添加日志记录状态变化

## 测试策略

### 1. 编译测试
```kotlin
// 验证包名修复
@Test
fun testPackageImportsCompile() {
    // 确保所有文件能够成功编译
    // 验证无包名相关错误
}
```

### 2. UI组件测试
```kotlin
// 验证UI组件渲染
@Test
fun testMediaItemCardRendering() {
    // 测试MediaItemCard组件正确显示
    // 验证点击事件处理
}

@Test
fun testNowPlayingScreenLayout() {
    // 测试播放界面布局正确
    // 验证控制按钮功能
}
```

### 3. ViewModel集成测试
```kotlin
// 验证ViewModel创建
@Test
fun testViewModelInjection() {
    // 测试依赖注入正确工作
    // 验证ViewModel状态管理
}
```

### 4. 导航测试
```kotlin
// 验证Compose导航
@Test
fun testNavigationFlow() {
    // 测试屏幕间导航正确
    // 验证参数传递和状态保持
}
```

## 实现细节

### 依赖配置
app/build.gradle.kts需要添加的依赖：
```kotlin
dependencies {
    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // 确保Media3依赖正确
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui) 
    implementation(libs.media3.common)
    implementation(libs.media3.session)
}
```

### ProGuard规则
确保发布版本正确保留必要的类：
```proguard
# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**
```

### 主题配置
确保Material3主题正确应用：
```kotlin
@Composable
fun MyUamp02Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

## 部署考虑

### 版本兼容性
- 确保Compose BOM版本与Kotlin编译器版本兼容
- 验证Media3版本与目标SDK版本兼容
- 测试在不同Android版本上的运行情况

### 性能优化
- 使用Compose的remember和LaunchedEffect优化重组
- 实现图片加载的内存缓存策略
- 优化列表滚动性能

### 维护性
- 建立清晰的包结构约定
- 提供详细的代码注释和文档
- 建立UI组件的设计系统规范