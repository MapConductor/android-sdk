plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("maven-publish")
    id("signing")
    id("com.gradleup.nmcp") version "1.5.0"
}

ktlint {
    android.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

android {
    namespace = "com.mapconductor.vectortile"
    compileSdk = project.property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = project.property("minSdk").toString().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        aarMetadata {
            minCompileSdk = project.property("compileSdk").toString().toInt()
        }
        // Only arm64 is prebuilt today; adding ABIs is a matter of adding
        // targets to mapconductor-vectortile/scripts/build-android.sh.
        ndk { abiFilters += "arm64-v8a" }
    }

    buildFeatures {
        compose = true
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    if (findProject(":android-sdk-compose") != null) {
        implementation(project(":android-sdk-compose"))
    } else {
        implementation("com.mapconductor:compose:${project.findProperty("coreLibraryVersion") as String? ?: "1.0.0"}")
    }

    // `VectorStyleRasteriser` はあちらが宣言し、こちらが実装する
    // （`VectorTileRasteriser`）。依存の向きはこの一方向だけ。
    if (findProject(":android-vectorstyle") != null) {
        api(project(":android-vectorstyle"))
    } else {
        api("com.mapconductor:vectorstyle:${project.findProperty("coreLibraryVersion") as String? ?: "1.0.0"}")
    }

    implementation(libs.kotlinx.coroutines.android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // The marker tile cost benchmark measures android-sdk-core's renderer
    // against this module's Rust PNG encoder, so the test source set needs
    // core on its compile classpath directly.
    androidTestImplementation(project(":android-sdk-core"))
    // The street tree benchmark builds its own MarkerIconInterface, whose
    // members are Compose geometry types.
    androidTestImplementation(libs.androidx.ui)
}
