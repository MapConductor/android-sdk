# WebMercator

The `WebMercator` object provides utility methods for converting between geographic coordinates (latitude and longitude) and the Web Mercator projection (EPSG:3857) coordinates. This projection is a standard for web-based mapping services.

This object implements the `ProjectionInterface`.

---

## project

Projects a geographic coordinate (latitude and longitude) into a 2D Cartesian coordinate (x, y) using the Web Mercator projection.

### Signature

```kotlin
fun project(position: GeoPointInterface): Offset
```

### Description

This function takes a `GeoPointInterface` object, which represents a point on the Earth's surface with latitude and longitude, and converts it into a 2D planar coordinate `Offset`. This is essential for rendering geographic data on a flat map surface.

### Parameters

| Parameter  | Type                | Description                                                  |
|------------|---------------------|--------------------------------------------------------------|
| `position` | `GeoPointInterface` | The geographic point to project, containing latitude and longitude. |

### Returns

**Type:** `Offset`

An `Offset` object representing the projected Cartesian coordinates (x, y) in meters.

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.projection.WebMercator
import androidx.compose.ui.geometry.Offset

// Define a geographic point for New York City
val nycGeoPoint = GeoPoint(latitude = 40.7128, longitude = -74.0060)

// Project the geographic point to Web Mercator coordinates
val projectedOffset: Offset = WebMercator.project(nycGeoPoint)

// projectedOffset.x will be approximately -8238322.5
// projectedOffset.y will be approximately 4970144.5
println("Projected Coordinates: x=${projectedOffset.x}, y=${projectedOffset.y}")
```

---

## unproject

Converts a 2D Cartesian coordinate (x, y) from the Web Mercator projection back into a geographic coordinate (latitude and longitude).

### Signature

```kotlin
fun unproject(point: Offset): GeoPointInterface
```

### Description

This function performs the inverse operation of `project`. It takes a 2D `Offset` in Web Mercator coordinates and converts it back to its corresponding geographic location represented by a `GeoPointInterface`.

### Parameters

| Parameter | Type     | Description                                  |
|-----------|----------|----------------------------------------------|
| `point`   | `Offset` | The Cartesian point (x, y) in meters to unproject. |

### Returns

**Type:** `GeoPointInterface`

A `GeoPointInterface` object representing the geographic coordinate (latitude, longitude). Note that the `altitude` of the returned point will always be `null`.

### Example

```kotlin
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.projection.WebMercator
import androidx.compose.ui.geometry.Offset

// Define a Web Mercator coordinate
val projectedOffset = Offset(x = -8238322.5f, y = 4970144.5f)

// Unproject the coordinate back to a geographic point
val geoPoint: GeoPointInterface = WebMercator.unproject(projectedOffset)

// geoPoint.latitude will be approximately 40.7128
// geoPoint.longitude will be approximately -74.0060
println("Unprojected GeoPoint: lat=${geoPoint.latitude}, lon=${geoPoint.longitude}")
```