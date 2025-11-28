import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Увеличиваем номер сборки при каждой компиляции
val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties()

if (versionPropsFile.exists()) {
    versionProps.load(versionPropsFile.inputStream())
}

val buildNumber = versionProps.getProperty("BUILD_NUMBER", "1").toInt()
val appVersion = versionProps.getProperty("APP_VERSION", "0.1.0")

val newBuildNumber = buildNumber + 1

gradle.taskGraph.whenReady {
    if (hasTask(":app:assembleDebug") || hasTask(":app:assembleRelease")) {
        versionProps["BUILD_NUMBER"] = newBuildNumber.toString()
        versionProps.store(versionPropsFile.writer(), null)
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
}

android {
    namespace = "ru.tgmaksim.gymnasium"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "ru.tgmaksim.gymnasium"
        minSdk = 30
        targetSdk = 36
        versionCode = newBuildNumber
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.junit)
    implementation(libs.androidx.core.ktx)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}