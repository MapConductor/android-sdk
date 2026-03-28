# Mapbox Interoperability Extensions

This document provides details on the extension functions that facilitate conversion between `GeoRectBounds` and the Mapbox `CoordinateBounds` types. These utilities are essential for seamless integration with the Mapbox SDK.

---

## `toGeoBox()`

### Signature
```kotlin
fun GeoRectBounds.toGeoBox(): CoordinateBounds?
```

### Description
Converts a `GeoRectBounds` instance into a Mapbox `CoordinateBounds` object. This function is useful for passing custom bounding box definitions to Mapbox APIs that expect `CoordinateBounds`.

The function handles cases where the source `GeoRectBounds` might be incomplete. If either the `southWest` or `northEast` property of the `GeoRectBounds` is `null`, this function will return `null`.

### Returns
| Type                 | Description                                                                                             |
| -------------------- | ------------------------------------------------------------------------------------------------------- |
| `CoordinateBounds?`  | The corresponding `CoordinateBounds` object, or `null` if the source `GeoRectBounds` is missing corner points. |

### Example
```kotlin
import com.mapbox.maps.CoordinateBounds
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds

// Assuming toPoint() and other necessary extensions are available

// --- Scenario 1: Successful conversion ---
val validBounds = GeoRectBounds(
    southWest = GeoPoint(lat = 34.0522, lon = -118.2437), // Los Angeles SW
    northEast = GeoPoint(lat = 40.7128, lon = -74.0060)   // New York NE
)

val coordinateBounds: CoordinateBounds? = validBounds.toGeoBox()

// coordinateBounds will be a valid CoordinateBounds object
println("Successful conversion: ${coordinateBounds != null}")
// Expected output: Successful conversion: true


// --- Scenario 2: Incomplete bounds leading to null ---
val incompleteBounds = GeoRectBounds(
    southWest = null,
    northEast = GeoPoint(lat = 40.7128, lon = -74.0060)
)

val nullCoordinateBounds: CoordinateBounds? = incompleteBounds.toGeoBox()

// nullCoordinateBounds will be null
println("Incomplete bounds conversion: ${nullCoordinateBounds == null}")
// Expected output: Incomplete bounds conversion: true
```

---

## `toGeoRectBounds()`

### Signature
```kotlin
fun CoordinateBounds.toGeoRectBounds(): GeoRectBounds
```

### Description
Converts a Mapbox `CoordinateBounds` instance into a `GeoRectBounds` object. This is useful for translating bounding box information from the Mapbox SDK into the application's custom `GeoRectBounds` type.

This function assumes the source `CoordinateBounds` is always valid and contains non-null `southwest` and `northeast` points.

### Returns
| Type            | Description                               |
| --------------- | ----------------------------------------- |
| `GeoRectBounds` | The newly created `GeoRectBounds` object. |

### Example
```kotlin
import com.mapbox.geojson.Point
import com.mapbox.maps.CoordinateBounds
import com.mapconductor.core.features.GeoRectBounds

// Assuming toGeoPoint() and other necessary extensions are available

// Define southwest and northeast points for Mapbox CoordinateBounds
val swPoint = Point.fromLngLat(-118.2437, 34.0522) // Los Angeles
val nePoint = Point.fromLngLat(-74.0060, 40.7128)   // New York

// Create a Mapbox CoordinateBounds instance
val mapboxBounds = CoordinateBounds(swPoint, nePoint)

// Convert to GeoRectBounds
val geoRectBounds: GeoRectBounds = mapboxBounds.toGeoRectBounds()

// geoRectBounds will be a valid GeoRectBounds object
println("SW Latitude: ${geoRectBounds.southWest?.lat}")
println("NE Longitude: ${geoRectBounds.northEast?.lon}")
// Expected output:
// SW Latitude: 34.0522
// NE Longitude: -74.0060
```