# MapboxMapViewHolder

A view holder class that encapsulates Mapbox's `MapView` and `MapboxMap` objects. It acts as a bridge between the MapConductor core library and the Mapbox SDK, providing essential functionalities like coordinate transformations and lifecycle management.

This class implements `MapViewHolderInterface<MapView, MapboxMap>` and `MapboxLifecycleObserver`. Upon initialization, it automatically registers itself as a lifecycle observer for the provided `MapView`.

```kotlin
class MapboxMapViewHolder(
    override val mapView: MapView,
    override val map: MapboxMap,
) : MapViewHolderInterface<MapView, MapboxMap>,
    MapboxLifecycleObserver
```

## Constructor

### Signature

```kotlin
MapboxMapViewHolder(mapView: MapView, map: MapboxMap)
```

### Description

Creates an instance of `MapboxMapViewHolder`.

### Parameters

| Parameter | Type        | Description                                     |
| :-------- | :---------- | :---------------------------------------------- |
| `mapView` | `MapView`   | The Mapbox `MapView` instance.                  |
| `map`     | `MapboxMap` | The `MapboxMap` instance associated with the `mapView`. |

---

## Functions

### toScreenOffset

#### Signature

```kotlin
override fun toScreenOffset(position: GeoPointInterface): Offset?
```

#### Description

Converts a geographical coordinate (`GeoPointInterface`) to a screen pixel `Offset` relative to the top-left corner of the map view.

#### Parameters

| Parameter  | Type                | Description                      |
| :--------- | :------------------ | :------------------------------- |
| `position` | `GeoPointInterface` | The geographical point to convert. |

#### Returns

| Type       | Description                                                              |
| :--------- | :----------------------------------------------------------------------- |
| `Offset?`  | The corresponding `Offset` on the screen, or `null` if the conversion fails. |

---

### fromScreenOffsetSync

#### Signature

```kotlin
override fun fromScreenOffsetSync(offset: Offset): GeoPoint?
```

#### Description

Synchronously converts a screen pixel `Offset` to its corresponding geographical coordinate (`GeoPoint`).

#### Parameters

| Parameter | Type     | Description                  |
| :-------- | :------- | :--------------------------- |
| `offset`  | `Offset` | The screen offset to convert. |

#### Returns

| Type        | Description                                                              |
| :---------- | :----------------------------------------------------------------------- |
| `GeoPoint?` | The corresponding `GeoPoint`, or `null` if the conversion fails. |

---

### fromScreenOffset (from ScreenCoordinate)

#### Signature

```kotlin
fun fromScreenOffset(coordinate: ScreenCoordinate): GeoPoint?
```

#### Description

Converts a Mapbox `ScreenCoordinate` object to its corresponding geographical coordinate (`GeoPoint`).

#### Parameters

| Parameter    | Type               | Description                       |
| :----------- | :----------------- | :-------------------------------- |
| `coordinate` | `ScreenCoordinate` | The `ScreenCoordinate` to convert. |

#### Returns

| Type        | Description                                                              |
| :---------- | :----------------------------------------------------------------------- |
| `GeoPoint?` | The corresponding `GeoPoint`, or `null` if the conversion fails. |

---

### fromScreenOffset (from Offset)

#### Signature

```kotlin
override suspend fun fromScreenOffset(offset: Offset): GeoPoint?
```

#### Description

Asynchronously converts a screen pixel `Offset` to its corresponding geographical coordinate (`GeoPoint`). This is the suspend function override from the `MapViewHolderInterface`.

#### Parameters

| Parameter | Type     | Description                  |
| :-------- | :------- | :--------------------------- |
| `offset`  | `Offset` | The screen offset to convert. |

#### Returns

| Type        | Description                                                              |
| :---------- | :----------------------------------------------------------------------- |
| `GeoPoint?` | The corresponding `GeoPoint`, or `null` if the conversion fails. |

---

### Lifecycle Methods

The following methods are part of the `MapboxLifecycleObserver` interface. They are automatically called by the Mapbox SDK's lifecycle plugin. In this class, they are currently empty implementations (no-op).

-   `onDestroy()`
-   `onLowMemory()`
-   `onStart()`
-   `onStop()`

---

## Example

```kotlin
import androidx.compose.ui.geometry.Offset
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapconductor.core.features.GeoPoint
import kotlinx.coroutines.runBlocking

// Assume mapView and mapboxMap are initialized instances
// val mapView: MapView = ...
// val mapboxMap: MapboxMap = ...

// 1. Create an instance of MapboxMapViewHolder
val mapViewHolder = MapboxMapViewHolder(mapView, mapboxMap)

// 2. Convert a geographical point to a screen offset
val geoPoint = GeoPoint(latitude = 40.7128, longitude = -74.0060) // New York City
val screenOffset = mapViewHolder.toScreenOffset(geoPoint)

screenOffset?.let {
    println("Screen offset for NYC: (${it.x}, ${it.y})")
}

// 3. Convert a screen offset back to a geographical point
val someOffset = Offset(x = 500f, y = 300f)

// Using the synchronous method
val convertedGeoPointSync = mapViewHolder.fromScreenOffsetSync(someOffset)
convertedGeoPointSync?.let {
    println("GeoPoint from sync conversion: Lat ${it.latitude}, Lon ${it.longitude}")
}

// Using the suspend method
runBlocking {
    val convertedGeoPointAsync = mapViewHolder.fromScreenOffset(someOffset)
    convertedGeoPointAsync?.let {
        println("GeoPoint from async conversion: Lat ${it.latitude}, Lon ${it.longitude}")
    }
}
```