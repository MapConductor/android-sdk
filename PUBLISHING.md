# MapConductor Publishing Guide

This document explains how to publish MapConductor modules to various Maven repositories.

## Available Modules

The following modules are configured for publishing:

- `mapconductor-core` - Core abstractions and shared functionality
- `mapconductor-for-arcgis` - ArcGIS Maps implementation
- `mapconductor-for-googlemaps` - Google Maps implementation
- `mapconductor-for-here` - HERE Maps implementation
- `mapconductor-for-mapbox` - Mapbox implementation
- `mapconductor-icons` - Reusable marker icon components
- `mapconductor-marker-native-strategy` - High-performance C++ marker strategies
- `mapconductor-marker-strategy` - Advanced marker rendering strategies

## Publishing Destinations

### 1. Local Maven Repository (Testing)

For local testing and development:

```bash
# Publish all modules to local repository
./gradlew publishAllLocal

# Publish individual module
./gradlew :mapconductor-core:publishToMavenLocal
```

Published artifacts will be available in `~/.m2/repository/com/mapconductor/`

### 2. GitHub Packages

For publishing to GitHub Packages repository:

#### Setup GitHub Credentials

Add to `local.properties` or set environment variables:

```properties
# local.properties
gpr.user=your-github-username
gpr.key=your-github-personal-access-token
```

Or set environment variables:
```bash
export GITHUB_ACTOR=your-github-username
export GITHUB_TOKEN=your-github-personal-access-token
```

#### Publish to GitHub

```bash
# Publish all modules to GitHub Packages
./gradlew publishAllToGitHub

# Publish individual module
./gradlew :mapconductor-core:publishReleasePublicationToGitHubPackagesRepository
```

### 3. Maven Central (Sonatype OSSRH)

For publishing to Maven Central:

#### Setup Sonatype Credentials

Add to `local.properties` or set environment variables:

```properties
# local.properties
signing.keyId=your-gpg-key-id
signing.password=your-gpg-key-password
signing.secretKeyRingFile=/path/to/secring.gpg
ossrh.username=your-sonatype-username
ossrh.password=your-sonatype-password
```

Or set environment variables:
```bash
export OSSRH_USERNAME=your-sonatype-username
export OSSRH_PASSWORD=your-sonatype-password
```

#### Publish to Maven Central

```bash
# Publish all modules to Maven Central
./gradlew publishAllToMavenCentral

# Publish individual module
./gradlew :mapconductor-core:publishReleasePublicationToOSSRHRepository
```

## Configuration

### Version Management

Update version in `gradle.properties`:

```properties
libraryVersion=1.0.1
versionName=1.0.1
```

### Library Metadata

Configure in `gradle.properties`:

```properties
libraryGroupId=com.mapconductor
libraryUrl=https://github.com/mapconductor/android-sdk
developerId=mapconductor
developerName=MapConductor Team
developerEmail=dev@mapconductor.com
scmUrl=https://github.com/mapconductor/android-sdk.git
```

### Module-Specific Configuration

Each module has its own artifact ID and description in its `build.gradle.kts`:

```kotlin
ext {
    libraryArtifactId = "mapconductor-core"
    libraryName = "MapConductor Core"
    libraryDescription = "Core abstractions and shared functionality for MapConductor unified mapping library"
}
```

## Generated Artifacts

Each module publishes the following artifacts:

- `{artifact-id}-{version}.aar` - Main library
- `{artifact-id}-{version}-sources.jar` - Source code
- `{artifact-id}-{version}-javadoc.jar` - Documentation
- `{artifact-id}-{version}.pom` - Maven metadata

## Usage in Other Projects

### With GitHub Packages

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/your-organization/mapconductor-android-sdk")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.mapconductor:mapconductor-core:1.0.0")
    implementation("com.mapconductor:mapconductor-for-googlemaps:1.0.0")
    // Add other modules as needed
}
```

### With Maven Central

```kotlin
dependencies {
    implementation("com.mapconductor:mapconductor-core:1.0.0")
    implementation("com.mapconductor:mapconductor-for-googlemaps:1.0.0")
    // Add other modules as needed
}
```

## Troubleshooting

### Common Issues

1. **Authentication Failed**: Verify credentials in `local.properties` or environment variables
2. **Signing Failed**: Ensure GPG key is properly configured for Maven Central
3. **Build Failed**: Run `./gradlew allLintChecks` to fix code style issues first

### Verifying Artifacts

Check generated artifacts before publishing:

```bash
# Build all modules
./gradlew build

# Check artifacts in build/outputs/aar/
ls mapconductor-core/build/outputs/aar/
```

### Clean Build

If you encounter issues, try a clean build:

```bash
./gradlew clean
./gradlew build
./gradlew publishAllLocal
```

## Release Checklist

Before publishing a release:

1. [ ] Update version in `gradle.properties`
2. [ ] Run `./gradlew allLintChecks` and fix any issues
3. [ ] Run `./gradlew build` and ensure all tests pass
4. [ ] Test locally with `./gradlew publishAllLocal`
5. [ ] Update `CHANGELOG.md` with release notes
6. [ ] Create git tag: `git tag v1.0.0`
7. [ ] Publish to target repository
8. [ ] Push tag: `git push origin v1.0.0`
