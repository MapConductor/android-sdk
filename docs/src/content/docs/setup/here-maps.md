---
title: HERE Maps Setup
---

# HERE Maps Setup

This section covers the setup process for HERE SDK integration with MapConductor.

> **Important**: MapConductor provides a unified API layer on top of existing map SDKs. You must set up the HERE SDK independently before using MapConductor's HERE Maps integration.

## Prerequisites

- Android development environment
- HERE developer account
- HERE API credentials

## Setup Steps

### 1. HERE Developer Portal Configuration

1. Sign up for a [HERE Developer account](https://developer.here.com/)
2. Go to the [HERE Developer Portal](https://developer.here.com/projects)
3. Create a new project
4. Generate API credentials (Access Key ID and Secret)
5. Download the HERE SDK AAR file

### 2. HERE SDK Installation

1. Download HERE SDK Explore Android (e.g. `4.23.2.0.210004`, as used in this repository)
2. Place the AAR file in your project's `libs/` directory:

   ```text
   libs/heresdk-explore-android-4.23.2.0.210004.aar
   ```

3. Ensure the AAR filename matches what you reference in Gradle.

### 3. Gradle Configuration

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
    // HERE SDK (local AAR)
    implementation(files("${rootProject.projectDir}/libs/heresdk-explore-android-4.23.2.0.210004.aar"))

    // MapConductor BOM for version management (v1.1.1)
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))

    // MapConductor modules (versions managed by BOM)
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-here")
}
```

The Secrets Gradle Plugin automatically reads your `secrets.properties` file and can be used to inject values into your `AndroidManifest.xml`.

### 4. Android Manifest Configuration

Add the HERE API key placeholders to your `AndroidManifest.xml`:

```xml
<application>
    <!-- HERE API Credentials -->
    <meta-data
        android:name="HERE_ACCESS_KEY_ID"
        android:value="${HERE_ACCESS_KEY_ID}" />
    <meta-data
        android:name="HERE_ACCESS_KEY_SECRET"
        android:value="${HERE_ACCESS_KEY_SECRET}" />

    <!-- Add internet and location permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</application>
```

### 5. API Credentials Configuration

Add your actual API credentials to your `secrets.properties` file:

```properties
# secrets.properties
HERE_ACCESS_KEY_ID=your_actual_here_access_key_id
HERE_ACCESS_KEY_SECRET=your_actual_here_access_key_secret
```

**Important**:

- Never commit your `secrets.properties` file or HERE SDK AAR to public version control.
- The Secrets Gradle Plugin automatically replaces `${HERE_ACCESS_KEY_ID}` and `${HERE_ACCESS_KEY_SECRET}` in your `AndroidManifest.xml` with the actual values from this file.
- For CI/CD builds, you can set environment variables or use other secure methods to provide these values.

## Verification

To verify your HERE Maps setup:

1. Build and run your app
2. Check that the HERE map displays correctly
3. Test map interactions and HERE-specific features

```kotlin
@Composable
fun TestHERE() {
    val mapState = rememberHereMapViewState()

    HereMapView(state = mapState) {
        // If this displays correctly, your setup is working
    }
}
```

## Troubleshooting

### Common Issues

**Map not loading**

- Verify HERE credentials are correct in `secrets.properties`
- Check that Access Key ID and Secret are properly configured and active

**AAR file not found**

- Verify the AAR file is in the `libs/` directory
- Check the filename matches exactly what is referenced in Gradle

**Build errors**

- Ensure the HERE SDK version matches your Gradle configuration
- Check that `secrets.properties` contains the required keys

## Next Steps

Once HERE SDK is properly configured, you can use MapConductor's `HereMapView` component with the unified API.

