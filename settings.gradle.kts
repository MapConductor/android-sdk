pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven {
            // Mapbox Maven repository
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
        maven {
            // ArcGIS Maven repository
            url = uri("https://esri.jfrog.io/artifactory/arcgis")
        }
        maven {
            // TomTom Orbis Maps SDK repository
            url = uri("https://repositories.tomtom.com/artifactory/maven")
        }
        maven {
            // Longdo Map API3 SDK repository
            url = uri("https://maven.longdo.com/artifactory/libs-release-public")
        }
        maven {
            // Mappls (MapmyIndia) Maven repository
            url = uri("https://maven.mappls.com/repository/mappls/")
        }

        if (System.getenv("GPR_USER") != null && System.getenv("GPR_TOKEN") != null) {
            maven {
                name = "GithubPackages"
                url = uri("https://maven.pkg.github.com/mapconductor/android-sdk")
                credentials {
                    username = System.getenv("GPR_USER")
                    password = System.getenv("GPR_TOKEN")
                }
                content { includeGroup("com.mapconductor") }
            }
        }
        flatDir {
            dirs(rootDir.resolve("libs"))
        }
    }
}

rootProject.name = "MapConductorSDK"

val modulesProp =
    rootDir
        .resolve("projects.properties")
        .readText()
        .lines()
        .joinToString("")
        .substringAfter("modules=")
        .substringBefore("\n#")
        .substringBefore("\nmodules.")
        .replace("\\", "")
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

modulesProp.forEach { include(":$it") }


include(":android-for-tomtom:sample-app")
