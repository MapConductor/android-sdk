// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.firebase.appdistribution) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.jlleitschuh.ktlint) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.secrets.gradle.plugin)
    }
}

val modules: List<String> =
    rootDir
        .resolve("projects.properties")
        .readLines()
        .firstOrNull { it.startsWith("modules=") }
        ?.removePrefix("modules=")
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

tasks.register("allLintChecks") {
    group = "verification"
    description = "Run ktlintFormat and lint for all modules"

    val lintTasks =
        modules.map { module ->
            listOf(":$module:ktlintFormat", ":$module:lint")
        }

    dependsOn(lintTasks)
}
