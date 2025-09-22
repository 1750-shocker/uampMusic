# Common Module - 音乐数据源

这个模块包含了音乐应用的通用组件，特别是用于从JSON文件加载音乐数据的`JsonSource`。

## 主要组件

### JsonSource
从远程JSON文件加载音乐目录的数据源类。

**特性：**
- 支持从网络URL加载JSON音乐目录
- 自动处理相对路径转换为绝对路径
- 集成专辑封面ContentProvider
- 基于Media3的MediaItem格式
- 协程支持的异步加载

### MusicSource & AbstractMusicSource
音乐数据源接口和抽象基类，提供统一的数据访问接口。

**特性：**
- 基于状态的生命周期管理（创建→初始化中→已初始化/错误）
- 回调机制：whenReady()支持多监听器注册
- 线程安全的状态变更通知
- 支持搜索功能
- 实现Iterable接口，支持for-in遍历

### MusicRepository
音乐数据仓库，提供状态管理和数据访问。

**特性：**
- StateFlow状态管理
- 加载状态跟踪
- 错误处理
- 根据ID查找音乐项

### AlbumArtContentProvider
专辑封面内容提供者，将网络图片URI转换为content://形式。

**特性：**
- 使用Glide进行智能图片下载和缓存
- 支持超时机制（30秒）保证用户体验
- Content Provider接口，与Android系统完美集成
- 支持ExoPlayer、通知系统、Android Auto等
- 提供稳定的URI引用，隐藏真实数据源
- 自动处理权限控制和访问管理

### BrowseTree
媒体浏览树，用于组织音乐内容的层次结构。

**特性：**
- 支持推荐、专辑、最近播放等分类浏览
- 自动构建专辑层次结构
- 基于Media3的MediaItem格式
- 支持搜索功能

## 使用方法

### 1. 基本使用

```kotlin
// 创建仓库实例
val repository = MusicRepository()

// 加载音乐数据
repository.loadMusicFromJson("https://example.com/music/catalog.json")

// 监听数据变化
repository.musicItems.collect { musicItems ->
    // 处理音乐列表
}
```

### 2. 直接使用JsonSource

```kotlin
val jsonUri = Uri.parse("https://example.com/music/catalog.json")
val jsonSource = JsonSource(jsonUri)

// 注册回调监听器
jsonSource.whenReady { success ->
    if (success) {
        println("数据加载成功！")
        // 遍历音乐项
        for (mediaItem in jsonSource) {
            println("${mediaItem.mediaMetadata.title} - ${mediaItem.mediaMetadata.artist}")
        }
    } else {
        println("数据加载失败")
    }
}

// 异步加载数据
CoroutineScope(Dispatchers.Main).launch {
    jsonSource.load()
}
```

### 3. 使用BrowseTree

```kotlin
// 创建浏览树
val browseTree = BrowseTree(
    context = context,
    musicSource = musicSource,
    recentMediaId = "recent_song_id"
)

// 获取根节点内容
val rootItems = browseTree[UAMP_BROWSABLE_ROOT]

// 获取推荐内容
val recommendedItems = browseTree[UAMP_RECOMMENDED_ROOT]

// 获取专辑列表
val albumItems = browseTree[UAMP_ALBUMS_ROOT]
```

### 4. JSON格式要求

```json
{
  "music": [
    {
      "id": "1",
      "title": "歌曲标题",
      "artist": "艺术家",
      "album": "专辑名称",
      "genre": "流行",
      "source": "https://example.com/music/song1.mp3",
      "image": "https://example.com/images/album1.jpg",
      "trackNumber": 1,
      "totalTrackCount": 12,
      "duration": 240,
      "site": "example.com"
    }
  ]
}
```

## 依赖

- Media3 (ExoPlayer, UI, Common, Session)
- Kotlin Coroutines
- Gson (JSON解析)
- Glide (图片加载和缓存)

## 权限要求

在AndroidManifest.xml中需要以下权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## ContentProvider注册

AlbumArtContentProvider已在模块的AndroidManifest.xml中注册：

```xml
<provider
    android:name=".media.library.AlbumArtContentProvider"
    android:authorities="com.wzh.uampmusic.albumart"
    android:exported="false" />
```

## 从旧版本迁移

这个实现是从基于ExoPlayer v2和MediaBrowserCompat的旧版本重写而来，主要变化：

1. **MediaMetadataCompat** → **MediaItem + MediaMetadata**
2. **MediaBrowserCompat.MediaItem** → **MediaItem**
3. **支持v4媒体库** → **Media3库**
4. 保持了相同的JSON格式兼容性
5. 保持了相同的功能特性

## 示例

查看 `JsonSourceExample.kt` 文件获取完整的使用示例。