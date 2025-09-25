# SDK Version Compatibility

This page shows the relationship between MapConductor versions and the underlying map SDK versions that each MapConductor module depends on.

## Version Matrix

### MapConductor 1.0.0

| MapConductor Module | Version | Underlying SDK | SDK Version | Notes |
|---------------------|---------|----------------|-------------|--------|
| mapconductor-core | 1.0.0 | - | - | Versioning dependency modules |
| core | 1.0.0 | - | - | Core abstractions and utilities |
| for-googlemaps | 1.0.0 | Google Play Services Maps | 19.2.0 | Stable, widely supported |
| for-mapbox | 1.0.0 | Mapbox Maps Android SDK | 11.14.3 | Uses NDK27 variant |
| for-here | 1.0.0 | HERE SDK Explore | 4.23.2.0.210004 | Local AAR dependency |
| for-arcgis | 1.0.0 | ArcGIS Maps SDK for Kotlin | 200.7.0 | Includes Compose toolkit |
| icons | 1.0.0 | - | - | Icon components |
| marker-strategy | 1.0.0 | - | - | Marker rendering strategies |
| marker-native-strategy | 1.0.0 | - | - | Native performance optimization |

## SDK Compatibility Notes

### Google Maps (Play Services Maps 19.2.0)
- **Stability**: Production-ready, stable API
- **Compatibility**: Android API 21+ (same as MapConductor min SDK 26)
- **Features**: Full feature support including markers, circles, polylines, polygons
- **Updates**: Regular updates through Google Play Services

### Mapbox (11.14.3)
- **Stability**: Stable, enterprise-grade mapping solution
- **Compatibility**: Android API 21+ with NDK27 support
- **Features**: Advanced styling, custom data sources, 3D capabilities
- **Notes**: Requires Mapbox access token for production use

### HERE Maps (4.23.2.0.210004)
- **Stability**: Enterprise mapping solution
- **Compatibility**: Distributed as local AAR file
- **Features**: Professional-grade mapping with routing capabilities
- **Installation**: Requires manual AAR placement in `libs/` directory

### ArcGIS Maps (200.7.0)
- **Stability**: Professional GIS mapping platform
- **Compatibility**: Includes Jetpack Compose toolkit integration
- **Features**: Advanced GIS capabilities, spatial analysis
- **Toolkit**: Uses BOM for consistent version management

## Android Framework Dependencies

### Common Dependencies (All Modules)
| Component | Version | Purpose |
|-----------|---------|---------|
| Android Gradle Plugin | 8.10.1 | Build system |
| Kotlin | 2.0.21 | Programming language |
| Jetpack Compose BOM | 2025.05.00 | UI framework version alignment |
| Compose Compiler | 1.8.1 | Compose compilation |
| AndroidX Core KTX | 1.15.0 | Android extensions |
| AndroidX Lifecycle | 2.9.0 | Lifecycle management |

### Target Platform
| Setting | Value | Description |
|---------|-------|-------------|
| Compile SDK | 35 | Android 15 |
| Target SDK | 35 | Android 15 |
| Min SDK | 26 | Android 8.0 (API level 26) |
| Java Version | 17 | JDK compatibility |

## Version Update Strategy

### MapConductor Versioning
MapConductor follows a unified versioning strategy across all modules:

1. **Major Version**: Updated when breaking API changes occur across the entire SDK
2. **Minor Version**: Updated when new features are added to core modules or significant map SDK updates
3. **Patch Version**: Updated for bug fixes and minor improvements

### Map SDK Updates
Map SDK dependencies are updated based on:

1. **Stability**: Only stable, production-ready versions are used
2. **Feature Compatibility**: Updates that maintain feature parity across providers
3. **Security**: Critical security updates are applied promptly
4. **Performance**: Updates that provide performance improvements

## Compatibility Testing

### Testing Matrix
Each MapConductor release is tested against:

- All supported map provider SDKs
- Minimum and target Android API levels
- Various device configurations (phones, tablets)
- Different Android versions (API 26-35)

### Known Issues
- HERE SDK requires manual AAR management
- Mapbox requires valid access token for all functionality
- ArcGIS requires ArcGIS Developer account for some features

## Migration Guides

### Updating Map SDK Dependencies
When updating to newer versions of underlying map SDKs:

1. **Check compatibility** with your MapConductor version
2. **Test thoroughly** on your target devices
3. **Update API keys** if required by the map provider
4. **Review breaking changes** in the map provider's changelog

### MapConductor Version Updates
When updating MapConductor versions:

1. **Review the changelog** for breaking changes
2. **Update all MapConductor modules** to the same version
3. **Test provider-specific functionality** thoroughly
4. **Update dependencies** in your app's build.gradle

## Support and Compatibility

### Supported Configurations
- **Android API**: 26-35 (Android 8.0 to Android 15)
- **Kotlin**: 2.0.21+
- **Jetpack Compose**: BOM 2025.05.00+
- **Java**: JDK 17+

### Reporting Issues
If you encounter compatibility issues:

1. Check this compatibility matrix
2. Verify your SDK versions match the supported versions
3. Check the map provider's documentation for known issues
4. Report issues with detailed version information

---

*Last updated: September 24, 2024*
*MapConductor SDK Version: 1.0.0*