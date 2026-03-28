Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

# Google Maps Type Conversions

This document provides details on the extension functions used for converting between MapConductor Core and Google Maps data types. These utilities facilitate seamless interoperability between the two libraries.

---

## `toLatLngBounds()`

Converts a `GeoRectBounds` object from the MapConductor Core library into a `LatLngBounds` object for the Google Maps SDK.

### Signature

```kotlin
fun GeoRectBounds.toLatLngBounds(): LatLngBounds?
```

### Description

This extension function transforms a `GeoRectBounds` instance into its equivalent `LatLngBounds` representation. The conversion relies on the `southWest` and `northEast` corners of the `GeoRectBounds`. If either of these corners is `null`, the function will return `null` to prevent potential runtime errors.

### Parameters

| Parameter | Type              | Description                               |
|-----------|-------------------|-------------------------------------------|
| `this`    | `GeoRectBounds`   | The `GeoRectBounds` instance to convert.  |

### Returns

A `LatLngBounds` object that represents the same geographical area, or `null` if the source `GeoRectBounds` has a `null` `southWest` or `northEast` property.

### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.google.android.gms.maps.model.LatLngBounds

// Define the source GeoRectBounds
val geoRect = GeoRectBounds(
    southWest = GeoPoint(latitude = 34.0522, longitude = -118.2437), // Los Angeles
    northEast = GeoPoint(latitude = 40.7128, longitude = -74.0060)   // New York City
)

// Perform the conversion
val latLngBounds: LatLngBounds? = geoRect.toLatLngBounds()

// The result is a Google Maps LatLngBounds object
if (latLngBounds != null) {
    println("Conversion successful: $latLngBounds")
} else {
    println("Conversion failed because one of the corners was null.")
}
```

---

## `toGeoRectBounds()`

Converts a `LatLngBounds` object from the Google Maps SDK into a `GeoRectBounds` object for the MapConductor Core library.

### Signature

```kotlin
fun LatLngBounds.toGeoRectBounds(): GeoRectBounds
```

### Description

This extension function transforms a Google Maps `LatLngBounds` instance into its equivalent `GeoRectBounds` representation. It maps the `southwest` and `northeast` properties of the `LatLngBounds` to create a new `GeoRectBounds` object. This conversion is non-nullable and always returns a valid `GeoRectBounds` instance.

### Parameters

| Parameter | Type              | Description                               |
|-----------|-------------------|-------------------------------------------|
| `this`    | `LatLngBounds`    | The `LatLngBounds` instance to convert.   |

### Returns

A non-null `GeoRectBounds` object that represents the same geographical area.

### Example

```kotlin
import com.mapconductor.core.features.GeoRectBounds
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

// Define the source LatLngBounds
val southwestCorner = LatLng(34.0522, -118.2437) // Los Angeles
val northeastCorner = LatLng(40.7128, -74.0060)   // New York City
val latLngBounds = LatLngBounds(southwestCorner, northeastCorner)

// Perform the conversion
val geoRect: GeoRectBounds = latLngBounds.toGeoRectBounds()

// The result is a MapConductor GeoRectBounds object
println("Conversion successful: $geoRect")
```