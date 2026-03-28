Of course. Here is a high-quality SDK document for the provided code snippet, meticulously crafted to be clear, complete, and developer-friendly, while addressing all previous feedback.

---

# ZoomAltitudeConverter

## Class: `ZoomAltitudeConverter`

### Signature
```kotlin
class ZoomAltitudeConverter(
    zoom0Altitude: Double = ARCGIS_OPTIMIZED_ZOOM0_ALTITUDE
) : AbstractZoomAltitudeConverter(zoom0Altitude)
```

### Description
The `ZoomAltitudeConverter` class provides a sophisticated mechanism for converting between abstract "zoom levels" (as used in map APIs like Google Maps) and camera "altitude" in meters (as used in ArcGIS `SceneView`).

This converter is specifically calibrated for ArcGIS and incorporates several factors to ensure a visually consistent and accurate mapping experience:

-   **Latitude Correction:** Adjusts for the visual distortion caused by the Mercator projection, where geographic areas appear larger further from the equator.
-   **Tilt Correction:** Accounts for the camera's tilt angle, converting the straight-line "distance" to the target into vertical "altitude".
-   **Viewport Scaling:** Dynamically scales the altitude based on the map view's height. This mimics the behavior of 2D tiled maps (like Google Maps), where a larger screen displays a larger geographic area at the same zoom level.

The class offers a range of methods, from comprehensive conversions that use all available context (latitude, tilt, viewport size) to simpler legacy methods for basic use cases or backward compatibility.

---

## Constructor

### Signature
```kotlin
ZoomAltitudeConverter(zoom0Altitude: Double = ARCGIS_OPTIMIZED_ZOOM0_ALTITUDE)
```

### Description
Initializes a new instance of the `ZoomAltitudeConverter`.

### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoom0Altitude` | `Double` | **Optional.** The camera altitude in meters that corresponds to zoom level 0.0. Defaults to `ARCGIS_OPTIMIZED_ZOOM0_ALTITUDE`. |

---

## Companion Object

### `ARCGIS_OPTIMIZED_ZOOM0_ALTITUDE`

#### Signature
```kotlin
const val ARCGIS_OPTIMIZED_ZOOM0_ALTITUDE = 124000000.0
```

#### Description
A calibrated constant representing the default altitude in meters for zoom level 0. This value has been empirically determined to provide an optimal starting point for conversions in an ArcGIS environment.

---

## Core Conversion Methods

These methods provide the most accurate conversions by incorporating latitude, tilt, and viewport dimensions. It is highly recommended to use these methods whenever the context is available.

### `zoomLevelToAltitude`

#### Signature
```kotlin
fun zoomLevelToAltitude(
    zoomLevel: Double,
    latitude: Double,
    tilt: Double,
    viewportWidthPx: Int,
    viewportHeightPx: Int
): Double
```

#### Description
Converts a map zoom level to the corresponding camera altitude in meters, applying corrections for latitude, camera tilt, and viewport dimensions. This is the most accurate method for conversion.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The abstract map zoom level to convert. |
| `latitude` | `Double` | The current latitude in degrees of the camera's target. |
| `tilt` | `Double` | The current camera tilt angle in degrees (0 is looking straight down). |
| `viewportWidthPx` | `Int` | The width of the map view in pixels. |
| `viewportHeightPx` | `Int` | The height of the map view in pixels. |

#### Returns
`Double` - The calculated camera altitude in meters, clamped within a valid range.

#### Example
```kotlin
val converter = ZoomAltitudeConverter()
val zoomLevel = 15.5
val latitude = 34.0522 // Los Angeles
val tilt = 45.0
val viewportWidth = 1080
val viewportHeight = 1920

val altitude = converter.zoomLevelToAltitude(
    zoomLevel, 
    latitude, 
    tilt, 
    viewportWidth, 
    viewportHeight
)
// e.g., altitude might be ~2450.0 meters
```

### `altitudeToZoomLevel`

#### Signature
```kotlin
fun altitudeToZoomLevel(
    altitude: Double,
    latitude: Double,
    tilt: Double,
    viewportWidthPx: Int,
    viewportHeightPx: Int
): Double
```

#### Description
Converts a camera altitude in meters to the corresponding map zoom level, applying corrections for latitude, camera tilt, and viewport dimensions. This is the most accurate method for reverse conversion.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `altitude` | `Double` | The camera altitude in meters to convert. |
| `latitude` | `Double` | The current latitude in degrees of the camera's target. |
| `tilt` | `Double` | The current camera tilt angle in degrees. |
| `viewportWidthPx` | `Int` | The width of the map view in pixels. |
| `viewportHeightPx` | `Int` | The height of the map view in pixels. |

#### Returns
`Double` - The calculated map zoom level, clamped within a valid range.

#### Example
```kotlin
val converter = ZoomAltitudeConverter()
val altitude = 2450.0
val latitude = 34.0522 // Los Angeles
val tilt = 45.0
val viewportWidth = 1080
val viewportHeight = 1920

val zoomLevel = converter.altitudeToZoomLevel(
    altitude, 
    latitude, 
    tilt, 
    viewportWidth, 
    viewportHeight
)
// e.g., zoomLevel might be ~15.5
```

---

## Simplified Conversion Methods (Overrides)

These methods are overrides from the `AbstractZoomAltitudeConverter` and provide conversions without viewport scaling. They are useful when viewport dimensions are not available.

### `zoomLevelToAltitude`

#### Signature
```kotlin
override fun zoomLevelToAltitude(
    zoomLevel: Double,
    latitude: Double,
    tilt: Double
): Double
```

#### Description
Converts a map zoom level to camera altitude, applying corrections for latitude and tilt. This version does not use viewport scaling.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The abstract map zoom level to convert. |
| `latitude` | `Double` | The current latitude in degrees of the camera's target. |
| `tilt` | `Double` | The current camera tilt angle in degrees. |

#### Returns
`Double` - The calculated camera altitude in meters.

#### Example
```kotlin
val converter = ZoomAltitudeConverter()
val zoomLevel = 12.0
val latitude = 48.8566 // Paris
val tilt = 30.0

val altitude = converter.zoomLevelToAltitude(zoomLevel, latitude, tilt)
// e.g., altitude might be ~14500.0 meters
```

### `altitudeToZoomLevel`

#### Signature
```kotlin
override fun altitudeToZoomLevel(
    altitude: Double,
    latitude: Double,
    tilt: Double
): Double
```

#### Description
Converts a camera altitude to a map zoom level, applying corrections for latitude and tilt. This version does not use viewport scaling.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `altitude` | `Double` | The camera altitude in meters to convert. |
| `latitude` | `Double` | The current latitude in degrees of the camera's target. |
| `tilt` | `Double` | The current camera tilt angle in degrees. |

#### Returns
`Double` - The calculated map zoom level.

#### Example
```kotlin
val converter = ZoomAltitudeConverter()
val altitude = 14500.0
val latitude = 48.8566 // Paris
val tilt = 30.0

val zoomLevel = converter.altitudeToZoomLevel(altitude, latitude, tilt)
// e.g., zoomLevel might be ~12.0
```

---

## Distance Conversion Methods

These methods convert a zoom level to the straight-line "distance" from the camera to the point on the ground. This value does not account for camera tilt.

### `zoomLevelToDistance` (without viewport scaling)

#### Signature
```kotlin
fun zoomLevelToDistance(
    zoomLevel: Double,
    latitude: Double
): Double
```

#### Description
Converts a map zoom level to the straight-line camera distance in meters, applying a correction for latitude. This method does not account for camera tilt or viewport size.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The abstract map zoom level to convert. |
| `latitude` | `Double` | The current latitude in degrees of the camera's target. |

#### Returns
`Double` - The calculated camera distance in meters.

#### Example
```kotlin
val converter = ZoomAltitudeConverter()
val zoomLevel = 10.0
val latitude = 51.5072 // London

val distance = converter.zoomLevelToDistance(zoomLevel, latitude)
// e.g., distance might be ~76000.0 meters
```

### `zoomLevelToDistance` (with viewport scaling)

#### Signature
```kotlin
fun zoomLevelToDistance(
    zoomLevel: Double,
    latitude: Double,
    viewportWidthPx: Int,
    viewportHeightPx: Int
): Double
```

#### Description
Converts a map zoom level to the straight-line camera distance in meters, applying corrections for both latitude and viewport dimensions for higher accuracy across different screen sizes.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The abstract map zoom level to convert. |
| `latitude` | `Double` | The current latitude in degrees of the camera's target. |
| `viewportWidthPx` | `Int` | The width of the map view in pixels. |
| `viewportHeightPx` | `Int` | The height of the map view in pixels. |

#### Returns
`Double` - The calculated camera distance in meters.

#### Example
```kotlin
val converter = ZoomAltitudeConverter()
val zoomLevel = 10.0
val latitude = 51.5072 // London
val viewportWidth = 1440
val viewportHeight = 2560

val distance = converter.zoomLevelToDistance(
    zoomLevel, 
    latitude, 
    viewportWidth, 
    viewportHeight
)
// e.g., distance might be ~105000.0 meters (higher due to larger viewport)
```

---

## Legacy Conversion Methods

These methods are provided for backward compatibility or for very simple use cases where context like latitude, tilt, or viewport size is unavailable. Their accuracy is limited.

### `zoomLevelToAltitude` (basic)

#### Signature
```kotlin
fun zoomLevelToAltitude(zoomLevel: Double): Double
```

#### Description
Performs a basic conversion from zoom level to altitude without any corrections for latitude, tilt, or viewport size.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The abstract map zoom level to convert. |

#### Returns
`Double` - The calculated camera altitude in meters.

#### Example
```kotlin
val converter = ZoomAltitudeConverter()
val altitude = converter.zoomLevelToAltitude(10.0)
// altitude will be 121093.75
```

### `zoomLevelToAltitude` (with latitude)

#### Signature
```kotlin
fun zoomLevelToAltitude(
    zoomLevel: Double,
    latitude: Double
): Double
```

#### Description
Converts a zoom level to altitude, applying a correction for latitude. It does not account for tilt or viewport size.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The abstract map zoom level to convert. |
| `latitude` | `Double` | The current latitude in degrees. |

#### Returns
`Double` - The calculated camera altitude in meters.

#### Example
```kotlin
val converter = ZoomAltitudeConverter()
// At the equator (latitude 0), cos(0) = 1, so result is same as basic method.
val altitudeAtEquator = converter.zoomLevelToAltitude(10.0, 0.0) 
// altitudeAtEquator will be 121093.75

// At 60 degrees latitude, cos(60) = 0.5, so altitude is halved.
val altitudeAt60 = converter.zoomLevelToAltitude(10.0, 60.0)
// altitudeAt60 will be 60546.875
```

### `altitudeToZoomLevel` (basic)

#### Signature
```kotlin
fun altitudeToZoomLevel(altitude: Double): Double
```

#### Description
Performs a basic conversion from altitude to zoom level without any corrections.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `altitude` | `Double` | The camera altitude in meters to convert. |

#### Returns
`Double` - The calculated map zoom level.

#### Example
```kotlin
val converter = ZoomAltitudeConverter()
val zoomLevel = converter.altitudeToZoomLevel(121093.75)
// zoomLevel will be 10.0
```

### `altitudeToZoomLevel` (with latitude)

#### Signature
```kotlin
fun altitudeToZoomLevel(
    altitude: Double,
    latitude: Double
): Double
```

#### Description
Converts an altitude to a zoom level, applying a correction for latitude.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `altitude` | `Double` | The camera altitude in meters to convert. |
| `latitude` | `Double` | The current latitude in degrees. |

#### Returns
`Double` - The calculated map zoom level.

#### Example
```kotlin
val converter = ZoomAltitudeConverter()
val zoomLevel = converter.altitudeToZoomLevel(60546.875, 60.0)
// zoomLevel will be 10.0
```