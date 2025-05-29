plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    alias(libs.plugins.kotlin.compose) // ← 任意（配布したい場合）
}

android {
    namespace = "com.mapconductor.mapbox"
    compileSdk = project.property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = project.property("minSdk").toString().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = project.property("kotlinCompilerExtensionVersion").toString()
    }

    buildTypes {
        release {
            isMinifyEnabled = project.property("isMinifyEnabled").toString().toBoolean()
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(project.property("javaVersion").toString())
        targetCompatibility = JavaVersion.toVersion(project.property("javaVersion").toString())
    }
    kotlinOptions {
        jvmTarget = project.property("jvmTarget").toString()
    }
}

dependencies {

    compileOnly(libs.androidx.ui)
    compileOnly(libs.androidx.foundation)
    compileOnly(libs.androidx.ui.tooling.preview)
    compileOnly(platform(libs.androidx.compose.bom)) // ← bomでバージョン合わせる
    // Lifecycle（MapView用）
    compileOnly(libs.androidx.lifecycle.runtime.ktx)
    compileOnly(libs.androidx.lifecycle.common.java8)

    // Mapbox SDK
    compileOnly(libs.mapbox.android)
    compileOnly(project(":mapconductor-core"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}