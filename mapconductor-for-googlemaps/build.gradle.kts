plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
    id("org.jlleitschuh.gradle.ktlint")
    id("maven-publish")
    id("signing")
}

ktlint {
    android.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

// Task to create secrets.properties in root project if it doesn't exist
tasks.register("ensureSecretsFiles") {
    doFirst {
        val secretsFile = rootProject.file("secrets.properties")
        val defaultsFile = rootProject.file("local.defaults.properties")

        // Create secrets.properties if it doesn't exist
        if (!secretsFile.exists()) {
            secretsFile.createNewFile()
            println("Created secrets.properties in root project")
        }

        // Create local.defaults.properties if it doesn't exist
        if (!defaultsFile.exists()) {
            defaultsFile.createNewFile()
            println("Created local.defaults.properties in root project")
        }

        // Ensure GOOGLE_MAPS_API_KEY exists in secrets.properties
        val secretsContent = secretsFile.readText()
        if (!secretsContent.contains("GOOGLE_MAPS_API_KEY=")) {
            secretsFile.appendText("GOOGLE_MAPS_API_KEY=GOOGLE_MAPS_API_KEY\n")
            println("Added GOOGLE_MAPS_API_KEY to secrets.properties")
        }

        // Ensure GOOGLE_MAPS_API_KEY exists in local.defaults.properties
        val defaultsContent = defaultsFile.readText()
        if (!defaultsContent.contains("GOOGLE_MAPS_API_KEY=")) {
            defaultsFile.appendText("GOOGLE_MAPS_API_KEY=GOOGLE_MAPS_API_KEY\n")
            println("Added GOOGLE_MAPS_API_KEY to local.defaults.properties")
        }
    }
}

// Function to read API key from secrets.properties
fun getGoogleMapsApiKey(): String {
    val secretsFile = rootProject.file("secrets.properties")
    val defaultsFile = rootProject.file("local.defaults.properties")

    // Try to read from secrets.properties first
    if (secretsFile.exists()) {
        val content = secretsFile.readText()
        content.lines().forEach { line ->
            if (line.startsWith("GOOGLE_MAPS_API_KEY=")) {
                val value = line.substringAfter("=").trim()
                if (value.isNotEmpty() && value != "GOOGLE_MAPS_API_KEY") {
                    return value
                }
            }
        }
    }

    // Fallback to local.defaults.properties
    if (defaultsFile.exists()) {
        val content = defaultsFile.readText()
        content.lines().forEach { line ->
            if (line.startsWith("GOOGLE_MAPS_API_KEY=")) {
                val value = line.substringAfter("=").trim()
                if (value.isNotEmpty()) {
                    return value
                }
            }
        }
    }

    // Return placeholder if no valid key found
    return "GOOGLE_MAPS_API_KEY"
}

android {
    namespace = "com.mapconductor.googlemaps"
    compileSdk = project.property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = project.property("minSdk").toString().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Set the Google Maps API key from secrets.properties
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = getGoogleMapsApiKey()
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

    // Run ensureSecretsFiles before processing manifests
    tasks.matching { it.name.contains("process") && it.name.contains("Manifest") }.configureEach {
        dependsOn("ensureSecretsFiles")
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(platform(libs.androidx.compose.bom)) // ← bomでバージョン合わせる
    // Lifecycle（MapView用）
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.common.java8)

    // Google Maps SDK
    implementation(libs.play.services.maps)
    implementation(project(":mapconductor-core"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Publishing configuration
val libraryGroupId = project.findProperty("libraryGroupId") as String? ?: "com.mapconductor"
val libraryArtifactId = "for-googlemaps"
val libraryVersion = project.findProperty("libraryVersion") as String? ?: "1.0.0"

// Set project version for NMCP plugin
version = libraryVersion
val libraryName = "MapConductor for Google Maps"
val libraryDescription = "Google Maps implementation for MapConductor unified mapping library"

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = libraryGroupId
                artifactId = libraryArtifactId
                version = libraryVersion

                artifact(javadocJar.get())

                pom {
                    name.set(libraryName)
                    description.set(libraryDescription)
                    url.set(
                        project.findProperty("libraryUrl") as String?
                            ?: "https://github.com/MapConductor/android-sdk",
                    )

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set(project.findProperty("developerId") as String? ?: "mapconductor")
                            name.set(project.findProperty("developerName") as String? ?: "MapConductor Team")
                            email.set(project.findProperty("developerEmail") as String? ?: "dev@mapconductor.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/MapConductor/android-sdk.git")
                        developerConnection
                            .set("scm:git:ssh://github.com:MapConductor/android-sdk.git")
                        url.set(
                            project.findProperty("scmUrl") as String?
                                ?: "https://github.com/MapConductor/android-sdk.git",
                        )
                    }
                }
            }
        }

        repositories {
            maven {
                name = "GitHubPackages"
                setUrl("https://maven.pkg.github.com/MapConductor/android-sdk")
                credentials {
                    username =
                        project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                            ?: System.getenv("GITHUB_ACTOR")
                    password =
                        project.findProperty("gpr.key") as String? ?: System.getenv("GPR_TOKEN")
                            ?: System.getenv("GITHUB_TOKEN")
                }
            }

            maven {
                name = "OSSRH"
                val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
                setUrl(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                credentials {
                    username = project.findProperty("ossrh.username") as String? ?: System.getenv("OSSRH_USERNAME")
                    password = project.findProperty("ossrh.password") as String? ?: System.getenv("OSSRH_PASSWORD")
                }
            }
        }
    }

    if (project.hasProperty("signing.keyId")) {
        signing {
            sign(publishing.publications["release"])
        }
    }
}
