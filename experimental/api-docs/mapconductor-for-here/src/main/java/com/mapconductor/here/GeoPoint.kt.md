Of course! Here is the high-quality SDK documentation for the provided code snippet.

# HERE SDK Interoperability Extensions

This document details a set of Kotlin extension functions designed to facilitate seamless conversion between the custom `GeoPoint` type and the HERE SDK's `GeoCoordinates` and `GeoOrientation` classes. These utilities simplify the process of passing data between your application's data models and the HERE SDK.

---

### `GeoPoint.toGeoCoordinates()`

Converts an instance of `GeoPoint` into a HERE SDK `GeoCoordinates` object. This is useful for passing a custom `GeoPoint` to HERE SDK APIs that require a `GeoCoordinates` parameter.

**Signature**
```kotlin
fun GeoPoint.toGeoCoordinates(): GeoCoordinates
```

**Returns**
| Type | Description |
|---|---|
| `GeoCoordinates` | A new `GeoCoordinates` object with the same latitude and longitude as the source `GeoPoint`. |

**Example**
```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.here.toGeoCoordinates

// Assuming GeoPoint is a class like: data class GeoPoint(val latitude: Double, val longitude: Double)
val myGeoPoint = GeoPoint(latitude = 40.7128, longitude = -74.0060)

// Convert to HERE SDK's GeoCoordinates
val hereGeoCoordinates = myGeoPoint.toGeoCoordinates()

println("Latitude: ${hereGeoCoordinates.latitude}, Longitude: ${hereGeoCoordinates.longitude}")
// Output: Latitude: 40.7128, Longitude: -74.0060
```

---

### `GeoPoint.Companion.from()`

A factory function on the `GeoPoint` companion object that creates a new `GeoPoint` instance from a HERE SDK `GeoCoordinates` object.

**Signature**
```kotlin
fun GeoPoint.Companion.from(geoCoordinates: GeoCoordinates): GeoPoint
```

**Parameters**
| Parameter | Type | Description |
|---|---|---|
| `geoCoordinates` | `GeoCoordinates` | The HERE SDK `GeoCoordinates` object to convert. |

**Returns**
| Type | Description |
|---|---|
| `GeoPoint` | A new `GeoPoint` instance with latitude and longitude values from the provided `GeoCoordinates`. |

**Example**
```kotlin
import com.here.sdk.core.GeoCoordinates
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.here.from

val hereGeoCoordinates = GeoCoordinates(34.0522, -118.2437)

// Create a GeoPoint from HERE SDK's GeoCoordinates
val myGeoPoint = GeoPoint.from(hereGeoCoordinates)

println("Latitude: ${myGeoPoint.latitude}, Longitude: ${myGeoPoint.longitude}")
// Output: Latitude: 34.0522, Longitude: -118.2437
```

---

### `GeoCoordinates.toGeoPoint()`

Converts a HERE SDK `GeoCoordinates` object into a `GeoPoint` object. This is useful for converting results from the HERE SDK into the application's custom `GeoPoint` type.

**Signature**
```kotlin
fun GeoCoordinates.toGeoPoint(): GeoPoint
```

**Returns**
| Type | Description |
|---|---|
| `GeoPoint` | A new `GeoPoint` object with the same latitude and longitude as the source `GeoCoordinates`. |

**Example**
```kotlin
import com.here.sdk.core.GeoCoordinates
import com.mapconductor.here.toGeoPoint

val hereGeoCoordinates = GeoCoordinates(51.5074, -0.1278) // London

// Convert to your application's GeoPoint
val myGeoPoint = hereGeoCoordinates.toGeoPoint()

println("Latitude: ${myGeoPoint.latitude}, Longitude: ${myGeoPoint.longitude}")
// Output: Latitude: 51.5074, Longitude: -0.1278
```

---

### `GeoCoordinates.toUpdate()`

Creates a `GeoCoordinatesUpdate` from a `GeoCoordinates` instance. This is a convenience function for APIs that require updates, such as animating map camera properties.

**Signature**
```kotlin
fun GeoCoordinates.toUpdate(): GeoCoordinatesUpdate
```

**Returns**
| Type | Description |
|---|---|
| `GeoCoordinatesUpdate` | A new `GeoCoordinatesUpdate` object containing the source `GeoCoordinates`. |

**Example**
```kotlin
import com.here.sdk.core.GeoCoordinates
import com.mapconductor.here.toUpdate

val targetCoordinates = GeoCoordinates(48.8566, 2.3522) // Paris

// Create an update object for camera animation
val coordinateUpdate = targetCoordinates.toUpdate()

// This update can now be used with the MapCamera
// mapCamera.lookAt(coordinateUpdate, ...)
```

---

### `GeoOrientation.toUpdate()`

Creates a `GeoOrientationUpdate` from a `GeoOrientation` instance. This is a convenience function for APIs that require orientation updates, such as changing the map camera's bearing or tilt.

**Signature**
```kotlin
fun GeoOrientation.toUpdate(): GeoOrientationUpdate
```

**Returns**
| Type | Description |
|---|---|
| `GeoOrientationUpdate` | A new `GeoOrientationUpdate` object containing the source `GeoOrientation`. |

**Example**
```kotlin
import com.here.sdk.core.GeoOrientation
import com.mapconductor.here.toUpdate

val newOrientation = GeoOrientation(45.0, 0.0) // 45-degree bearing, 0-degree tilt

// Create an update object for camera orientation
val orientationUpdate = newOrientation.toUpdate()

// This update can now be used to change the map's orientation
// mapCamera.lookAt(point, orientationUpdate, ...)
```