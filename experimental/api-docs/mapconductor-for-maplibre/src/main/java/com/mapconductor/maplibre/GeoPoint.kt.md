Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

# MapLibre Interoperability Extensions

This document provides a reference for a set of Kotlin extension functions designed to facilitate seamless conversion between the `com.mapconductor.core.features.GeoPoint` class and MapLibre's native geometry types, `org.maplibre.android.geometry.LatLng` and `org.maplibre.geojson.Point`.

These utilities simplify the process of passing location data between your application's core logic and the MapLibre SDK.

---

## `GeoPoint.toLatLng()`

Converts a `GeoPoint` object to a MapLibre `LatLng` object.

### Signature
```kotlin
fun GeoPoint.toLatLng(): LatLng
```

### Description
This extension function creates a new `LatLng` instance using the `latitude`, `longitude`, and `altitude` values from the source `GeoPoint` object.

### Returns
| Type | Description |
|---|---|
| `LatLng` | A new MapLibre `LatLng` object with the same coordinate values. |

### Example
```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.maplibre.toLatLng

val geoPoint = GeoPoint(latitude = 40.7128, longitude = -74.0060, altitude = 10.0)
val latLng = geoPoint.toLatLng()

// latLng is now a LatLng object with latitude=40.7128, longitude=-74.0060, altitude=10.0
```

---

## `GeoPoint.Companion.from(latLng: LatLng)`

A factory function to create a `GeoPoint` instance from a MapLibre `LatLng` object.

### Signature
```kotlin
fun GeoPoint.Companion.from(latLng: LatLng): GeoPoint
```

### Description
This companion object function constructs a `GeoPoint` using the coordinate data from the provided `LatLng` object.

### Parameters
| Parameter | Type | Description |
|---|---|---|
| `latLng` | `LatLng` | The MapLibre `LatLng` object to convert. |

### Returns
| Type | Description |
|---|---|
| `GeoPoint` | A new `GeoPoint` instance with values from the `latLng` object. |

### Example
```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.maplibre.from
import org.maplibre.android.geometry.LatLng

val latLng = LatLng(34.0522, -118.2437, 50.0)
val geoPoint = GeoPoint.from(latLng)

// geoPoint is now a GeoPoint object with latitude=34.0522, longitude=-118.2437, altitude=50.0
```

---

## `LatLng.toGeoPoint()`

Converts a MapLibre `LatLng` object to a `GeoPoint` object.

### Signature
```kotlin
fun LatLng.toGeoPoint(): GeoPoint
```

### Description
This extension function creates a new `GeoPoint` instance using the `latitude`, `longitude`, and `altitude` values from the source `LatLng` object.

### Returns
| Type | Description |
|---|---|
| `GeoPoint` | A new `GeoPoint` object with the same coordinate values. |

### Example
```kotlin
import com.mapconductor.maplibre.toGeoPoint
import org.maplibre.android.geometry.LatLng

val latLng = LatLng(48.8566, 2.3522, 35.0)
val geoPoint = latLng.toGeoPoint()

// geoPoint is now a GeoPoint object with latitude=48.8566, longitude=2.3522, altitude=35.0
```

---

## `GeoPoint.toPoint()`

Converts a `GeoPoint` object to a MapLibre GeoJSON `Point` object.

### Signature
```kotlin
fun GeoPoint.toPoint(): Point
```

### Description
This extension function creates a new GeoJSON `Point` instance from a `GeoPoint`. It correctly handles the `(longitude, latitude)` order required by the GeoJSON specification and the `Point.fromLngLat` factory method.

### Returns
| Type | Description |
|---|---|
| `Point` | A new MapLibre GeoJSON `Point` object representing the `GeoPoint`. |

### Example
```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.maplibre.toPoint

val geoPoint = GeoPoint(latitude = 35.6895, longitude = 139.6917)
val point = geoPoint.toPoint()

// point is now a GeoJSON Point object
```

---

## `GeoPoint.Companion.from(point: Point)`

A factory function to create a `GeoPoint` instance from a MapLibre GeoJSON `Point` object.

### Signature
```kotlin
fun GeoPoint.Companion.from(point: Point): GeoPoint
```

### Description
This companion object function constructs a `GeoPoint` using the coordinate data from the provided GeoJSON `Point` object. It correctly extracts latitude, longitude, and altitude.

### Parameters
| Parameter | Type | Description |
|---|---|---|
| `point` | `Point` | The MapLibre GeoJSON `Point` object to convert. |

### Returns
| Type | Description |
|---|---|
| `GeoPoint` | A new `GeoPoint` instance with values from the `point` object. |

### Example
```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.maplibre.from
import org.maplibre.geojson.Point

// Note: Point.fromLngLat takes longitude first
val point = Point.fromLngLat(-0.1278, 51.5074, 25.0)
val geoPoint = GeoPoint.from(point)

// geoPoint is now a GeoPoint object with latitude=51.5074, longitude=-0.1278, altitude=25.0
```