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

kotlin {
    compilerOptions {
        jvmTarget.set(
            org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(
                project.property("jvmTarget").toString(),
            ),
        )
    }
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
    implementation(libs.androidx.appcompat)

    // Google Maps SDK
    implementation(libs.play.services.maps)

    // Here Maps SDK
    implementation(
        fileTree(
            mapOf(
                "dir" to rootDir.resolve("libs").toString(),
                "include" to arrayOf("heresdk*.jar", "heresdk*.aar"),
            ),
        ),
    )

    // Mapbox SDK
    implementation(libs.mapbox.android)

    // ArcGIS Maps for Kotlin - SDK dependency
    implementation(libs.arcgis.maps.kotlin)
    implementation(platform(libs.arcgis.maps.kotlin.toolkit.bom))
    implementation(libs.arcgis.maps.kotlin.toolkit.geoview.compose)
    implementation(libs.arcgis.maps.kotlin.toolkit.authentication)

    // MapLibre SDK
    implementation(libs.maplibre.sdk)
    implementation(libs.maplibre.annotation)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)

    // Map Conductor
//    implementation("com.mapconductor:core")
//    implementation("com.mapconductor:icons")
//    implementation("com.mapconductor:for-googlemaps")
//    implementation("com.mapconductor:for-here")
//    implementation("com.mapconductor:for-mapbox")
//    implementation("com.mapconductor:for-arcgis")
//    implementation("com.mapconductor:marker-strategy")
//    implementation("com.mapconductor:marker-native-strategy")

    // Use project dependency for debug, Maven artifact for release
    // Align versions in release via the project BOM
    releaseImplementation(platform(project(":mapconductor-bom")))
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

    debugImplementation(project(":android-sdk-core"))
    debugImplementation(project(":android-icons"))
    debugImplementation(project(":android-for-googlemaps"))
    debugImplementation(project(":android-for-here"))
    debugImplementation(project(":android-for-mapbox"))
    debugImplementation(project(":android-for-arcgis"))
    debugImplementation(project(":android-for-maplibre"))
    debugImplementation(project(":android-marker-clustering"))
    debugImplementation(project(":android-heatmap"))

    implementation(libs.androidx.vectordrawable)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
