# Mapbox GeoJSON Interoperability

This document provides a reference for the extension functions that facilitate conversion between `com.mapconductor.core.features.GeoPoint` and `com.mapbox.geojson.Point` objects. These utilities streamline the process of using MapConductor Core types with the Mapbox SDK.

---

## `GeoPoint.toPoint()`

Converts a `GeoPoint` object to its equivalent Mapbox `Point` representation.

### Signature

```kotlin
fun GeoPoint.toPoint(): Point
```

### Description

This extension function is called on a `GeoPoint` instance. It creates a new Mapbox `Point` object using the `latitude`, `longitude`, and `altitude` from the source `GeoPoint`. This is useful when you need to pass a MapConductor location to a Mapbox SDK function that expects a `Point`.

### Returns

| Type | Description |
|---|---|
| `Point` | A new Mapbox `Point` object with the same coordinate values. |

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.mapbox.toPoint
import com.mapbox.geojson.Point

// Create a GeoPoint instance
val geoPoint = GeoPoint(latitude = 40.7128, longitude = -74.0060, altitude = 10.0)

// Convert it to a Mapbox Point
val mapboxPoint: Point = geoPoint.toPoint()

// Verify the coordinates
println("Longitude: ${mapboxPoint.longitude()}") // -74.0060
println("Latitude: ${mapboxPoint.latitude()}")   // 40.7128
println("Altitude: ${mapboxPoint.altitude()}")   // 10.0
```

---

## `GeoPoint.Companion.from()`

Creates a `GeoPoint` instance from a Mapbox `Point` object.

### Signature

```kotlin
fun GeoPoint.Companion.from(point: Point): GeoPoint
```

### Description

This extension function on the `GeoPoint.Companion` object acts as a factory method. It takes a Mapbox `Point` and constructs a `GeoPoint` with the corresponding `latitude`, `longitude`, and `altitude`.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `point` | `Point` | The Mapbox `Point` object to convert. |

### Returns

| Type | Description |
|---|---|
| `GeoPoint` | A new `GeoPoint` instance with coordinate values from the source `Point`. |

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.mapbox.from
import com.mapbox.geojson.Point

// Create a Mapbox Point instance
val mapboxPoint = Point.fromLngLat(-74.0060, 40.7128, 10.0)

// Create a GeoPoint from the Mapbox Point
val geoPoint: GeoPoint = GeoPoint.from(mapboxPoint)

// Verify the coordinates
println("Longitude: ${geoPoint.longitude}") // -74.0060
println("Latitude: ${geoPoint.latitude}")   // 40.7128
println("Altitude: ${geoPoint.altitude}")   // 10.0
```

---

## `Point.toGeoPoint()`

Converts a Mapbox `Point` object to a `GeoPoint`.

### Signature

```kotlin
fun Point.toGeoPoint(): GeoPoint
```

### Description

This extension function is called on a Mapbox `Point` instance. It creates a new `GeoPoint` using the `latitude` and `longitude` from the source `Point`.

**Note:** This conversion does not preserve the altitude. The resulting `GeoPoint` will have a default altitude value. If altitude is important, use `GeoPoint.Companion.from(point)` instead.

### Returns

| Type | Description |
|---|---|
| `GeoPoint` | A new `GeoPoint` instance. |

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.mapbox.toGeoPoint
import com.mapbox.geojson.Point

// Create a Mapbox Point with altitude
val mapboxPoint = Point.fromLngLat(-74.0060, 40.7128, 10.0)

// Convert it to a GeoPoint
val geoPoint: GeoPoint = mapboxPoint.toGeoPoint()

// Verify the coordinates
println("Longitude: ${geoPoint.longitude}") // -74.0060
println("Latitude: ${geoPoint.latitude}")   // 40.7128
// Note that altitude is not carried over
println("Altitude: ${geoPoint.altitude}")   // Will be the default value for GeoPoint
```