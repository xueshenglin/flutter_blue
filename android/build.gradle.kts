group = "com.pauldemarco.flutter_blue"

buildscript {
    val kotlinVersion by extra("1.8.22")
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.7.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.library") apply true
    kotlin("android") 
}

android {
    namespace = "com.pauldemarco.flutter_blue"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
            java.srcDirs("src/main/java")
            java.srcDirs("$projectDir/src/generated/source/proto/release/java")
            resources.srcDirs("$projectDir/../protos")
        }
        getByName("test").java.srcDirs("src/test/kotlin")
    }

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.0")
        testImplementation("org.mockito:mockito-core:5.7.0")
        implementation("com.google.protobuf:protobuf-javalite:3.11.4")
    }
}