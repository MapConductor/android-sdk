plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jlleitschuh.gradle.ktlint")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
}

ktlint {
    android.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

android {
    namespace = "com.mapconductor.simplemapapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mapconductor.simplemapapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            buildConfigField("String", "BUILD_CONFIG_VERSION", "\"release\"")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources {
            excludes += "META-INF/versions/9/previous-compilation-data.bin"
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            excludes += "META-INF/androidx.*.version"
            excludes += "**/*.proto"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
            excludes += "META-INF/*.version"
            excludes += "**/*.properties"
            excludes += "kotlin/**"
            excludes += "DebugProbesKt.bin"
            excludes += "**/kotlin-tooling-metadata.json"
            excludes += "**/*.txt"
            excludes += "**/*.md"
            excludes += "**/*.html"
        }

        jniLibs {
            useLegacyPackaging = true
        }
    }

    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(
            org.jetbrains.kotlin.gradle.dsl.JvmTarget
                .fromTarget("11"),
        )
    }
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime)

    implementation(project(":android-sdk-core"))

    // Google Maps SDK
    implementation(libs.play.services.maps)
    implementation(project(":android-for-googlemaps"))

    // Mapbox SDK
    implementation(libs.mapbox.android)
    implementation(project(":android-for-mapbox"))
//    implementation("com.mapbox.plugin:maps-locationcomponent:11.17.0")

    // MapLibre SDK
    implementation(libs.maplibre.sdk)
    implementation(libs.maplibre.annotation)
    implementation(project(":android-for-maplibre"))

    // arcgis
    implementation(project(":android-for-arcgis"))
    implementation(libs.arcgis.maps.kotlin)
    implementation(platform(libs.arcgis.maps.kotlin.toolkit.bom))
    implementation(libs.arcgis.maps.kotlin.toolkit.geoview.compose)
    implementation(libs.arcgis.maps.kotlin.toolkit.authentication)

    // Here Maps SDK
    implementation(project(":android-for-here"))
    implementation(
        fileTree(
            mapOf(
                "dir" to rootDir.resolve("libs").toString(),
                "include" to arrayOf("heresdk*.jar", "heresdk*.aar"),
            ),
        ),
    )

    implementation(project(":android-heatmap"))
}
