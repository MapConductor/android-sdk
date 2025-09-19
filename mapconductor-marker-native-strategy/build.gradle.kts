plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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
    namespace = "com.mapconductor.marker.nativestrategy"
    compileSdk = project.property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = project.property("minSdk").toString().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    compileOnly(project(":mapconductor-core"))

    // Compose dependencies for BitmapIcon/DefaultIcon
    compileOnly(libs.androidx.ui)
    compileOnly(libs.androidx.foundation)
    compileOnly(platform(libs.androidx.compose.bom))

    // Coroutines for Semaphore and withPermit
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
