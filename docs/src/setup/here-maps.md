# HERE Maps Setup

This section covers the setup process for HERE SDK integration with MapConductor.

> **Important**: MapConductor provides a unified API layer on top of existing map SDKs. You must set up the HERE SDK independently before using MapConductor's HERE Maps integration.

## Prerequisites

- Android development environment
- HERE developer account
- HERE API key

## Setup Steps

### 1. HERE Developer Portal Configuration

1. Sign up for a [HERE Developer account](https://developer.here.com/)
2. Go to the [HERE Developer Portal](https://developer.here.com/projects)
3. Create a new project
4. Generate API credentials (Access Key ID and Secret)
5. Download the HERE SDK AAR file

### 2. HERE SDK Installation

1. Download HERE SDK Explore Android version **4.23.2.0.210004** (as used by MapConductor 1.0.0)
2. Place the AAR file in your project's `libs/` directory:
   ```
   libs/heresdk-explore-android-4.23.2.0.210004.aar
   ```
3. The AAR file must match the exact version specified in `gradle.properties`

### 3. Gradle Configuration

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
    // HERE SDK - Version 4.23.2.0.210004 (as used by MapConductor 1.0.0)
    implementation(files("${rootProject.projectDir}/libs/heresdk-explore-android-4.23.2.0.210004.aar"))

    // MapConductor BOM for version management
    implementation(platform("com.mapconductor:mapconductor-bom:1.0.0"))

    // MapConductor modules (versions managed by BOM)
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-here")
}
```

The Secrets Gradle Plugin automatically reads your `secrets.properties` file and injects the values into your AndroidManifest.xml at build time.

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

### 5. API Key Configuration

Add your actual API credentials to your `secrets.properties` file:

```properties
# secrets.properties
HERE_ACCESS_KEY_ID=your_actual_here_access_key_id
HERE_ACCESS_KEY_SECRET=your_actual_here_access_key_secret
```

**Important**:
- Never commit your `secrets.properties` file to version control. Add it to `.gitignore`.
- The Secrets Gradle Plugin automatically replaces `${HERE_ACCESS_KEY_ID}` and `${HERE_ACCESS_KEY_SECRET}` in your AndroidManifest.xml with the actual values from this file.
- The HERE SDK AAR file is not publicly available and must be obtained from HERE Developer Portal.
- For CI/CD builds, you can set environment variables or use other secure methods to provide these values.

## Verification

To verify your HERE Maps setup:

1. Build and run your app
2. Check that the HERE map displays correctly
3. Test map interactions and HERE-specific features
4. Verify offline capabilities (if using)

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
- Check that Access Key ID and Secret are properly configured
- Ensure HERE Developer account is active

**AAR file not found**
- Verify AAR file is in `libs/` directory
- Check filename matches exactly: `heresdk-explore-android-4.23.2.0.210004.aar`
- Ensure AAR file was downloaded from HERE Developer Portal

**Build errors**
- Verify AAR file version matches gradle configuration
- Check that HERE credentials are set in `secrets.properties`
- Ensure `gradle.properties` contains correct `hereSdkAarName` value

## Next Steps

Once HERE SDK is properly configured, you can use MapConductor's `HereMapView` component as described in the [MapViewComponent](../components/mapviewcomponent.md) documentation.