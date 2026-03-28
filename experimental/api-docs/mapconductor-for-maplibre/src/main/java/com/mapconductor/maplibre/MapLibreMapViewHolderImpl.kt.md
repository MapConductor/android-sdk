Of course! Here is the high-quality SDK documentation for the provided code snippet.

# `MapLibreMapViewHolder`

An internal class that acts as a container for the MapLibre `MapView` and `MapLibreMap` instances. It provides essential utility functions for converting between geographical coordinates and screen pixel coordinates. This class implements the `MapLibreMapViewHolderInterface`.

> **Note:** This is an `internal` class and is not intended for direct use by external consumers of the SDK. It is used internally by the map conductor framework.

## Constructor

### Signature

```kotlin
internal class MapLibreMapViewHolder(
    override val mapView: MapView,
    override val map: MapLibreMap,
)
```

### Description

Creates an instance of `MapLibreMapViewHolder`.

### Parameters

| Parameter | Type          | Description                                                              |
| :-------- | :------------ | :----------------------------------------------------------------------- |
| `mapView` | `MapView`     | The Android `MapView` instance from the MapLibre library.                |
| `map`     | `MapLibreMap` | The core `MapLibreMap` object used for map interactions and projections. |

---

## Functions

### `getController`

Retrieves the `MapLibreViewController` associated with this map view holder. The controller is responsible for managing map state and interactions.

#### Signature

```kotlin
override fun getController(): MapLibreViewController?
```

#### Returns

| Type                      | Description                                                                    |
| :------------------------ | :----------------------------------------------------------------------------- |
| `MapLibreViewController?` | The associated view controller instance, or `null` if one has not been set yet. |

---

### `toScreenOffset`

Converts a geographical coordinate (`GeoPointInterface`) into a screen pixel coordinate (`Offset`) relative to the top-left corner of the `MapView`.

#### Signature

```kotlin
override fun toScreenOffset(position: GeoPointInterface): Offset?
```

#### Parameters

| Parameter  | Type                | Description                                                        |
| :--------- | :------------------ | :----------------------------------------------------------------- |
| `position` | `GeoPointInterface` | The geographical coordinate (latitude and longitude) to be converted. |

#### Returns

| Type       | Description                                                                                             |
| :--------- | :------------------------------------------------------------------------------------------------------ |
| `Offset?`  | An `Offset` object containing the `x` and `y` pixel coordinates on the screen. Returns `null` if the conversion fails. |

#### Example

```kotlin
// Assuming 'viewHolder' is an instance of MapLibreMapViewHolder
// and 'someGeoPoint' is an object implementing GeoPointInterface.
val screenOffset: Offset? = viewHolder.toScreenOffset(someGeoPoint)

screenOffset?.let {
    println("Screen coordinates: x=${it.x}, y=${it.y}")
}
```

---

### `fromScreenOffset`

Asynchronously converts a screen pixel coordinate (`Offset`) into a geographical coordinate (`GeoPoint`). This is a `suspend` function and should be called from a coroutine or another `suspend` function.

#### Signature

```kotlin
override suspend fun fromScreenOffset(offset: Offset): GeoPoint?
```

#### Parameters

| Parameter | Type     | Description                                      |
| :-------- | :------- | :----------------------------------------------- |
| `offset`  | `Offset` | The screen coordinate (x, y pixels) to convert. |

#### Returns

| Type        | Description                                                                                                    |
| :---------- | :------------------------------------------------------------------------------------------------------------- |
| `GeoPoint?` | A `GeoPoint` object representing the geographical coordinate at the specified screen location, or `null` if the conversion fails. |

#### Example

```kotlin
// Inside a coroutine scope
viewModelScope.launch {
    val screenTapOffset = Offset(250f, 400f)
    val geoPoint: GeoPoint? = viewHolder.fromScreenOffset(screenTapOffset)

    geoPoint?.let {
        println("Tapped at: lat=${it.latitude}, lon=${it.longitude}")
    }
}
```

---

### `fromScreenOffsetSync`

Synchronously converts a screen pixel coordinate (`Offset`) into a geographical coordinate (`GeoPoint`).

> **Warning:** This function may block the calling thread. Prefer the `suspend` version, `fromScreenOffset`, when working with coroutines to avoid blocking the main thread.

#### Signature

```kotlin
override fun fromScreenOffsetSync(offset: Offset): GeoPoint?
```

#### Parameters

| Parameter | Type     | Description                                      |
| :-------- | :------- | :----------------------------------------------- |
| `offset`  | `Offset` | The screen coordinate (x, y pixels) to convert. |

#### Returns

| Type        | Description                                                                                                    |
| :---------- | :------------------------------------------------------------------------------------------------------------- |
| `GeoPoint?` | A `GeoPoint` object representing the geographical coordinate at the specified screen location, or `null` if the conversion fails. |

---

### `setController`

Sets the `MapLibreViewController` for this view holder. This method is used internally to establish the link between the view holder and its controller.

#### Signature

```kotlin
internal fun setController(ctrl: MapLibreViewController)
```

#### Parameters

| Parameter | Type                     | Description                                                    |
| :-------- | :----------------------- | :------------------------------------------------------------- |
| `ctrl`    | `MapLibreViewController` | The controller instance to associate with this view holder. |