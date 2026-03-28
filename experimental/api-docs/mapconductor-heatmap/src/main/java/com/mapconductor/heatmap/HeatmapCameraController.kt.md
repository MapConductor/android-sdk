Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# HeatmapCameraController

## Description

A specialized controller that links map camera movements to a `HeatmapTileRenderer`. Its primary function is to listen for changes in the map's camera position and update the heatmap renderer with the new zoom level. This allows the heatmap's appearance (e.g., tile resolution or intensity) to be adjusted dynamically as the user zooms in or out.

This controller implements `OverlayControllerInterface` but is designed to be lightweight. It does not manage individual data points, state, or click events, focusing solely on camera event handling.

## Signature

```kotlin
class HeatmapCameraController(
    private val renderer: HeatmapTileRenderer,
) : OverlayControllerInterface<Unit, Unit, Unit>
```

## Constructor

### `HeatmapCameraController(renderer: HeatmapTileRenderer)`

Creates an instance of `HeatmapCameraController`.

#### Parameters

| Name     | Type                  | Description                                                              |
| :------- | :-------------------- | :----------------------------------------------------------------------- |
| `renderer` | `HeatmapTileRenderer` | The heatmap renderer instance that will be updated with camera zoom changes. |

---

## Methods

### `onCameraChanged`

Called by the map framework whenever the camera position changes. This method extracts the zoom level from the `mapCameraPosition` and passes it to the associated `HeatmapTileRenderer` to update its state.

#### Signature

```kotlin
override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)
```

#### Parameters

| Name                | Type                | Description                                        |
| :------------------ | :------------------ | :------------------------------------------------- |
| `mapCameraPosition` | `MapCameraPosition` | The new position and zoom level of the map camera. |

---

## Interface Methods

The following methods from `OverlayControllerInterface` are implemented but have no effect, as this controller's scope is limited to camera handling.

| Method                                | Description                                      |
| :------------------------------------ | :----------------------------------------------- |
| `add(data: List<Unit>)`               | No-op. Does not add data to the overlay.         |
| `update(state: Unit)`                 | No-op. Does not update the overlay's state.      |
| `clear()`                             | No-op. Does not clear any data.                  |
| `find(position: GeoPointInterface)`   | No-op. Always returns `null`.                    |
| `destroy()`                           | No-op. No native resources to clean up.          |

---

## Example

The following example demonstrates how to initialize and use the `HeatmapCameraController` with a `HeatmapTileRenderer`.

```kotlin
// Assume 'heatmapDataPoints' is a list of geographical points for the heatmap
// and 'context' is an Android Context.

// 1. Initialize the components needed for the heatmap renderer.
//    (HeatmapTileProvider is a hypothetical class that provides tile data).
val tileProvider = HeatmapTileProvider(heatmapDataPoints)
val heatmapRenderer = HeatmapTileRenderer(context, tileProvider)

// 2. Create the camera controller, linking it to the renderer.
val heatmapCameraController = HeatmapCameraController(heatmapRenderer)

// 3. Add the controller to the map's overlay management system.
//    (This is a hypothetical example of how it might be used with a map view).
mapView.overlayManager.addController(heatmapCameraController)

// Now, whenever the user zooms or pans the map, the map framework will call
// heatmapCameraController.onCameraChanged. This automatically updates the
// heatmapRenderer with the new zoom level, allowing it to render appropriate tiles.
```