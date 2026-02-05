plugins {
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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
    namespace = "com.mapconductor.core"
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
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
    // Make Compose dependencies implementation instead of compileOnly for proper runtime support
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    compileOnly(libs.androidx.ui.tooling.preview)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.foundation)
    implementation(libs.net.sf.geographiclib)
    implementation(libs.jts.core)

    // Core dependencies - use api to avoid version conflicts
    implementation(libs.androidx.core.ktx)
    // Lifecycle（MapView用）
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.common.java8)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Publishing configuration
val libraryGroupId = project.findProperty("libraryGroupId") as String? ?: "com.mapconductor"
val libraryArtifactId = "core"
val libraryVersion = project.findProperty("libraryVersion") as String? ?: "1.0.0"

// Set project version for NMCP plugin
version = libraryVersion
val libraryName = "MapConductor Core"
val libraryDescription = "Core abstractions and shared functionality for MapConductor unified mapping library"

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    // Since Android libraries don't have javadoc task by default, create empty jar
}

publishing {
    publications {
        create<MavenPublication>("release") {
            project.afterEvaluate {
                from(components["release"])
            }

            groupId = libraryGroupId
            artifactId = libraryArtifactId
            version = libraryVersion

            artifact(javadocJar.get())

            pom {
                name.set(libraryName)
                description.set(libraryDescription)
                url.set(
                    project.findProperty("libraryUrl") as String?
                        ?: "https://github.com/mapconductor/android-sdk",
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
                    connection.set("scm:git:git://github.com/mapconductor/android-sdk.git")
                    developerConnection
                        .set("scm:git:ssh://github.com:mapconductor/android-sdk.git")
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
            setUrl("https://maven.pkg.github.com/MapConductor/android-sdk/")
            credentials {
                username =
                    project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                        ?: System.getenv("GITHUB_ACTOR")
                password =
                    project.findProperty("gpr.key") as String? ?: System.getenv("GPR_TOKEN")
                        ?: System.getenv("GITHUB_TOKEN")
            }
        }

        // Central Portal publishing is handled by nmcp plugin, no manual repository needed
    }
}

signing {
    val signingKey = findProperty("signingKey") as String?
    val signingPassword = findProperty("signingPassword") as String?
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["release"])
    }
}

// Central Portal configuration is handled in root build.gradle.kts
