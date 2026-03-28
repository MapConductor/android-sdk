Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet, formatted in Markdown.

# ArcGIS Geometry Converters

This document provides detailed information on a set of Kotlin extension and factory functions designed for seamless conversion between a custom `GeoPoint` type and the ArcGIS Maps SDK for Kotlin `Point` type.

---

## `GeoPoint.toPoint()`

Converts a `GeoPoint` instance into an ArcGIS `Point` object.

### Signature
```kotlin
fun GeoPoint.toPoint(spatialReference: SpatialReference? = null): Point
```

### Description
This extension function transforms a `GeoPoint` into an ArcGIS `Point`. It maps the `longitude`, `latitude`, and `altitude` properties of the `GeoPoint` to the `x`, `y`, and `z` coordinates of the resulting `Point`, respectively. You can optionally assign a `SpatialReference` to the new `Point`.

### Parameters
| Parameter          | Type                  | Description                                                  |
|--------------------|-----------------------|--------------------------------------------------------------|
| `spatialReference` | `SpatialReference?`   | (Optional) The spatial reference to assign to the created `Point`. Defaults to `null`. |

### Returns
| Type    | Description                               |
|---------|-------------------------------------------|
| `Point` | An ArcGIS `Point` object.                 |

### Example
```kotlin
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.mapconductor.core.features.GeoPoint

// Assuming GeoPoint is defined as:
// data class GeoPoint(val latitude: Double, val longitude: Double, val altitude: Double)

val geoPoint = GeoPoint(latitude = 34.0522, longitude = -118.2437, altitude = 71.0)
val wgs84 = SpatialReference.wgs84()

// Convert GeoPoint to an ArcGIS Point with a spatial reference
val arcgisPoint = geoPoint.toPoint(spatialReference = wgs84)

println("ArcGIS Point: x=${arcgisPoint.x}, y=${arcgisPoint.y}, z=${arcgisPoint.z}")
// Expected output: ArcGIS Point: x=-118.2437, y=34.0522, z=71.0
```

---

## `GeoPoint.Companion.fromLatLongAltitude()`

A factory method to create a `GeoPoint` instance.

### Signature
```kotlin
fun GeoPoint.Companion.fromLatLongAltitude(
    latitude: Double,
    longitude: Double,
    altitude: Double
): GeoPoint
```

### Description
Creates a new `GeoPoint` instance from the provided latitude, longitude, and altitude values. This is a convenience factory method that follows the standard `latitude, longitude` ordering.

### Parameters
| Parameter   | Type     | Description                |
|-------------|----------|----------------------------|
| `latitude`  | `Double` | The latitude coordinate.   |
| `longitude` | `Double` | The longitude coordinate.  |
| `altitude`  | `Double` | The altitude value.        |

### Returns
| Type       | Description                               |
|------------|-------------------------------------------|
| `GeoPoint` | A new `GeoPoint` instance.                |

### Example
```kotlin
val geoPoint = GeoPoint.fromLatLongAltitude(
    latitude = 40.7128,
    longitude = -74.0060,
    altitude = 10.0
)

println("Created GeoPoint: lat=${geoPoint.latitude}, lon=${geoPoint.longitude}, alt=${geoPoint.altitude}")
// Expected output: Created GeoPoint: lat=40.7128, lon=-74.0060, alt=10.0
```

---

## `GeoPoint.Companion.fromLongLat()`

A factory method to create a `GeoPoint` instance.

### Signature
```kotlin
fun GeoPoint.Companion.fromLongLat(
    longitude: Double,
    latitude: Double,
    altitude: Double
): GeoPoint
```

### Description
Creates a new `GeoPoint` instance from the provided longitude, latitude, and altitude values. This is a convenience factory method for cases where coordinates are provided in `longitude, latitude` order.

### Parameters
| Parameter   | Type     | Description                |
|-------------|----------|----------------------------|
| `longitude` | `Double` | The longitude coordinate.  |
| `latitude`  | `Double` | The latitude coordinate.   |
| `altitude`  | `Double` | The altitude value.        |

### Returns
| Type       | Description                               |
|------------|-------------------------------------------|
| `GeoPoint` | A new `GeoPoint` instance.                |

### Example
```kotlin
val geoPoint = GeoPoint.fromLongLat(
    longitude = -74.0060,
    latitude = 40.7128,
    altitude = 10.0
)

println("Created GeoPoint: lat=${geoPoint.latitude}, lon=${geoPoint.longitude}, alt=${geoPoint.altitude}")
// Expected output: Created GeoPoint: lat=40.7128, lon=-74.0060, alt=10.0
```

---

## `Point.toGeoPoint()`

Converts an ArcGIS `Point` object into a `GeoPoint`.

### Signature
```kotlin
fun Point.toGeoPoint(): GeoPoint
```

### Description
This extension function transforms an ArcGIS `Point` into a `GeoPoint`. It directly maps the `x`, `y`, and `z` coordinates of the `Point` to the `longitude`, `latitude`, and `altitude` properties of the `GeoPoint`. If the `Point`'s `z` value is `null` (i.e., it is a 2D point), the resulting `GeoPoint`'s altitude will be set to `0.0`.

**Note:** This function performs a direct coordinate mapping (`x` -> `longitude`, `y` -> `latitude`). It does **not** perform any spatial reference projection. For accurate conversion, ensure the source `Point` is in a geographic coordinate system (like WGS84) where the x-coordinate represents longitude and the y-coordinate represents latitude.

### Returns
| Type       | Description                               |
|------------|-------------------------------------------|
| `GeoPoint` | A `GeoPoint` instance.                    |

### Example
```kotlin
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference

// Create a 3D ArcGIS Point in WGS84
val arcgisPoint3D = Point(x = -122.4194, y = 37.7749, z = 52.0, spatialReference = SpatialReference.wgs84())

// Convert the ArcGIS Point to a GeoPoint
val geoPoint3D = arcgisPoint3D.toGeoPoint()

println("Converted 3D GeoPoint: lat=${geoPoint3D.latitude}, lon=${geoPoint3D.longitude}, alt=${geoPoint3D.altitude}")
// Expected output: Converted 3D GeoPoint: lat=37.7749, lon=-122.4194, alt=52.0

// Create a 2D ArcGIS Point (null z-value)
val arcgisPoint2D = Point(x = -0.1278, y = 51.5074, spatialReference = SpatialReference.wgs84())
val geoPoint2D = arcgisPoint2D.toGeoPoint()

println("Converted 2D GeoPoint: lat=${geoPoint2D.latitude}, lon=${geoPoint2D.longitude}, alt=${geoPoint2D.altitude}")
// Expected output: Converted 2D GeoPoint: lat=51.5074, lon=-0.1278, alt=0.0
```