# ArcGIS Maps Setup

This section covers the setup process for ArcGIS Maps SDK integration with MapConductor.

> **Important**: MapConductor provides a unified API layer on top of existing map SDKs. You must set up the ArcGIS Maps SDK independently before using MapConductor's ArcGIS integration.

## Prerequisites

- Android development environment
- ArcGIS developer account
- ArcGIS API key

## Setup Steps

### 1. ArcGIS Developer Dashboard Configuration

1. Sign up for an [ArcGIS Developer account](https://developers.arcgis.com/)
2. Go to the [ArcGIS Developer Dashboard](https://developers.arcgis.com/dashboard)
3. Create a new application or select an existing one
4. Generate an API key with appropriate scopes
5. Note the API key for use in your app

### 2. Gradle Configuration

First, add the Esri repository to your project's `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Esri public repository for ArcGIS Maps SDK
        maven {
            url = uri("https://esri.jfrog.io/artifactory/arcgis")
        }
    }
}
```

Then, add the Secrets Gradle Plugin to your project's root `build.gradle.kts`:

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
    // ArcGIS Maps SDK - Version 200.7.0 (as used by MapConductor 1.0.0)
    implementation(platform("com.esri:arcgis-maps-kotlin-toolkit-bom:200.7.0"))
    implementation("com.esri:arcgis-maps-kotlin:200.7.0")
    implementation("com.esri:arcgis-maps-kotlin-toolkit-geoview-compose")
    implementation("com.esri:arcgis-maps-kotlin-toolkit-authentication")

    // MapConductor BOM for version management
    implementation(platform("com.mapconductor:mapconductor-bom:1.0.0"))

    // MapConductor modules (versions managed by BOM)
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-arcgis")
}
```

The Secrets Gradle Plugin automatically reads your `secrets.properties` file and injects the values into your AndroidManifest.xml at build time.

### 3. Android Manifest Configuration

Add the ArcGIS API key placeholder to your `AndroidManifest.xml`:

```xml
<application>
    <!-- ArcGIS API Key -->
    <meta-data
        android:name="ARCGIS_API_KEY"
        android:value="${ARCGIS_API_KEY}" />

    <!-- Add internet and location permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</application>
```

### 4. API Key Configuration

Add your actual API key to your `secrets.properties` file:

```properties
# secrets.properties
ARCGIS_API_KEY=your_actual_arcgis_api_key_here
```

**Important**:
- Never commit your `secrets.properties` file to version control. Add it to `.gitignore`.
- The Secrets Gradle Plugin automatically replaces `${ARCGIS_API_KEY}` in your AndroidManifest.xml with the actual value from this file.
- For CI/CD builds, you can set environment variables or use other secure methods to provide these values.

### 5. License Configuration

ArcGIS SDK requires proper license configuration. Set the license in your application:

```kotlin
// In your Application class or MainActivity
ArcGISEnvironment.setApiKey("your_api_key_here")

// For development/testing
ArcGISEnvironment.setLicense("your_license_key_here") // Optional for basic use
```

For production apps, consider using a [Named User License](https://developers.arcgis.com/documentation/security-and-authentication/licensing/) or [License Key](https://developers.arcgis.com/documentation/security-and-authentication/licensing/).

## Verification

To verify your ArcGIS setup:

1. Build and run your app
2. Check that the ArcGIS map displays correctly
3. Test GIS-specific features (if using)
4. Verify authentication is working

```kotlin
@Composable
fun TestArcGIS() {
    val mapState = rememberArcGISMapViewState()

    ArcGISMapView(state = mapState) {
        // If this displays correctly, your setup is working
    }
}
```

## Troubleshooting

### Common Issues

**Map not loading**
- Verify API key is correct in `secrets.properties`
- Check that API key has proper scopes in ArcGIS Dashboard
- Ensure ArcGIS Developer account is in good standing

**License errors**
- Set API key using `ArcGISEnvironment.setApiKey()`
- For production apps, configure proper licensing
- Check ArcGIS license terms for your use case

**Build errors**
- Ensure ArcGIS SDK version matches: `200.7.0`
- Verify BOM is used for version management
- Check that Compose toolkit dependencies are included
- Ensure `secrets.properties` contains the API key

## Next Steps

Once ArcGIS Maps SDK is properly configured, you can use MapConductor's `ArcGISMapView` component as described in the [MapViewComponent](../components/mapviewcomponent.md) documentation.