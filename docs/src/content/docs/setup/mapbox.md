---
title: Mapbox Setup
---

This section covers the setup process for Mapbox Maps SDK integration with MapConductor.

> **Important**: MapConductor provides a unified API layer on top of existing map SDKs. You must set up the Mapbox Maps SDK independently before using MapConductor's Mapbox integration.

## Prerequisites

- Android development environment
- Mapbox account
- Mapbox access token

## Setup Steps

### 1. Mapbox Account Configuration

1. Sign up for a [Mapbox account](https://www.mapbox.com/)
2. Go to your [Mapbox Account Dashboard](https://account.mapbox.com/)
3. Navigate to **Access tokens**
4. Create a new access token or use the default public token
5. Copy the access token for use in your app

### 2. Gradle Configuration

Ensure the Mapbox Maven repository is configured (this repository already has it in `settings.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Mapbox Maven repository
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
    }
}
```

Add the Secrets Gradle Plugin to your project's root `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}
```

Then, add the dependencies and apply the plugin in your app's `build.gradle.kts`:

```kotlin
// App build.gradle.kts
plugins {
    // ... other plugins
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

dependencies {
    // Mapbox Maps SDK (NDK27 variant, version managed via libs.versions.toml)
    implementation(libs.mapbox.android)

    // MapConductor BOM for version management (v1.1.1)
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))

    // MapConductor modules (versions managed by BOM)
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-mapbox")
}
```

The Secrets Gradle Plugin automatically reads your `secrets.properties` file and injects the values into your `AndroidManifest.xml` at build time.

### 3. Android Manifest Configuration

Add the Mapbox access token placeholder to your `AndroidManifest.xml`:

```xml
<application>
    <!-- Mapbox Access Token -->
    <meta-data
        android:name="MAPBOX_ACCESS_TOKEN"
        android:value="${MAPBOX_ACCESS_TOKEN}" />

    <!-- Add internet and location permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</application>
```

### 4. Access Token Configuration

Add your actual access token to your `secrets.properties` file:

```properties
# secrets.properties
MAPBOX_ACCESS_TOKEN=your_actual_mapbox_access_token_here
```

**Important**:

- Never commit your `secrets.properties` file to version control. Add it to `.gitignore`.
- The Secrets Gradle Plugin automatically replaces `${MAPBOX_ACCESS_TOKEN}` in your `AndroidManifest.xml` with the actual value from this file.
- For CI/CD builds, you can set environment variables or use other secure methods to provide these values.

### 5. Style Configuration

Mapbox allows custom map styles. You can use built-in styles or create custom ones:

```kotlin
// Using built-in styles
val mapStyle = Style.MAPBOX_STREETS
// or Style.SATELLITE, Style.OUTDOORS, etc.

// Using custom style URL
val customStyle = "mapbox://styles/your-username/your-style-id"
```

## Verification

To verify your Mapbox setup:

1. Build and run your app
2. Check that the map displays with Mapbox styling
3. Test map interactions (zoom, pan, rotate)
4. Verify custom styles load correctly (if using)

```kotlin
@Composable
fun TestMapbox() {
    val mapState = rememberMapboxMapViewState()

    MapboxMapView(state = mapState) {
        // If this displays correctly, your setup is working
    }
}
```

## Troubleshooting

### Common Issues

**Map not displaying (blank screen)**

- Verify access token is correct in `secrets.properties`
- Check that the token has not expired
- Ensure internet connectivity for tile loading

**Access token errors**

- Double-check the token value (typically starts with `pk.`)
- Verify token permissions in Mapbox account dashboard
- Check token scopes include required permissions

**Build errors**

- Ensure the Mapbox SDK coordinates match your project configuration
- Verify the NDK27 variant is used
- Check that `secrets.properties` file exists and contains the token

## Next Steps

Once Mapbox Maps SDK is properly configured, you can use MapConductor's `MapboxMapView` component with the unified API.

