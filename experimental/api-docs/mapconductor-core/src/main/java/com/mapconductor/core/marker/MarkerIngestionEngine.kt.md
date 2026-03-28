Excellent. Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

***

# MarkerIngestionEngine

Provides shared, low-level logic for ingesting and diffing marker data.

## Description

The `MarkerIngestionEngine` is a singleton object responsible for processing marker data updates. It compares a new list of `MarkerState` objects against the current state managed by a `MarkerManager`. By performing this diff, it calculates the necessary additions, updates, and removals.

The engine delegates the actual rendering operations (creating, modifying, or deleting marker visuals on the map) to a platform-specific `MarkerOverlayRendererInterface`. It also contains the core logic for determining whether a marker should be rendered natively or as part of a more performant tile layer, based on provided configuration and rules.

This centralized engine ensures consistent marker handling behavior across different map SDK implementations.

## Nested Classes

### `data class Result`

A data class that encapsulates the outcome of the `ingest` operation, specifically concerning the state of tiled markers.

| Property           | Type      | Description                                                                                             |
| ------------------ | --------- | ------------------------------------------------------------------------------------------------------- |
| `tiledDataChanged` | `Boolean` | Is `true` if the set of tiled markers was modified (e.g., a marker was added to, or removed from, tiling). |
| `hasTiledMarkers`  | `Boolean` | Is `true` if there are any markers currently designated as tiled after the operation completes.           |

## Functions

### ingest`<ActualMarker>`

Asynchronously processes a list of `MarkerState` objects to update the markers displayed on the map.

**Signature**

```kotlin
suspend fun <ActualMarker : Any> ingest(
    data: List<MarkerState>,
    markerManager: MarkerManager<ActualMarker>,
    renderer: MarkerOverlayRendererInterface<ActualMarker>,
    defaultMarkerIcon: BitmapIcon,
    tilingEnabled: Boolean,
    tiledMarkerIds: MutableSet<String>,
    shouldTile: (MarkerState) -> Boolean,
): Result
```

**Description**

This function is the core of the marker update process. It performs a diff operation between the incoming `data` list and the current markers tracked by the `markerManager`. It identifies which markers are new, which have been updated, and which should be removed.

Based on the `tilingEnabled` flag and the `shouldTile` predicate, it also determines whether a marker should be rendered natively or as part of a tile layer. The function then uses the provided `renderer` to apply these changes to the map and updates the `markerManager` and `tiledMarkerIds` set to reflect the new state.

As a `suspend` function, it must be called from a coroutine or another suspend function.

**Parameters**

| Parameter           | Type                                           | Description                                                                                                                            |
| ------------------- | ---------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `data`              | `List<MarkerState>`                            | The complete, desired list of marker states to be displayed on the map.                                                                |
| `markerManager`     | `MarkerManager<ActualMarker>`                  | The manager that holds the current state of all marker entities. This function will update it to reflect the new state.                |
| `renderer`          | `MarkerOverlayRendererInterface<ActualMarker>` | The platform-specific renderer responsible for adding, changing, and removing the actual marker objects (`ActualMarker`) on the map view. |
| `defaultMarkerIcon` | `BitmapIcon`                                   | The fallback icon to use for markers that do not have a specific icon defined in their `MarkerState`.                                  |
| `tilingEnabled`     | `Boolean`                                      | A master switch to enable or disable the marker tiling functionality. If `false`, all markers will be rendered natively.                |
| `tiledMarkerIds`    | `MutableSet<String>`                           | A mutable set containing the IDs of markers that are currently tiled. This function will read from and write to this set.              |
| `shouldTile`        | `(MarkerState) -> Boolean`                     | A predicate function called for each marker to decide if it should be tiled. This is only invoked if `tilingEnabled` is `true`.         |

**Returns**

A `Result` object containing two booleans:
*   `tiledDataChanged`: Indicates if the composition of the tiled marker set has changed.
*   `hasTiledMarkers`: Indicates if any markers are designated for tiling after the operation.

**Example**

This example demonstrates how to call the `ingest` function within a coroutine scope.

```kotlin
import kotlinx.coroutines.runBlocking

// Assume these are defined elsewhere and implemented for a specific map SDK
// val markerManager: MarkerManager<MapboxMarker> = ...
// val renderer: MarkerOverlayRendererInterface<MapboxMarker> = ...
// val defaultIcon: BitmapIcon = ...
// val newMarkerStates: List<MarkerState> = ...

// A mutable set to track which markers are part of a tile layer
val tiledMarkerIds = mutableSetOf<String>()

fun processMarkerUpdates() = runBlocking {
    // Define the logic for when a marker should be tiled.
    // For example, tile markers that are beyond a certain zoom level or have a specific property.
    val shouldTilePredicate: (MarkerState) -> Boolean = { markerState ->
        // Example logic: tile if a custom property 'isLowImportance' is true
        markerState.customProperties["isLowImportance"] as? Boolean ?: false
    }

    // Call the ingestion engine
    val result = MarkerIngestionEngine.ingest(
        data = newMarkerStates,
        markerManager = markerManager,
        renderer = renderer,
        defaultMarkerIcon = defaultIcon,
        tilingEnabled = true,
        tiledMarkerIds = tiledMarkerIds,
        shouldTile = shouldTilePredicate
    )

    // The engine has updated the markerManager and renderer.
    // Now, we can use the result to trigger other actions, like refreshing a tile layer.
    if (result.tiledDataChanged) {
        println("Tiled marker data has changed. Refreshing tile layer...")
        // ... logic to bust tile cache and refresh the raster layer
    }

    println("Ingestion complete. Tiled markers exist: ${result.hasTiledMarkers}")
    println("Current tiled marker IDs: $tiledMarkerIds")
}
```