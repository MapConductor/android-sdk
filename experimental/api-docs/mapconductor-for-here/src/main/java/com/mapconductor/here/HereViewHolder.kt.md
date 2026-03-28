Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

# Class `HereViewHolder`

A container class that holds instances of the HERE SDK's `MapView` and `MapScene`. It implements the `MapViewHolderInterface` to provide a standardized way of interacting with the map, specifically for converting between geographical coordinates and screen coordinates.

This class acts as a bridge between the generic `MapViewHolderInterface` and the specific implementation details of the HERE Maps SDK.

## Signature

```kotlin
class HereViewHolder(
    override val mapView: MapView,
    override val map: MapScene,
) : MapViewHolderInterface<MapView, MapScene>
```

## Parameters

| Parameter | Type      | Description                               |
| :-------- | :-------- | :---------------------------------------- |
| `mapView` | `MapView` | The HERE SDK `MapView` instance.          |
| `map`     | `MapScene`  | The HERE SDK `MapScene` instance for the map. |

---

## Methods

### `toScreenOffset`

Converts a geographical coordinate (`GeoPointInterface`) into a screen coordinate (`Offset`) relative to the `MapView`.

#### Signature

```kotlin
override fun toScreenOffset(position: GeoPointInterface): Offset?
```

#### Description

This function takes a geographical point and calculates its corresponding pixel offset on the screen. If the provided geographical point is not currently visible on the map view, this function will return `null`.

#### Parameters

| Parameter  | Type                | Description                                  |
| :--------- | :------------------ | :------------------------------------------- |
| `position` | `GeoPointInterface` | The geographical coordinate to be converted. |

#### Returns

**`Offset?`**: An `Offset` object containing the x and y screen coordinates, or `null` if the geographical point is outside the visible map area.

#### Example

```kotlin
val geoPoint: GeoPointInterface = GeoPoint(52.5200, 13.4050) // Berlin
val screenOffset: Offset? = hereViewHolder.toScreenOffset(geoPoint)

if (screenOffset != null) {
    println("Screen coordinates: x=${screenOffset.x}, y=${screenOffset.y}")
} else {
    println("The location is not visible on the screen.")
}
```

---

### `fromScreenOffset`

Asynchronously converts a screen coordinate (`Offset`) into a geographical coordinate (`GeoPoint`).

#### Signature

```kotlin
override suspend fun fromScreenOffset(offset: Offset): GeoPoint?
```

#### Description

This suspend function takes a pixel offset from the screen and converts it into the corresponding geographical coordinate on the map. Since this is a `suspend` function, it should be called from a coroutine or another suspend function.

#### Parameters

| Parameter | Type     | Description                                       |
| :-------- | :------- | :------------------------------------------------ |
| `offset`  | `Offset` | The screen coordinate (`x`, `y`) to be converted. |

#### Returns

**`GeoPoint?`**: A `GeoPoint` object representing the geographical coordinate, or `null` if the conversion is not possible (e.g., the offset is outside the map view).

#### Example

```kotlin
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

// Assuming this is called within a Composable or an Activity/Fragment
lifecycleScope.launch {
    val screenTapOffset = Offset(400f, 600f)
    val geoPoint: GeoPoint? = hereViewHolder.fromScreenOffset(screenTapOffset)

    geoPoint?.let {
        println("Tapped at: lat=${it.latitude}, lon=${it.longitude}")
    }
}
```

---

### `fromScreenOffsetSync`

Synchronously converts a screen coordinate (`Offset`) into a geographical coordinate (`GeoPoint`).

#### Signature

```kotlin
override fun fromScreenOffsetSync(offset: Offset): GeoPoint?
```

#### Description

This function takes a pixel offset from the screen and converts it into the corresponding geographical coordinate on the map. Unlike `fromScreenOffset`, this operation is performed synchronously.

#### Parameters

| Parameter | Type     | Description                                       |
| :-------- | :------- | :------------------------------------------------ |
| `offset`  | `Offset` | The screen coordinate (`x`, `y`) to be converted. |

#### Returns

**`GeoPoint?`**: A `GeoPoint` object representing the geographical coordinate, or `null` if the conversion is not possible.

#### Example

```kotlin
val screenTapOffset = Offset(400f, 600f)
val geoPoint: GeoPoint? = hereViewHolder.fromScreenOffsetSync(screenTapOffset)

geoPoint?.let {
    println("Tapped at: lat=${it.latitude}, lon=${it.longitude}")
}
```