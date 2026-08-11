plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

ktlint {
    android.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

// ドライバー実装点（@InternalMapConductorApi）を使うためのオプトイン。
// android-for-* は地図SDKドライバーなので、モジュール単位で許可する。
kotlin {
    compilerOptions {
        optIn.add("com.mapconductor.core.InternalMapConductorApi")
    }
}

android {
    namespace = "com.mapconductor.openmobilemaps"
    compileSdk = project.property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = project.property("minSdk").toString().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(project.property("javaVersion").toString())
        targetCompatibility = JavaVersion.toVersion(project.property("javaVersion").toString())
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.foundation)

    // Lifecycle（MapView が Lifecycle を要求する）
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.common.java8)

    // Open Mobile Maps SDK
    api(libs.openmobilemaps.mapscore)
    // ローカルタイルサーバの 404 を 204 へ書き換えるインターセプタで使う
    // （mapscore の transitive 依存と同じ版。DataLoader が okhttp3.Interceptor を受け取る）
    implementation("com.squareup.okhttp3:okhttp:5.1.0")

    api(project(":android-sdk-compose"))

    testImplementation(libs.junit)
}
