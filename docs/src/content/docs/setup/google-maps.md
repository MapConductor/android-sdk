---
title: Google Maps Setup
---

This section covers the setup process for Google Maps SDK integration with MapConductor.

> **Important**: MapConductor provides a unified API layer on top of existing map SDKs. You must set up the Google Maps SDK independently before using MapConductor's Google Maps integration.

## Prerequisites

- Android development environment
- Google Cloud Console account
- Maps SDK for Android enabled in your Google Cloud project

## Setup Steps

### 1. Google Cloud Console Configuration

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Maps SDK for Android** API
4. Go to **Credentials** and create an API key
5. Restrict the API key to your app's package name and SHA-1 certificate fingerprint

### 2. Gradle Configuration

First, add the Secrets Gradle Plugin to your project's root `build.gradle.kts`:

```kotlin
// Root build.gradle.kts
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
    // Google Maps SDK (version managed via libs.versions.toml)
    implementation(libs.play.services.maps)

    // MapConductor BOM for version management (v1.1.1)
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))

    // MapConductor modules (versions managed by BOM)
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-googlemaps")
}
```

The Secrets Gradle Plugin automatically reads your `secrets.properties` file and injects the values into your `AndroidManifest.xml` at build time.

### 3. Android Manifest Configuration

Add the Google Maps API key placeholder to your `AndroidManifest.xml`:

```xml
<application>
    <!-- Google Maps API Key -->
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="${GOOGLE_MAPS_API_KEY}" />

    <!-- Add location permissions -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</application>
```

### 4. API Key Configuration

Add your actual API key to your `secrets.properties` file (create if it doesn't exist):

```properties
# secrets.properties
GOOGLE_MAPS_API_KEY=your_actual_google_maps_api_key_here
```

**Important**:

- Never commit your `secrets.properties` file to version control. Add it to `.gitignore`.
- The Secrets Gradle Plugin automatically replaces `${GOOGLE_MAPS_API_KEY}` in your `AndroidManifest.xml` with the actual value from this file.
- For CI/CD builds, you can set environment variables or use other secure methods to provide these values.

### 5. ProGuard / R8 Configuration (if applicable)

If you're using ProGuard/R8, add these rules to your `proguard-rules.pro`:

```proguard
# Google Play Services
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.**
```

## Verification

To verify your Google Maps setup:

1. Build and run your app
2. Check that the map displays without errors
3. Verify location permissions are working
4. Test basic map interactions (zoom, pan)

```kotlin
@Composable
fun TestGoogleMaps() {
    val mapState = rememberGoogleMapViewState()

    GoogleMapView(state = mapState) {
        // If this displays correctly, your setup is working
    }
}
```

## Troubleshooting

### Common Issues

**Map not displaying (gray screen)**

- Verify API key is correct in `secrets.properties`
- Check that Maps SDK for Android is enabled in Google Cloud Console
- Ensure API key restrictions match your app's package name and SHA-1

**API key errors in logs**

- Double-check the API key value
- Verify the key has not expired
- Check API key restrictions in Google Cloud Console

**Build errors**

- Ensure the Play Services Maps dependency matches your project configuration
- Verify `secrets.properties` file exists and is properly formatted
- Check that `secrets.properties` is not accidentally ignored for local builds

## Next Steps

Once Google Maps SDK is properly configured, you can use MapConductor's `GoogleMapView` component as described in the Map View component documentation.

