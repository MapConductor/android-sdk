Of course! Here is the high-quality SDK documentation for the provided code snippet.

# GeoBox and GeoRectBounds Conversion Utilities

This document provides details on the extension functions for converting between `GeoRectBounds` and the HERE SDK's `GeoBox` types. These utilities facilitate interoperability between the Map Conductor Core library and the HERE SDK.

---

## `GeoRectBounds.toGeoBox()`

Converts a `GeoRectBounds` object to a HERE SDK `GeoBox` object.

### Signature
```kotlin
fun GeoRectBounds.toGeoBox(): GeoBox?
```

### Description
This extension function creates a `GeoBox` that represents the same geographical bounding box as the source `GeoRectBounds`. The conversion relies on the `southWest` and `northEast` corner points of the `GeoRectBounds`.

If either the `southWest` or `northEast` property of the `GeoRectBounds` is `null`, the conversion cannot be completed, and the function will return `null`.

### Returns
| Type | Description |
|---|---|
| `GeoBox?` | A new `GeoBox` instance representing the bounds, or `null` if the source `GeoRectBounds` has null corner points. |

### Example
```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.here.toGeoBox

// 1. Define the corner points for GeoRectBounds
val southWestPoint = GeoPoint(latitude = 40.7128, longitude = -74.0060) // New York City
val northEastPoint = GeoPoint(latitude = 48.8566, longitude = 2.3522)   // Paris

// 2. Create a GeoRectBounds instance
val rectBounds = GeoRectBounds(southWest = southWestPoint, northEast = northEastPoint)

// 3. Convert to a GeoBox
val geoBox = rectBounds.toGeoBox()

if (geoBox != null) {
    println("Successfully converted to GeoBox.")
    println("SW Corner: ${geoBox.southWestCorner}")
    println("NE Corner: ${geoBox.northEastCorner}")
} else {
    println("Conversion failed: GeoRectBounds contains null points.")
}

// Example with null points
val invalidRectBounds = GeoRectBounds(southWest = null, northEast = northEastPoint)
val nullGeoBox = invalidRectBounds.toGeoBox() // This will be null
println("Result of converting invalid bounds: $nullGeoBox")
```

---

## `GeoBox.toGeoRectBounds()`

Converts a HERE SDK `GeoBox` object to a `GeoRectBounds` object.

### Signature
```kotlin
fun GeoBox.toGeoRectBounds(): GeoRectBounds
```

### Description
This extension function creates a `GeoRectBounds` object that represents the same geographical bounding box as the source `GeoBox`. It extracts the `southWestCorner` and `northEastCorner` from the `GeoBox` to construct the new `GeoRectBounds` instance. This operation is non-nullable and will always succeed.

### Returns
| Type | Description |
|---|---|
| `GeoRectBounds` | A new `GeoRectBounds` instance representing the same area as the source `GeoBox`. |

### Example
```kotlin
import com.here.sdk.core.GeoBox
import com.here.sdk.core.GeoCoordinates
import com.mapconductor.here.toGeoRectBounds

// 1. Define the corner coordinates for a GeoBox
val southWestCoords = GeoCoordinates(52.5163, 13.3777) // Berlin
val northEastCoords = GeoCoordinates(51.5074, -0.1278) // London

// 2. Create a GeoBox instance
val geoBox = GeoBox(southWestCoords, northEastCoords)

// 3. Convert to a GeoRectBounds
val rectBounds = geoBox.toGeoRectBounds()

println("Successfully converted to GeoRectBounds.")
println("SW Point: ${rectBounds.southWest}")
println("NE Point: ${rectBounds.northEast}")
```