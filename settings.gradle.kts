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
        // Mapbox Maven repository
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
        flatDir {
            dirs(rootDir.resolve("libs")) // ← プロジェクトルートの libs/
        }
    }
}

rootProject.name = "MapConductorSDK"
include(":example-app")
include(":mapconductor-for-here")
include(":mapconductor-for-mapbox")
include(":mapconductor-for-googlemaps")
include(":mapconductor-core")
