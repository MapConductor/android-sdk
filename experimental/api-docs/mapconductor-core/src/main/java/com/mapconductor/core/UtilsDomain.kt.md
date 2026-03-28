Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

***

## Package `com.mapconductor.core`

This document provides an overview of utility functions available in the `com.mapconductor.core` package.

### `printPoints`

#### Signature
```kotlin
fun printPoints(
    tag: String,
    points: List<GeoPointInterface>
)
```

#### Description
Logs a list of geographic points to the Android Logcat for debugging purposes. It first logs a separator line, then iterates through the provided list. Each point is converted to its URL-safe string representation using `GeoPoint.from(point).toUrlValue()` before being logged with a `DEBUG` level.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `tag` | `String` | The tag to use for the log messages in Logcat. |
| `points` | `List<GeoPointInterface>` | A list of geographic point objects to be printed. |

#### Returns
This function does not return a value.

#### Example
```kotlin
// Assuming GeoPoint is a data class implementing GeoPointInterface
val point1 = GeoPoint(latitude = 35.681236, longitude = 139.767125) // Tokyo Station
val point2 = GeoPoint(latitude = 34.652500, longitude = 135.506302) // Osaka

val pointsToLog = listOf(point1, point2)

// Log the points with the tag "MapDebug"
printPoints("MapDebug", pointsToLog)

/*
Expected Logcat output:
D/MapDebug: -----------
D/MapDebug: 35.681236,139.767125
D/MapDebug: 34.6525,135.506302
*/
```

### `calculateZIndex`

#### Signature
```kotlin
fun calculateZIndex(geoPointBase: GeoPointInterface): Int
```

#### Description
Calculates a Z-index integer value for a geographic point. The calculation is designed to create a visual sense of depth on a 2D map, where points further south appear "in front" (higher Z-index) and points further north appear "in the back" (lower Z-index). For points at the same latitude, points further west are given priority (a higher Z-index) to appear in front.

The formula used is: `(-latitude * 1,000,000 - longitude).roundToInt()`

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `geoPointBase` | `GeoPointInterface` | The geographic point for which to calculate the Z-index. |

#### Returns
| Type | Description |
| :--- | :--- |
| `Int` | The calculated Z-index value. |

#### Example
```kotlin
// Assuming GeoPoint is a data class implementing GeoPointInterface
val point = GeoPoint(latitude = 35.681236, longitude = 139.767125)

val zIndex = calculateZIndex(point)

// zIndex will be -35821003
println(zIndex) 
```

### `normalizeLng`

#### Signature
```kotlin
fun normalizeLng(lng: Double): Double
```

#### Description
Normalizes a given longitude value to ensure it falls within the standard geographical range of `[-180.0, 180.0]`. This is useful for correcting longitude values that may have wrapped around during map panning or other calculations.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `lng` | `Double` | The longitude value to be normalized. |

#### Returns
| Type | Description |
| :--- | :--- |
| `Double` | The normalized longitude value, guaranteed to be within the range `[-180.0, 180.0]`. |

#### Example
```kotlin
// A longitude value that is out of the standard range
val longitude1 = 190.0
val normalizedLng1 = normalizeLng(longitude1) // Result: -170.0

// A negative longitude value that is out of range
val longitude2 = -200.0
val normalizedLng2 = normalizeLng(longitude2) // Result: 160.0

// A longitude value already within the range
val longitude3 = 150.5
val normalizedLng3 = normalizeLng(longitude3) // Result: 150.5

println("190.0 becomes $normalizedLng1")
println("-200.0 becomes $normalizedLng2")
println("150.5 becomes $normalizedLng3")
```