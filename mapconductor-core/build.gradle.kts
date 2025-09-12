plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    alias(libs.plugins.kotlin.compose)
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    android.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

android {
    namespace = "com.mapconductor.core"
    compileSdk = project.property("compileSdk").toString().toInt()
    ndkVersion = "27.0.12077973"

    defaultConfig {
        minSdk = project.property("minSdk").toString().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++17")
                arguments +=
                    listOf(
                        "-DANDROID_STL=c++_static",
                        "-DANDROID_PLATFORM=android-$minSdk",
                    )
                // Add 16KB page alignment arguments for Android 15+ compatibility
                arguments += listOf("-DCMAKE_SHARED_LINKER_FLAGS=-Wl,-z,max-page-size=16384")
                version = "3.22.1"
            }
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion =
            project.property("kotlinCompilerExtensionVersion").toString()
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro",
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packagingOptions {
        pickFirst("**/libc++_shared.so")
        pickFirst("**/libjsc.so")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(project.property("javaVersion").toString())
        targetCompatibility = JavaVersion.toVersion(project.property("javaVersion").toString())
    }

    kotlinOptions {
        jvmTarget = project.property("jvmTarget").toString()
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    // Configure NDK path for modern Gradle
    if (project.hasProperty("android.ndkPath")) {
        ndkPath = project.property("android.ndkPath").toString()
    } else if (System.getenv("ANDROID_NDK_ROOT") != null) {
        ndkPath = System.getenv("ANDROID_NDK_ROOT")
    } else {
        // Fallback to default SDK location
        val androidSdkRoot =
            System.getenv("ANDROID_SDK_ROOT")
                ?: System.getenv("ANDROID_HOME")
                ?: "C:\\Users\\masashi\\AppData\\Local\\Android\\Sdk"
        ndkPath = "$androidSdkRoot\\ndk\\27.0.12077973"
    }
}

dependencies {
    implementation(libs.ui)
    compileOnly(libs.androidx.core.ktx)
    compileOnly(libs.androidx.foundation)

    compileOnly(libs.androidx.ui)
    compileOnly(libs.androidx.ui.graphics)
    compileOnly(libs.androidx.ui.tooling.preview)
    compileOnly(platform(libs.androidx.compose.bom)) // ← bomでバージョン合わせる
    // Lifecycle（MapView用）
    compileOnly(libs.androidx.lifecycle.runtime.ktx)
    compileOnly(libs.androidx.lifecycle.common.java8)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
