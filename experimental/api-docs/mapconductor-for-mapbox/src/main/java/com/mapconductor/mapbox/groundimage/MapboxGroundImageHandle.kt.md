Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

### `MapboxGroundImageHandle`

A data class that serves as a handle for a ground image overlay on a Mapbox map.

This class encapsulates all the necessary identifiers and components required to manage a specific ground image instance, including its associated route, version, cache key, and the underlying Mapbox source and layer IDs.

As a Kotlin `data class`, it automatically provides helpful methods like `equals()`, `hashCode()`, `toString()`, and `copy()`.

### Signature

```kotlin
data class MapboxGroundImageHandle(
    val routeId: String,
    val generation: Long,
    val cacheKey: String,
    val sourceId: String,
    val layerId: String,
    val tileProvider: GroundImageTileProvider,
)
```

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `routeId` | `String` | The unique identifier for the route associated with the ground image. |
| `generation` | `Long` | A version number, often a timestamp, used to distinguish between different generations or updates of the same ground image. |
| `cacheKey` | `String` | A unique key used for caching the ground image tiles and associated data. |
| `sourceId` | `String` | The ID of the Mapbox `ImageSource` used to supply the image data for the overlay. |
| `layerId` | `String` | The ID of the Mapbox `RasterLayer` used to render the ground image on the map. |
| `tileProvider` | `GroundImageTileProvider` | The provider instance responsible for fetching and supplying the image tiles for the overlay. |

### Example

The following example demonstrates how to create an instance of `MapboxGroundImageHandle`. This handle can then be used by other parts of the application to manage the lifecycle of the ground image on the map.

```kotlin
import com.mapconductor.core.groundimage.GroundImageTileProvider
import com.mapconductor.mapbox.groundimage.MapboxGroundImageHandle

// Assume MyTileProvider is a custom implementation of GroundImageTileProvider
class MyTileProvider : GroundImageTileProvider {
    // Implementation details for fetching tiles...
    // For example, it might fetch tiles from a network source or local storage.
}

fun createGroundImage() {
    val tileProvider = MyTileProvider()
    val currentRouteId = "route-sf-to-la-123"
    val imageGeneration = System.currentTimeMillis()

    // Create an instance of the handle to represent a new ground image
    val groundImageHandle = MapboxGroundImageHandle(
        routeId = currentRouteId,
        generation = imageGeneration,
        cacheKey = "ground-image-cache-$currentRouteId-$imageGeneration",
        sourceId = "ground-image-source-$currentRouteId",
        layerId = "ground-image-layer-$currentRouteId",
        tileProvider = tileProvider
    )

    println("Created ground image handle for route: ${groundImageHandle.routeId}")
    
    // This handle can now be passed to a manager class that adds, updates,
    // or removes the corresponding source and layer from the Mapbox map.
}
```