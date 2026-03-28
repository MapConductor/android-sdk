Of course! Here is the high-quality SDK documentation for the provided code snippet.

# GeoPoint and LatLng Interoperability

This document outlines a set of Kotlin extension functions designed to facilitate seamless conversion between the `com.mapconductor.core.features.GeoPoint` and the Google Maps `com.google.android.gms.maps.model.LatLng` types. These utilities simplify the process of working with location data across different parts of an application that may use these distinct types.

---

## `GeoPoint.toLatLng()`

Converts a `GeoPoint` object into a Google Maps `LatLng` object. This is useful for passing `GeoPoint` data to Google Maps SDK components that require a `LatLng`.

### Signature

```kotlin
fun GeoPoint.toLatLng(): LatLng
```

### Description

This extension function is called on an instance of `GeoPoint`. It creates and returns a new `LatLng` object using the latitude and longitude from the source `GeoPoint`.

### Returns

| Type | Description |
|---|---|
| `LatLng` | A `LatLng` object with the same latitude and longitude as the source `GeoPoint`. |

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.googlemaps.toLatLng
import com.google.android.gms.maps.model.MarkerOptions

// 1. Create a GeoPoint instance
val geoPoint = GeoPoint(latitude = 40.7128, longitude = -74.0060)

// 2. Convert it to a LatLng object
val latLng = geoPoint.toLatLng()

// 3. Use the LatLng object with the Google Maps SDK
// googleMap.addMarker(MarkerOptions().position(latLng).title("New York City"))
```

---

## `GeoPoint.Companion.from()`

Creates a `GeoPoint` instance from a Google Maps `LatLng` object. This serves as a factory method for converting Google Maps data into the `GeoPoint` type.

### Signature

```kotlin
fun GeoPoint.Companion.from(latLng: LatLng): GeoPoint
```

### Description

This extension function is called on the `GeoPoint` companion object. It takes a `LatLng` object and constructs a new `GeoPoint` instance with the corresponding latitude and longitude.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `latLng` | `LatLng` | The Google Maps `LatLng` object to convert. |

### Returns

| Type | Description |
|---|---|
| `GeoPoint` | A new `GeoPoint` instance with the latitude and longitude from the provided `LatLng` object. |

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.googlemaps.from
import com.google.android.gms.maps.model.LatLng

// 1. Assume you have a LatLng object from the Google Maps SDK
val latLng = LatLng(34.0522, -118.2437)

// 2. Create a GeoPoint from the LatLng object
val geoPoint = GeoPoint.from(latLng)

// Now you can use the geoPoint object in your application's core logic
// assert(geoPoint.latitude == 34.0522)
// assert(geoPoint.longitude == -118.2437)
```

---

## `LatLng.toGeoPoint()`

Converts a Google Maps `LatLng` object into a `GeoPoint` object. This is a convenient way to handle location data returned from the Google Maps SDK and use it within your application's core logic.

### Signature

```kotlin
fun LatLng.toGeoPoint(): GeoPoint
```

### Description

This extension function is called on an instance of `LatLng`. It creates and returns a new `GeoPoint` object using the latitude and longitude from the source `LatLng`.

### Returns

| Type | Description |
|---|---|
| `GeoPoint` | A `GeoPoint` object with the same latitude and longitude as the source `LatLng`. |

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.googlemaps.toGeoPoint
import com.google.android.gms.maps.model.LatLng

// 1. Assume you receive a LatLng from a map click event
val mapClickPosition = LatLng(48.8566, 2.3522)

// 2. Convert the LatLng to a GeoPoint
val geoPoint = mapClickPosition.toGeoPoint()

// 3. Use the GeoPoint for other operations, like saving to a database
// saveLocation(geoPoint)
```