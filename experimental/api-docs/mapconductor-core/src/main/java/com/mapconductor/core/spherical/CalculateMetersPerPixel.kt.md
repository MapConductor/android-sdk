Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

## Spherical Geometry Utilities

This document provides details on utility functions for performing calculations related to spherical geometry on Web Mercator maps.

### `calculateMetersPerPixel`

#### Signature

```kotlin
fun calculateMetersPerPixel(
    latitude: Double,
    zoom: Double,
    tileSize: Double = 256.0
): Double
```

#### Description

Calculates the number of meters represented by a single pixel on a Web Mercator map for a given latitude and zoom level.

The calculation is based on the standard Web Mercator projection, where the map is stretched vertically as the distance from the equator increases. This function accounts for this distortion by adjusting the scale based on the cosine of the latitude. At zoom level 0, the entire world circumference fits within a single tile. Each subsequent zoom level doubles the resolution.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `latitude` | `Double` | The geographical latitude in degrees, ranging from -90.0 to 90.0. |
| `zoom` | `Double` | The map zoom level. Higher values correspond to greater detail. |
| `tileSize` | `Double` | The size of the map tiles in pixels. The default is `256.0`. |

#### Returns

**Type:** `Double`

Returns the number of meters per pixel at the specified latitude and zoom level.

#### Example

```kotlin
val latitude = 40.7128 // New York City
val zoomLevel = 12.0

val metersPerPixel = calculateMetersPerPixel(latitude = latitude, zoom = zoomLevel)

// At latitude 40.7128 and zoom 12.0, 1 pixel represents approximately 11.8 meters.
println("At latitude $latitude and zoom $zoomLevel, 1 pixel represents approximately $metersPerPixel meters.")
```

---

### `meterToPixel`

#### Signature

```kotlin
fun meterToPixel(
    meter: Double,
    latitude: Double,
    zoom: Double,
    tileSize: Double = 256.0
): Double
```

#### Description

Converts a distance in meters to its equivalent size in pixels at a specific latitude and zoom level on a Web Mercator map. This function is the inverse of `calculateMetersPerPixel` and is useful for drawing shapes, markers, or overlays with real-world dimensions on a map.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `meter` | `Double` | The distance in meters to be converted to pixels. |
| `latitude` | `Double` | The geographical latitude in degrees where the conversion is applied. |
| `zoom` | `Double` | The map zoom level. |
| `tileSize` | `Double` | The size of the map tiles in pixels. Common values are `256.0` (e.g., Google Maps) and `512.0` (e.g., Mapbox v10+). The default is `256.0`. |

#### Returns

**Type:** `Double`

Returns the equivalent size in pixels for the given meter value.

#### Example

```kotlin
val distanceInMeters = 1000.0 // 1 kilometer
val latitude = 34.0522 // Los Angeles
val zoomLevel = 14.0

val sizeInPixels = meterToPixel(
    meter = distanceInMeters,
    latitude = latitude,
    zoom = zoomLevel
)

// 1000.0 meters at latitude 34.0522 and zoom 14.0 is equivalent to approximately 254.0 pixels.
println("$distanceInMeters meters at latitude $latitude and zoom $zoomLevel is equivalent to approximately $sizeInPixels pixels.")
```