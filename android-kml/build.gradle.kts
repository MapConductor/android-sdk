plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("maven-publish")
    id("signing")
    id("com.gradleup.nmcp") version "1.5.0"
}

ktlint {
    android.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

android {
    namespace = "com.mapconductor.kml"
    compileSdk = project.property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = project.property("minSdk").toString().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        aarMetadata {
            minCompileSdk = project.property("compileSdk").toString().toInt()
        }
    }

    buildFeatures {
        compose = true
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    if (findProject(":android-sdk-compose") != null) {
        implementation(project(":android-sdk-compose"))
    } else {
        implementation("com.mapconductor:compose:${project.findProperty("coreLibraryVersion") as String? ?: "1.0.0"}")
    }

    implementation(libs.kotlinx.coroutines.android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)

    testImplementation(libs.junit)
    // JVM 単体テストで XmlPullParserFactory の実装を提供する（Android 実機では OS 内蔵の kxml2 が使われる）
    testImplementation("net.sf.kxml:kxml2:2.3.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Publishing configuration
val libraryGroupId = project.findProperty("libraryGroupId") as String? ?: "com.mapconductor"
val libraryArtifactId = "kml"
val libraryVersion = project.findProperty("libraryVersion") as String? ?: "1.0.0"
val coreLibraryVersion = project.findProperty("coreLibraryVersion") as String? ?: "1.0.0"

version = libraryVersion
val libraryName = "MapConductor KML Layer"
val libraryDescription = "KML tile-rendered data layer for MapConductor"

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
                            ?: "https://github.com/MapConductor/android-kml",
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
                            email.set(project.findProperty("developerEmail") as String? ?: "info@mkgeeklab.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/MapConductor/android-kml.git")
                        developerConnection
                            .set("scm:git:ssh://github.com:MapConductor/android-kml.git")
                        url.set(
                            project.findProperty("scmUrl") as String?
                                ?: "https://github.com/MapConductor/android-kml.git",
                        )
                    }
                }
            }
        }

        repositories {
            maven {
                name = "GitHubPackages"
                setUrl("https://maven.pkg.github.com/MapConductor/android-kml")
                credentials {
                    username =
                        project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                            ?: System.getenv("GITHUB_ACTOR")
                    password =
                        project.findProperty("gpr.key") as String? ?: System.getenv("GPR_TOKEN")
                            ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }

    signing {
        val signingKey = findProperty("signingKey") as String?
        val signingPassword = findProperty("signingPassword") as String?
        if (!signingKey.isNullOrEmpty() && !signingPassword.isNullOrEmpty()) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications["release"])
        }
    }

    if (project == rootProject) {
        nmcp {
            publishAllPublicationsToCentralPortal {
                username.set(findProperty("ossrh_username") as String? ?: System.getenv("OSSRH_USERNAME") ?: "")
                password.set(findProperty("ossrh_password") as String? ?: System.getenv("OSSRH_PASSWORD") ?: "")
            }
        }
    }
}
