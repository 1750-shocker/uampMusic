pluginManagement {
    repositories {
        // 国内镜像源 - 优先级最高，提升下载速度
        maven {
            name = "Aliyun Public"
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            name = "Aliyun Google"
            url = uri("https://maven.aliyun.com/repository/google")
        }
        maven {
            name = "Tencent Cloud"
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        }
        
        // 官方源 - 作为回退选项
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 国内镜像源 - 优先级最高，确保快速下载依赖
        maven {
            name = "Aliyun Public"
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            name = "Aliyun Google"
            url = uri("https://maven.aliyun.com/repository/google")
        }
        maven {
            name = "Tencent Cloud"
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        }
        
        // 官方源 - 作为回退选项，确保依赖完整性
        google()
        mavenCentral()
    }
}

rootProject.name = "uampMusic"
include(":app")
 