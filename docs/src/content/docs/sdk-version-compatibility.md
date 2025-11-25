---
title: SDK Version Compatibility
---

# SDK Version Compatibility

This page explains the version compatibility between MapConductor, each map SDK, and the Android/Kotlin/Compose environment.

## Supported Version Overview

MapConductor v1.1.1 is designed for the following environment:

- **Android**: Minimum SDK version 26, Target SDK 35
- **Kotlin**: 1.9.25
- **Jetpack Compose**: 1.7.1
- **Java**: 17

Each module is tested with these versions as prerequisites. If you use different versions, please pay attention to compatibility.

## Example Gradle Configuration

```kotlin
android {
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
```

## MapConductor and Map SDKs

MapConductor is built on top of each map SDK and is designed to work with the following combinations (refer to the official documentation of each map SDK for actual minimum and recommended versions):

- Google Maps Android SDK (latest stable version)
- Mapbox Maps SDK for Android
- HERE SDK for Android
- ArcGIS Maps SDK for Kotlin / Android
- MapLibre Native / MapLibre GL for Android

MapConductor development verifies version combinations to maintain compatibility at both compile time and runtime.

## Dependency Update Policy

- Critical security updates or incompatible changes will be addressed in minor/major releases
- API changes that require significant modifications will be documented in release notes

For the latest information, check the release notes on GitHub and the version history on Maven Central.
