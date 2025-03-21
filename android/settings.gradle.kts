rootProject.name = "flutter_blue"

dependencyResolutionManagement {

    //RepositoriesMode.PREFER_PROJECT 优先使用项目中的依赖
    // RepositoriesMode.PREFER_SETTINGS 优先使用settings.gradle.kts中的依赖
    // RepositoriesMode.FAIL_ON_PROJECT_REPOS 如果项目中存在依赖，则报错
    // 默认是RepositoriesMode.PREFER_SETTINGS
    // repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        // 配置阿里云镜像源
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        google()
        mavenCentral()
    }
} 