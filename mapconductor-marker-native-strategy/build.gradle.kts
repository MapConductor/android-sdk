plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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

// Publishing configuration
val libraryGroupId = project.findProperty("libraryGroupId") as String? ?: "com.mapconductor"
val libraryArtifactId = "marker-native-strategy"
val libraryVersion = project.findProperty("libraryVersion") as String? ?: project.property("versionName") as String
val libraryName = "MapConductor Native Marker Strategy"
val libraryDescription = "High-performance C++ marker rendering strategies for MapConductor"

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    // Since Android libraries don't have javadoc task by default, create empty jar
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
