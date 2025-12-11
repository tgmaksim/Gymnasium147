import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val propsFile = rootProject.file("variables.properties")
val props = Properties()

if (propsFile.exists()) {
    props.load(propsFile.inputStream())
}

val buildNumber = props.getProperty("BUILD_NUMBER", "1").toInt()
val appVersion: String = props.getProperty("APP_VERSION", "0.1.0")
val domain: String = props.getProperty("DOMAIN")
val apiKey: String = props.getProperty("API_KEY")
val docsViewUrl: String = props.getProperty("DOCS_VIEW_URL")

// Увеличение номера сборки при каждой компиляции
val newBuildNumber = buildNumber + 1

gradle.taskGraph.whenReady {
    if (hasTask(":app:assembleDebug") || hasTask(":app:assembleRelease")) {
        props["BUILD_NUMBER"] = newBuildNumber.toString()
        props.store(propsFile.writer(), null)
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

        buildConfigField("String", "DOMAIN", "\"$domain\"")
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        buildConfigField("String", "DOCS_VIEW_ULR", "\"$docsViewUrl\"")

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
        buildConfig = true
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
    implementation(libs.okhttp)
    implementation(libs.okio)
    implementation(libs.androidx.documentfile)
    testImplementation(libs.junit)
    implementation(libs.androidx.core.ktx)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}