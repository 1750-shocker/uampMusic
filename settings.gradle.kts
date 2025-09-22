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

rootProject.name = "uampMusic"
include(":app")
include(":common")
