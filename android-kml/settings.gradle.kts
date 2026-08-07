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
        mavenLocal()
        google()
        mavenCentral()
        if (!System.getenv("GPR_USER").isNullOrEmpty() && !System.getenv("GPR_TOKEN").isNullOrEmpty()) {
            maven {
                name = "GithubPackages"
                url = uri("https://maven.pkg.github.com/mapconductor/android-kml")
                credentials {
                    username = System.getenv("GPR_USER") ?: ""
                    password = System.getenv("GPR_TOKEN") ?: ""
                }
            }
        }
        if (!System.getenv("GPR_USER").isNullOrEmpty() || !System.getenv("GITHUB_ACTOR").isNullOrEmpty()) {
            maven {
                name = "GithubPackages-core"
                url = uri("https://maven.pkg.github.com/MapConductor/android-sdk-core")
                credentials {
                    username = System.getenv("GPR_USER") ?: System.getenv("GITHUB_ACTOR") ?: ""
                    password = System.getenv("GPR_TOKEN") ?: System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
        }
    }
}

rootProject.name = "android-kml"
