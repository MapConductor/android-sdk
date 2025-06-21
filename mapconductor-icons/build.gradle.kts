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
    namespace = "com.mapconductor.icons"
    compileSdk = project.property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = project.property("minSdk").toString().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    compileOnly(libs.androidx.foundation)
    compileOnly(platform(libs.androidx.compose.bom))
    compileOnly(project(":mapconductor-core"))
//    implementation(libs.androidx.appcompat)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
}
