# 需求文档

## 介绍

本文档描述了修复app模块UI层与Media3框架和Compose兼容性问题的需求。当前的UI实现存在包名不一致、缺失依赖、资源引用错误等问题，需要系统性地修复以确保应用能够正常编译和运行。

## 需求

### 需求 1

**用户故事:** 作为开发者，我希望修复包名引用不一致的问题，以便代码能够正确编译

#### 验收标准

1. WHEN 检查所有Kotlin文件的import语句 THEN 系统应该使用正确的包名 `com.wzh.uampmusic` 而不是 `com.gta.myuamp02`
2. WHEN 修复包名引用 THEN 所有UI组件、ViewModel和工具类应该使用统一的包名结构
3. WHEN 编译项目 THEN 不应该出现包名相关的编译错误

### 需求 2

**用户故事:** 作为开发者，我希望修复缺失的数据模型和资源文件，以便UI组件能够正常工作

#### 验收标准

1. WHEN UI组件引用MediaItemData类 THEN 该类应该在正确的包中存在并可访问
2. WHEN UI组件引用drawable资源 THEN 所有必需的图标资源应该存在于app模块中
3. WHEN 应用运行时 THEN 不应该出现ClassNotFoundException或资源未找到的错误

### 需求 3

**用户故事:** 作为开发者，我希望确保Compose UI与Media3的集成正确，以便音乐播放功能正常工作

#### 验收标准

1. WHEN UI组件与MusicServiceConnection交互 THEN 应该正确处理Media3的播放状态和元数据
2. WHEN 用户点击播放控制按钮 THEN 应该正确调用Media3的传输控制接口
3. WHEN 播放状态改变时 THEN UI应该实时反映当前的播放状态

### 需求 4

**用户故事:** 作为开发者，我希望修复ViewModel的依赖注入问题，以便应用架构正确工作

#### 验收标准

1. WHEN 创建ViewModel实例 THEN 依赖注入应该正确提供MusicServiceConnection
2. WHEN ViewModel工厂创建ViewModel THEN 不应该出现类型转换或实例化错误
3. WHEN Activity或Fragment使用ViewModel THEN 应该能够正确获取和使用ViewModel实例

### 需求 5

**用户故事:** 作为开发者，我希望确保Compose导航正确配置，以便用户能够在不同屏幕间正常导航

#### 验收标准

1. WHEN 用户点击媒体项目 THEN 应该正确导航到相应的屏幕（列表或播放界面）
2. WHEN 导航事件触发 THEN NavController应该正确处理路由和参数传递
3. WHEN 用户使用返回按钮 THEN 应该正确返回到上一个屏幕

### 需求 6

**用户故事:** 作为开发者，我希望修复Coil图片加载库的配置，以便专辑封面能够正确显示

#### 验收标准

1. WHEN UI组件加载专辑封面 THEN Coil应该正确处理网络图片和本地资源
2. WHEN 图片加载失败时 THEN 应该显示默认的占位图片
3. WHEN 图片加载成功时 THEN 应该平滑地显示专辑封面并支持交叉淡入动画

### 需求 7

**用户故事:** 作为开发者，我希望确保所有必需的Compose依赖正确配置，以便UI能够使用最新的Material3组件

#### 验收标准

1. WHEN 使用Material3组件 THEN 应该正确应用主题和样式
2. WHEN 使用Compose导航 THEN 应该包含必要的navigation-compose依赖
3. WHEN 使用Coil图片加载 THEN 应该包含coil-compose依赖并正确配置