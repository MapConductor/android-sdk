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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
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
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/mapconductor/android-sdk")
            credentials {
                username = System.getenv("GPR_USER")
                password = System.getenv("GPR_TOKEN")
            }
            content { includeGroup("com.mapconductor") }
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
        .readLines()
        .firstOrNull { it.startsWith("modules=") }
        ?.removePrefix("modules=")
        ?.split(",")
        ?.map { it.trim() }
        ?: emptyList()

modulesProp.forEach { include(":$it") }
