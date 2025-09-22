# 设计文档

## 概述

本设计文档描述了如何在Android项目中配置中国国内镜像源和集成Media3库的技术方案。设计采用渐进式配置方法，确保网络访问的稳定性和依赖管理的清晰性。

## 架构

### 镜像源配置架构
```
项目根目录
├── settings.gradle.kts (仓库配置)
├── gradle.properties (全局属性)
└── app/
    └── build.gradle.kts (应用依赖)
```

### 依赖管理架构
```
Version Catalog (libs.versions.toml)
├── [versions] - 版本定义
├── [libraries] - 库定义  
└── [plugins] - 插件定义
```

## 组件和接口

### 1. 仓库配置组件

**位置**: `settings.gradle.kts`

**职责**: 
- 配置插件和依赖仓库
- 设置镜像源优先级
- 提供回退机制

**接口设计**:
```kotlin
pluginManagement {
    repositories {
        // 国内镜像源 (优先级高)
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        // 官方源 (回退)
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        // 同样的镜像源配置
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()
    }
}
```

### 2. 版本管理组件

**位置**: `gradle/libs.versions.toml`

**职责**:
- 统一管理Media3版本
- 定义相关依赖版本
- 确保版本兼容性

**接口设计**:
```toml
[versions]
media3 = "1.4.1"
# 其他现有版本...

[libraries]
# Media3 核心库
media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
media3-common = { group = "androidx.media3", name = "media3-common", version.ref = "media3" }
media3-session = { group = "androidx.media3", name = "media3-session", version.ref = "media3" }
```

### 3. 应用依赖组件

**位置**: `app/build.gradle.kts`

**职责**:
- 引入Media3依赖
- 配置ProGuard规则
- 设置权限要求

## 数据模型

### 配置数据模型

```kotlin
data class RepositoryConfig(
    val name: String,
    val url: String,
    val priority: Int,
    val isBackup: Boolean = false
)

data class Media3Dependencies(
    val exoplayer: String,
    val ui: String, 
    val common: String,
    val session: String
)
```

### 镜像源优先级模型

```
优先级 1: 阿里云公共仓库 (maven.aliyun.com/repository/public)
优先级 2: 阿里云Google仓库 (maven.aliyun.com/repository/google)  
优先级 3: 腾讯云仓库 (mirrors.cloud.tencent.com/nexus/repository/maven-public)
优先级 4: Google官方 (google())
优先级 5: Maven中央仓库 (mavenCentral())
```

## 错误处理

### 网络连接错误
- **场景**: 镜像源不可访问
- **处理**: 自动回退到下一个可用源
- **日志**: 记录失败的仓库和重试信息

### 依赖解析错误  
- **场景**: 版本冲突或依赖缺失
- **处理**: 提供清晰的错误信息和解决建议
- **回退**: 使用已知稳定版本

### 编译错误
- **场景**: Media3配置导致编译失败
- **处理**: 检查最小SDK版本兼容性
- **修复**: 提供ProGuard规则和权限配置

## 测试策略

### 1. 配置验证测试
```kotlin
// 验证仓库配置
@Test
fun testRepositoryConfiguration() {
    // 验证镜像源可访问性
    // 验证依赖解析成功
}
```

### 2. 依赖集成测试
```kotlin
// 验证Media3依赖
@Test  
fun testMedia3Dependencies() {
    // 验证所有Media3模块可用
    // 验证版本兼容性
}
```

### 3. 编译测试
```kotlin
// 验证项目编译
@Test
fun testProjectCompilation() {
    // 执行gradle build
    // 验证无编译错误
}
```

### 4. 性能测试
- **下载速度测试**: 对比使用镜像源前后的依赖下载时间
- **同步时间测试**: 测量gradle sync的完成时间
- **网络回退测试**: 模拟网络问题验证回退机制

## 实现细节

### ProGuard配置
Media3需要特定的ProGuard规则来避免混淆关键类：

```proguard
# Media3 ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
```

### 权限要求
音乐播放应用需要以下权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

### 最小SDK要求
Media3要求最小SDK版本为21，当前项目设置为24，满足要求。

## 部署考虑

### 团队协作
- 所有配置文件应提交到版本控制
- 提供清晰的注释说明配置目的
- 建立配置变更的代码审查流程

### 维护性
- 定期更新Media3版本
- 监控镜像源的可用性
- 建立配置问题的快速诊断流程