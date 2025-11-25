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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime)

    debugImplementation(project(":mapconductor-core"))

    // Google Maps SDK
    implementation(libs.play.services.maps)
    debugImplementation(project(":mapconductor-for-googlemaps"))

    // Mapbox SDK
    implementation(libs.mapbox.android)
    debugImplementation(project(":mapconductor-for-mapbox"))

    // MapLibre SDK
    implementation(libs.maplibre.sdk)
    implementation(libs.maplibre.annotation)
    debugImplementation(project(":mapconductor-for-maplibre"))

    // arcgis
    debugImplementation(project(":mapconductor-for-arcgis"))
    implementation(libs.arcgis.maps.kotlin)
    implementation(platform(libs.arcgis.maps.kotlin.toolkit.bom))
    implementation(libs.arcgis.maps.kotlin.toolkit.geoview.compose)
    implementation(libs.arcgis.maps.kotlin.toolkit.authentication)

    // Here Maps SDK
    debugImplementation(project(":mapconductor-for-here"))
    implementation(
        fileTree(
            mapOf(
                "dir" to rootDir.resolve("libs").toString(),
                "include" to arrayOf("heresdk*.jar", "heresdk*.aar"),
            ),
        ),
    )
}
