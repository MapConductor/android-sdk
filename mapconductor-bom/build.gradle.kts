plugins {
    id("java-platform")
    id("maven-publish")
    id("signing")
    id("com.gradleup.nmcp") version "1.5.0"
}

// Publishing configuration
val libraryGroupId = project.findProperty("libraryGroupId") as String? ?: "com.mapconductor"
val libraryArtifactId = "mapconductor-bom"
val libraryVersion = project.findProperty("libraryVersion") as String? ?: "1.0.0"

// Set project version for NMCP plugin
version = libraryVersion
val libraryName = "MapConductor BOM"
val libraryDescription = "Bill of Materials for MapConductor unified mapping library"

// Module information with their project paths, artifact IDs, and versions
val moduleInfo = mapOf(
    "core" to getModuleVersion(":android-sdk-core"),
    "for-arcgis" to getModuleVersion(":android-for-arcgis"),
    "for-googlemaps" to getModuleVersion(":android-for-googlemaps"),
    "for-here" to getModuleVersion(":android-for-here"),
    "for-mapbox" to getModuleVersion(":android-for-mapbox"),
    "for-maplibre" to getModuleVersion(":android-for-maplibre"),
    "icons" to getModuleVersion(":android-icons"),
    "heatmap" to getModuleVersion(":android-heatmap"),
    "geojson-layer" to getModuleVersion(":android-geojson-layer"),
    "marker-clustering" to getModuleVersion(":android-marker-clustering"),
)

// Function to get version from a module's gradle.properties or fallback to root version
fun getModuleVersion(modulePath: String): String {
    val moduleDir = file("../${modulePath.removePrefix(":")}")
    val moduleGradleProperties = File(moduleDir, "gradle.properties")

    return if (moduleGradleProperties.exists()) {
        val props = java.util.Properties()
        moduleGradleProperties.inputStream().use { props.load(it) }
        props.getProperty("libraryVersion") ?: libraryVersion
    } else {
        libraryVersion
    }
}

dependencies {
    constraints {
        moduleInfo.forEach { (artifactId, version) ->
            api("com.mapconductor:$artifactId:$version")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["javaPlatform"])

            groupId = libraryGroupId
            artifactId = libraryArtifactId
            version = libraryVersion

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

signing {
    val signingKey = findProperty("signingKey") as String?
    val signingPassword = findProperty("signingPassword") as String?
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["release"])
    }
}
