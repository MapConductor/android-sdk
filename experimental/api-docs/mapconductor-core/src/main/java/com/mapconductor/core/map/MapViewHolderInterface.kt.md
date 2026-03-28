Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

# MapViewHolderInterface

## Signature

```kotlin
interface MapViewHolderInterface<ActualMapView, ActualMap>
```

## Description

The `MapViewHolderInterface` defines a contract for a view holder that manages a map instance. It serves as an abstraction layer, bridging the gap between the core application logic and a platform-specific map implementation (e.g., Google Maps, Mapbox).

This interface provides access to the native map view and map objects and defines essential utility functions for converting between geographical coordinates (`GeoPoint`) and screen coordinates (`Offset`).

The generic types `ActualMapView` and `ActualMap` allow implementers to specify the concrete types of the map view and map objects provided by the underlying map SDK.

---

## Properties

| Name      | Type            | Description                                            |
| :-------- | :-------------- | :----------------------------------------------------- |
| `mapView` | `ActualMapView` | The underlying, platform-specific map view instance.   |
| `map`     | `ActualMap`     | The underlying, platform-specific map object instance. |

---

## Functions

### toScreenOffset

Converts a geographical coordinate to its corresponding pixel offset on the screen. This is useful for positioning UI elements (like custom markers or overlays) on the map at a specific latitude and longitude.

**Signature**

```kotlin
fun toScreenOffset(position: GeoPointInterface): Offset?
```

**Parameters**

| Parameter  | Type                | Description                                                                                                                            |
| :--------- | :------------------ | :------------------------------------------------------------------------------------------------------------------------------------- |
| `position` | `GeoPointInterface` | The geographical coordinate (`latitude`, `longitude`) to be converted into a screen offset.                                            |

**Returns**

`Offset?`: The screen coordinate as an `Offset(x, y)`, or `null` if the geographical point is not currently visible on the screen or the conversion cannot be performed.

---

### fromScreenOffset

Asynchronously converts a screen pixel offset (e.g., from a touch event) to its corresponding geographical coordinate on the map.

As a `suspend` function, it must be called from a coroutine scope. This allows for non-blocking execution, which is often required by map SDKs for projection calculations.

**Signature**

```kotlin
suspend fun fromScreenOffset(offset: Offset): GeoPoint?
```

**Parameters**

| Parameter | Type     | Description                                                              |
| :-------- | :------- | :----------------------------------------------------------------------- |
| `offset`  | `Offset` | The screen pixel coordinate (`x`, `y`) to be converted to a geo-coordinate. |

**Returns**

`GeoPoint?`: The corresponding `GeoPoint` on the map, or `null` if the offset is outside the map's display area.

---

### fromScreenOffsetSync

Synchronously converts a screen pixel offset to its corresponding geographical coordinate.

This function provides a default implementation that returns `null`. Implementers should override this method only if the underlying map SDK supports a synchronous, blocking conversion. Use `fromScreenOffset` for a more universally compatible, non-blocking approach.

**Signature**

```kotlin
fun fromScreenOffsetSync(offset: Offset): GeoPoint? = null
```

**Parameters**

| Parameter | Type     | Description                                                              |
| :-------- | :------- | :----------------------------------------------------------------------- |
| `offset`  | `Offset` | The screen pixel coordinate (`x`, `y`) to be converted to a geo-coordinate. |

**Returns**

`GeoPoint?`: The corresponding `GeoPoint`, or `null` if the conversion is not supported synchronously or the offset is outside the map's display area.

---

## Example

Here is an example of how to implement and use the `MapViewHolderInterface` with a hypothetical Google Maps integration.

### 1. Implementation

```kotlin
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapViewHolderInterface
import kotlinx.coroutines.tasks.await

// A concrete implementation for Google Maps
class GoogleMapViewHolder(
    override val mapView: MapView,
    override val map: GoogleMap
) : MapViewHolderInterface<MapView, GoogleMap> {

    override fun toScreenOffset(position: GeoPointInterface): Offset? {
        val projection = map.projection
        val screenPoint = projection.toScreenLocation(LatLng(position.latitude, position.longitude))
        return if (mapView.getGlobalVisibleRect(android.graphics.Rect(0, 0, mapView.width, mapView.height))) {
            Offset(screenPoint.x.toFloat(), screenPoint.y.toFloat())
        } else {
            null // The point is not on the visible part of the map
        }
    }

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? {
        // Google Maps projection is synchronous, but we adhere to the suspend contract.
        // For other SDKs, this might involve an await() call.
        val latLng = map.projection.fromScreenLocation(
            android.graphics.Point(offset.x.toInt(), offset.y.toInt())
        )
        return latLng?.let { GeoPoint(it.latitude, it.longitude) }
    }
    
    override fun fromScreenOffsetSync(offset: Offset): GeoPoint? {
        // Since Google Maps supports this synchronously, we can override the default.
        val latLng = map.projection.fromScreenLocation(
            android.graphics.Point(offset.x.toInt(), offset.y.toInt())
        )
        return latLng?.let { GeoPoint(it.latitude, it.longitude) }
    }
}
```

### 2. Usage

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint
import kotlinx.coroutines.launch

@Composable
fun MapWithCustomMarker(mapViewHolder: GoogleMapViewHolder) {
    val coroutineScope = rememberCoroutineScope()
    
    // A fixed geographical point for our marker
    val tokyoTowerPoint = GeoPoint(35.6586, 139.7454)
    
    // State to hold the screen position of our marker
    var markerScreenOffset by remember { mutableStateOf<Offset?>(null) }

    // Update the marker's screen position whenever the map moves
    LaunchedEffect(mapViewHolder.map.cameraPosition) {
        markerScreenOffset = mapViewHolder.toScreenOffset(tokyoTowerPoint)
    }

    Box {
        // The actual map view would be here
        // GoogleMapView(...)

        // Place a custom Composable icon on the map
        markerScreenOffset?.let { offset ->
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Tokyo Tower",
                modifier = Modifier.offset(
                    x = offset.x.dp,
                    y = offset.y.dp
                )
            )
        }
    }

    // Example of converting a tap location to a GeoPoint
    fun handleTap(tapOffset: Offset) {
        coroutineScope.launch {
            val geoPoint = mapViewHolder.fromScreenOffset(tapOffset)
            geoPoint?.let {
                println("Tapped at: Lat ${it.latitude}, Lon ${it.longitude}")
            }
        }
    }
}
```