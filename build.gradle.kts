// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0-rc.1"
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

tasks.register("allLintChecks") {
    group = "verification"
    description = "Run ktLintCheck and lint for all modules"
    dependsOn(
        ":ktlintFormat",
        ":mapconductor-core:lint",
        ":mapconductor-for-arcgis:lint",
        ":mapconductor-for-here:lint",
        ":mapconductor-for-googlemaps:lint",
        ":mapconductor-for-mapbox:lint",
    )
}
