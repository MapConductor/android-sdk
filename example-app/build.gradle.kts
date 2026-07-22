import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
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

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}

android {
    namespace = "com.mapconductor.example"
    compileSdk = project.property("compileSdk").toString().toInt()

    defaultConfig {
        applicationId = "com.mapconductor.example"
        minSdk = project.property("minSdk").toString().toInt()
        targetSdk = project.property("targetSdk").toString().toInt()
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
        vectorDrawables {
            useSupportLibrary = true
        }

        versionCode = 6
        versionName = "1.0.5"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        create("local") {
            initWith(getByName("debug"))
            isDebuggable = true
            isMinifyEnabled = false
        }

        debug {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-rules-release.pro",
            )

            buildConfigField("String", "BUILD_CONFIG_VERSION", "\"release\"")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources {
            excludes += "META-INF/versions/9/previous-compilation-data.bin"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
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

    compileOptions {

        sourceCompatibility = JavaVersion.toVersion(project.property("javaVersion").toString())

        targetCompatibility = JavaVersion.toVersion(project.property("javaVersion").toString())
    }

    buildFeatures {

        compose = true

        buildConfig = true
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

    testOptions {

        unitTests {

            isIncludeAndroidResources = true
        }
    }
}

// Read libraryVersion from each module's gradle.properties for the 'local' build type
fun localVersion(moduleDir: String): String {
    val props = Properties()
    val propsFile = rootProject.file("$moduleDir/gradle.properties")
    if (!propsFile.exists()) return "1.0.0"
    propsFile.inputStream().use { stream -> props.load(stream) }
    return props.getProperty("libraryVersion") ?: "1.0.0"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.appcompat)

    // Google Maps SDK
//    implementation(libs.play.services.maps)

    // Here Maps SDK
    implementation(
        fileTree(
            mapOf(
                "dir" to rootDir.resolve("libs").toString(),
                "include" to arrayOf("heresdk*.jar", "heresdk*.aar"),
            ),
        ),
    )

    // Use project dependency for debug, Maven artifact for release
    // Align versions in release via the project BOM
//    releaseImplementation(platform(project(":mapconductor-bom")))
    releaseImplementation(libs.mapconductor.core)
    releaseImplementation(libs.mapconductor.icons)
    releaseImplementation(libs.mapconductor.googlemaps)
    releaseImplementation(libs.mapconductor.here)
    releaseImplementation(libs.mapconductor.mapbox)
    releaseImplementation(libs.mapconductor.arcgis)
    releaseImplementation(libs.mapconductor.maplibre)
    releaseImplementation(libs.mapconductor.marker.strategy)
    releaseImplementation(libs.mapconductor.marker.native.strategy)
    releaseImplementation(libs.mapconductor.marker.clustering)
    releaseImplementation(libs.mapconductor.geojson)

    debugImplementation(project(":android-sdk-core"))
    debugImplementation(project(":android-icons"))
    debugImplementation(project(":android-for-googlemaps"))
    debugImplementation(project(":android-for-here"))
    debugImplementation(project(":android-for-mapbox"))
    debugImplementation(project(":android-for-maplibre"))
    debugImplementation(project(":android-for-arcgis"))
    debugImplementation(project(":android-for-tomtom"))
    debugImplementation(project(":android-marker-clustering"))
    debugImplementation(project(":android-heatmap"))
    debugImplementation(project(":android-geojson-layer"))

    // local build type: uses MavenLocal published artifacts (published by publishAllLocal)
    // Map SDKs must be declared explicitly because published AARs expose them as runtime-only scope
    "localImplementation"(libs.play.services.maps)
    "localImplementation"(libs.mapbox.android)
    "localImplementation"(libs.maplibre.sdk)
    "localImplementation"("com.mapconductor:core:${localVersion("android-sdk-core")}")
    "localImplementation"("com.mapconductor:icons:${localVersion("android-icons")}")
    "localImplementation"("com.mapconductor:for-googlemaps:${localVersion("android-for-googlemaps")}")
    "localImplementation"("com.mapconductor:for-here:${localVersion("android-for-here")}")
    "localImplementation"("com.mapconductor:for-mapbox:${localVersion("android-for-mapbox")}")
    "localImplementation"("com.mapconductor:for-arcgis:${localVersion("android-for-arcgis")}")
    "localImplementation"("com.mapconductor:for-maplibre:${localVersion("android-for-maplibre")}")
    "localImplementation"(libs.tomtom.map.display)
    "localImplementation"("com.mapconductor:for-tomtom:${localVersion("android-for-tomtom")}")
    "localImplementation"("com.mapconductor:marker-clustering:${localVersion("android-marker-clustering")}")
    "localImplementation"("com.mapconductor:heatmap:${localVersion("android-heatmap")}")
    "localImplementation"("com.mapconductor:geojson:${localVersion("android-geojson-layer")}")

    implementation(libs.androidx.vectordrawable)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
