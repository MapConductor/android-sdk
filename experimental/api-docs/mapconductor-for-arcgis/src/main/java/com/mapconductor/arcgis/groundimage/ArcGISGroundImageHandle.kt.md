# ArcGISGroundImageHandle

## Signature

```kotlin
data class ArcGISGroundImageHandle(
    val routeId: String,
    val generation: Long,
    val cacheKey: String,
    val tileProvider: GroundImageTileProvider,
    val layer: WebTiledLayer,
)
```

## Description

The `ArcGISGroundImageHandle` is a data class that serves as a container for all the components and metadata associated with a specific ground imagery layer displayed on an ArcGIS map.

This handle conveniently groups the identifying information (such as `routeId` and `generation`), the caching key, the custom tile provider, and the actual ArcGIS `WebTiledLayer` object. It is typically returned when a ground imagery layer is created and added to the map, allowing for easy management and reference to the layer and its related resources.

## Parameters

| Parameter      | Type                      | Description                                                                                                |
| :------------- | :------------------------ | :--------------------------------------------------------------------------------------------------------- |
| `routeId`      | `String`                  | The unique identifier for the route to which the ground imagery belongs.                                   |
| `generation`   | `Long`                    | A version number for the imagery data. This can be used to manage updates and invalidate caches.           |
| `cacheKey`     | `String`                  | A unique key used for caching the ground imagery tiles, ensuring efficient retrieval.                       |
| `tileProvider` | `GroundImageTileProvider` | The custom tile provider instance responsible for fetching and supplying the ground image tiles.             |
| `layer`        | `WebTiledLayer`           | The ArcGIS `WebTiledLayer` instance that is added to the map to render the ground imagery.                 |

## Example

The following example demonstrates how to create an instance of `ArcGISGroundImageHandle`.

```kotlin
import com.arcgismaps.mapping.layers.WebTiledLayer
import com.mapconductor.core.groundimage.GroundImageTileProvider

// Assume these are initialized elsewhere in your application
val myTileProvider = GroundImageTileProvider(/*...*/)
val myWebTiledLayer = WebTiledLayer(/*...*/)

// Create an instance of ArcGISGroundImageHandle
val groundImageHandle = ArcGISGroundImageHandle(
    routeId = "route-12345",
    generation = 1678886400L,
    cacheKey = "route-12345-g1678886400",
    tileProvider = myTileProvider,
    layer = myWebTiledLayer
)

// You can now use the handle to access properties of the ground image layer
println("Managing layer for route: ${groundImageHandle.routeId}")
// Example: map.operationalLayers.add(groundImageHandle.layer)
```