# GoogleMapRasterLayerController

## Signature

```kotlin
class GoogleMapRasterLayerController(
    rasterLayerManager: RasterLayerManagerInterface<TileOverlay> = RasterLayerManager(),
    renderer: GoogleMapRasterLayerOverlayRenderer,
) : RasterLayerController<TileOverlay>(rasterLayerManager, renderer)
```

## Description

The `GoogleMapRasterLayerController` is a specialized controller responsible for managing and displaying raster tile layers on a Google Map. It serves as a concrete implementation of the abstract `RasterLayerController`, tailored for the Google Maps SDK for Android.

This class orchestrates the interaction between the generic layer management logic provided by `RasterLayerManager` and the platform-specific rendering handled by `GoogleMapRasterLayerOverlayRenderer`. It uses `TileOverlay` as the native map object for representing raster layers. By using this controller, you can easily add, remove, and manage various raster data sources (such as XYZ, WMS, or WMTS) on a Google Map.

## Parameters

This class is instantiated through its primary constructor.

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `rasterLayerManager` | `RasterLayerManagerInterface<TileOverlay>` | An optional manager for the collection of raster layers. It handles the lifecycle and state of each layer. If not provided, a default `RasterLayerManager` instance is created. |
| `renderer` | `GoogleMapRasterLayerOverlayRenderer` | A required renderer that handles the creation and display of `TileOverlay` objects on the associated Google Map. This object bridges the controller's logic with the map view. |

## Returns

An instance of `GoogleMapRasterLayerController`, which can be used to manage raster layers on a Google Map.

## Example

The following example demonstrates how to initialize and use the `GoogleMapRasterLayerController` to add an OpenStreetMap tile layer to a Google Map. This code would typically be placed within the `onMapReady` callback where the `GoogleMap` object is available.

```kotlin
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.mapconductor.core.raster.XyzRasterLayer
import com.mapconductor.googlemaps.raster.GoogleMapRasterLayerController
import com.mapconductor.googlemaps.raster.GoogleMapRasterLayerOverlayRenderer

class MyMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var rasterLayerController: GoogleMapRasterLayerController
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 1. Create a renderer for the Google Map.
        // This class is responsible for drawing the tile overlays on the map.
        val renderer = GoogleMapRasterLayerOverlayRenderer(googleMap)

        // 2. Instantiate the controller with the renderer.
        // We will use the default RasterLayerManager provided by the constructor.
        rasterLayerController = GoogleMapRasterLayerController(renderer = renderer)

        // 3. Define a raster layer to add (e.g., an XYZ tile layer).
        val openStreetMapLayer = XyzRasterLayer(
            id = "osm-standard-layer",
            url = "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
            displayName = "OpenStreetMap"
        )

        // 4. Use the controller to add the layer to the map.
        // The controller will use the renderer to create and display the TileOverlay.
        rasterLayerController.addLayer(openStreetMapLayer)
    }
}
```