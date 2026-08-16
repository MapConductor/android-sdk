// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.util.Properties
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jlleitschuh.ktlint) apply false
    id("com.gradleup.nmcp") version "1.5.0" apply false
}

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven {
            // Mappls services plugin（example-app が .a.conf / .a.olf を読み込むのに使う）
            url = uri("https://maven.mappls.com/repository/mappls/")
        }
    }
    dependencies {
        classpath("com.mappls.services:mappls-services:1.0.1")
    }
}

val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    val localProperties = Properties().apply {
        localPropertiesFile.inputStream().use { load(it) }
    }

    localProperties.forEach { key, value ->
        val keyString = key as String
        if (!extra.has(keyString)) {
            extra[keyString] = value
        }
    }

    subprojects {
        localProperties.forEach { key, value ->
            val keyString = key as String
            if (!extra.has(keyString)) {
                extra[keyString] = value
            }
        }
    }
}

val modules: List<String> =
    rootDir
        .resolve("projects.properties")
        .readLines()
        .dropWhile { !it.startsWith("modules=") }
        .takeWhile { it.startsWith("modules=") || it.trim().endsWith(",\\") || it.trim().matches(Regex("^[a-zA-Z0-9-]+$")) }
        .joinToString("")
        .removePrefix("modules=")
        .replace("\\", "")
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

// 公開 API サーフェスのスナップショット（apiDump / apiCheck）。
apply(from = "gradle/api-surface.gradle.kts")

tasks.register("allLintChecks") {
    group = "verification"
    description = "Run ktlintFormat and lint for all modules"

    val lintTasks =
        modules
            .filter { it != "mapconductor-bom" }
            .flatMap { module ->
                listOf(":$module:ktlintFormat", ":$module:lint")
            }

    dependsOn(lintTasks)
}

// Publishing tasks for all modules
val publishableModules = listOf(
    "mapconductor-bom",
    "android-sdk-core",
    "android-sdk-compose",
    "android-icons",
    "android-marker-clustering",
    "android-heatmap",
    "android-geojson-layer",
    "android-for-arcgis",
    "android-for-googlemaps",
    "android-for-here",
    "android-for-mapbox",
    "android-for-maplibre",
    "android-for-tomtom"
)

tasks.register("publishAllLocal") {
    group = "publishing"
    description = "Publish all MapConductor modules to local repository"

    val publishTasks = publishableModules.map { module ->
        ":$module:publishToMavenLocal"
    }

    dependsOn(publishTasks)
}

tasks.register("publishAllToGitHub") {
    group = "publishing"
    description = "Publish all MapConductor modules to GitHub Packages"

    val publishTasks = publishableModules.map { module ->
        ":$module:publishReleasePublicationToGitHubPackagesRepository"
    }

    dependsOn(publishTasks)
}

tasks.register("publishAllToMavenCentral") {
    group = "publishing"
    description = "Publish all MapConductor modules to Maven Central via Central Portal"

    val publishTasks = publishableModules.map { module ->
        ":$module:publishAllPublicationsToNmcpReleaseRepository"
    }

    dependsOn(publishTasks)
}

