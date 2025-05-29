
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}


android {
    namespace = "com.mapconductor.example"
    compileSdk = project.property("compileSdk").toString().toInt()

    defaultConfig {
        applicationId = "com.mapconductor.example"
        minSdk = project.property("minSdk").toString().toInt()
        targetSdk =  project.property("targetSdk").toString().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    composeOptions {
        kotlinCompilerExtensionVersion =
            project.property("kotlinCompilerExtensionVersion").toString()
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
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}
secrets {
    // To add your Maps API key to this project:
    // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
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
    implementation(libs.androidx.appcompat)


    // Google Maps SDK
    implementation(libs.play.services.maps)
    // Here Maps SDK
    val hereSdkAarName: String by project
    implementation(files("${rootProject.projectDir}/libs/$hereSdkAarName.aar"))
    // Mapbox SDK
    implementation(libs.mapbox.android)

    // ArcGIS Maps for Kotlin - SDK dependency
    implementation(libs.arcgis.maps.kotlin)
    implementation(platform(libs.arcgis.maps.kotlin.toolkit.bom))
    implementation(libs.arcgis.maps.kotlin.toolkit.geoview.compose)
    implementation(libs.arcgis.maps.kotlin.toolkit.authentication)

    // Map Conductor
    implementation(project(":mapconductor-core"))
    implementation(project(":mapconductor-for-googlemaps"))
    implementation(project(":mapconductor-for-here"))
    implementation(project(":mapconductor-for-mapbox"))
    implementation(project(":mapconductor-for-arcgis"))
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