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

android {
    namespace = "com.mapconductor.here"
    compileSdk = project.property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = project.property("minSdk").toString().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(project.property("javaVersion").toString())
        targetCompatibility = JavaVersion.toVersion(project.property("javaVersion").toString())
    }
    kotlinOptions {
        jvmTarget = project.property("jvmTarget").toString()
    }
}

dependencies {

    compileOnly(libs.androidx.ui)
    compileOnly(libs.androidx.ui.tooling.preview)
    compileOnly(libs.androidx.foundation)
    compileOnly(platform(libs.androidx.compose.bom)) // ← bomでバージョン合わせる
    // Lifecycle（MapView用）
    compileOnly(libs.androidx.lifecycle.runtime.ktx)
    compileOnly(libs.androidx.lifecycle.common.java8)

    // libs
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to arrayOf("heresdk*."))))
    compileOnly(project(":mapconductor-core"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Publishing configuration
val libraryGroupId = project.findProperty("libraryGroupId") as String? ?: "com.mapconductor"
val libraryArtifactId = "for-here"
val libraryVersion = project.findProperty("libraryVersion") as String? ?: "1.0.0"

// Set project version for NMCP plugin
version = libraryVersion
val libraryName = "MapConductor for HERE Maps"
val libraryDescription = "HERE Maps implementation for MapConductor unified mapping library"

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
