// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.util.Properties
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jlleitschuh.ktlint) apply false
    id("com.gradleup.nmcp") version "0.0.8"
}

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
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
    "android-core",
    "android-for-arcgis",
    "android-for-googlemaps",
    "android-for-here",
    "android-for-mapbox",
    "android-icons"
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

// Central Portal configuration
nmcp {
    publishAllProjectsProbablyBreakingProjectIsolation {
        username = findProperty("ossrh.username") as String? ?: System.getenv("OSSRH_USERNAME")
        password = findProperty("ossrh.password") as String? ?: System.getenv("OSSRH_PASSWORD")
        // All publications from all subprojects will be published
    }
}

