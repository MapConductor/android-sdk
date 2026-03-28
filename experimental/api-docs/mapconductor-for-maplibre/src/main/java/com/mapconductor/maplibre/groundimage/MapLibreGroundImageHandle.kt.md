# MapLibreGroundImageHandle

A data class that encapsulates all the necessary information and resources for managing a ground image layer on a MapLibre map.

This handle acts as a container for identifiers, caching keys, and the data provider required to display and manage the lifecycle of a ground image. It is typically returned when a ground image is added to the map and can be used later to reference or remove it.

## Signature

```kotlin
data class MapLibreGroundImageHandle(
    val routeId: String,
    val generation: Long,
    val cacheKey: String,
    val sourceId: String,
    val layerId: String,
    val tileProvider: GroundImageTileProvider,
)
```

## Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `routeId` | `String` | A unique identifier for the route or entity associated with the ground image. |
| `generation` | `Long` | A version number for the ground image data, often used for cache invalidation and tracking updates. |
| `cacheKey` | `String` | A unique key used for caching the ground image tiles. |
| `sourceId` | `String` | The ID of the MapLibre `Source` that provides the data for the ground image layer. |
| `layerId` | `String` | The ID of the MapLibre `Layer` that renders the ground image on the map. |
| `tileProvider` | `GroundImageTileProvider` | The provider instance responsible for fetching and supplying the ground image tiles. |

## Example

The following example demonstrates how to create an instance of `MapLibreGroundImageHandle`.

```kotlin
import com.mapconductor.core.groundimage.GroundImageTileProvider
import com.mapconductor.maplibre.groundimage.MapLibreGroundImageHandle

// Assume 'myTileProvider' is an existing instance of a class
// that implements the GroundImageTileProvider interface.
val myTileProvider: GroundImageTileProvider = /* ... implementation ... */

// Create an instance of MapLibreGroundImageHandle
val groundImageHandle = MapLibreGroundImageHandle(
    routeId = "route-abc-456",
    generation = System.currentTimeMillis(),
    cacheKey = "route-abc-456-ground-image-v2",
    sourceId = "ground-image-source-456",
    layerId = "ground-image-layer-456",
    tileProvider = myTileProvider
)

// The handle can now be used to manage the ground image layer,
// for example, to remove it from the map later.
println("Created handle for layer: ${groundImageHandle.layerId}")
```