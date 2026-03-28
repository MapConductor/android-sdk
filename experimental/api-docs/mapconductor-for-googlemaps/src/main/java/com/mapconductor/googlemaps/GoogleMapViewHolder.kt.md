Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# GoogleMapViewHolder

A concrete implementation of `MapViewHolderInterface` that wraps the Google Maps SDK (`MapView` and `GoogleMap`). It provides essential utility functions for converting between geographical coordinates and screen pixel coordinates.

This class acts as an adapter, allowing the core map logic to interact with the Google Maps Android API in a standardized way.

## Constructor

### Signature

```kotlin
class GoogleMapViewHolder(
    override val mapView: MapView,
    override val map: GoogleMap,
) : MapViewHolderInterface<MapView, GoogleMap>
```

### Description

Creates an instance of `GoogleMapViewHolder`, which holds references to the `MapView` and the `GoogleMap` object.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `mapView` | `MapView` | The Android `MapView` instance being controlled. |
| `map` | `GoogleMap` | The `GoogleMap` object used for map interactions and projections. |

## Methods

### toScreenOffset

Converts a geographical coordinate (`GeoPointInterface`) into a screen pixel coordinate (`Offset`) relative to the map view's top-left corner.

#### Signature

```kotlin
override fun toScreenOffset(position: GeoPointInterface): Offset?
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | The geographical coordinate to convert. |

#### Returns

**`Offset?`**

A Compose `Offset` object representing the x and y pixel coordinates on the screen. Returns `null` if the projection is not available or fails.

### fromScreenOffset

Converts a screen pixel coordinate (`Offset`) into a geographical coordinate (`GeoPoint`). This is a `suspend` function, as the underlying projection operation can be asynchronous.

#### Signature

```kotlin
override suspend fun fromScreenOffset(offset: Offset): GeoPoint?
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `offset` | `Offset` | The screen pixel coordinate (relative to the map view's top-left corner) to convert. |

#### Returns

**`GeoPoint?`**

A `GeoPoint` object representing the geographical location at the given screen offset. Returns `null` if the conversion is not possible.

## Example

The following example demonstrates how to create a `GoogleMapViewHolder` and use its conversion methods within a coroutine scope.

```kotlin
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.mapconductor.core.features.GeoPoint
import kotlinx.coroutines.launch

// Assume you have a MapView and a GoogleMap instance from your map setup
lateinit var mapView: MapView
lateinit var googleMap: GoogleMap

// In your Composable or setup code:
val mapViewHolder = GoogleMapViewHolder(mapView, googleMap)
val coroutineScope = rememberCoroutineScope()

// --- Example 1: Convert a GeoPoint to a screen Offset ---
val newYorkCity = GeoPoint(latitude = 40.7128, longitude = -74.0060)
val screenOffset = mapViewHolder.toScreenOffset(newYorkCity)

if (screenOffset != null) {
    println("Screen coordinates for NYC: x=${screenOffset.x}, y=${screenOffset.y}")
} else {
    println("Could not project GeoPoint to screen.")
}

// --- Example 2: Convert a screen Offset to a GeoPoint ---
// This must be called from a coroutine
coroutineScope.launch {
    // An offset representing a tap on the screen, e.g., the center of a 1080x1920 display
    val touchOffset = Offset(x = 540f, y = 960f) 
    val locationAtOffset = mapViewHolder.fromScreenOffset(touchOffset)

    if (locationAtOffset != null) {
        println("Geographical coordinates at offset: lat=${locationAtOffset.latitude}, lon=${locationAtOffset.longitude}")
    } else {
        println("Could not project screen offset to a location.")
    }
}
```