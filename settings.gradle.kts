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
        flatDir {
            dirs(rootDir.resolve("libs")) // ← プロジェクトルートの libs/
        }
    }
}

rootProject.name = "MapConductorSDK"
include(
    ":example-app",
    ":mapconductor-for-here",
    ":mapconductor-for-mapbox",
    ":mapconductor-for-googlemaps",
    ":mapconductor-for-arcgis",
    ":mapconductor-core",
)
