# MarkerLayer

The `MarkerLayer` class encapsulates the logic for managing and rendering a collection of markers as a single layer on a MapLibre map. It combines a `GeoJsonSource` to hold the marker data and a `SymbolLayer` to define their visual appearance.

This class is responsible for creating and configuring the necessary MapLibre `SymbolLayer` and `GeoJsonSource`, and provides a `draw` method to update the map with a list of marker entities.

## Constructor

### Signature

```kotlin
open class MarkerLayer(
    open val sourceId: String,
    open val layerId: String,
)
```

### Description

Creates a new instance of `MarkerLayer` with unique identifiers for its underlying source and layer.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `sourceId` | `String` | A unique identifier for the `GeoJsonSource` that will contain the marker data. |
| `layerId` | `String` | A unique identifier for the `SymbolLayer` that will render the markers. |

## Properties

### layer: SymbolLayer

The configured MapLibre `SymbolLayer` used to render the markers. This layer is pre-configured to pull styling properties (like icon image, anchor, and Z-index) directly from the properties of the GeoJSON features.

### source: GeoJsonSource

The MapLibre `GeoJsonSource` that holds the marker data as a `FeatureCollection`. The `draw` method updates this source with the latest marker information.

## Methods

### draw

#### Signature

```kotlin
fun draw(
    entities: List<MarkerEntityInterface<Feature>>,
    style: org.maplibre.android.maps.Style,
)
```

#### Description

Updates the marker layer with a new set of entities to be displayed on the map. This method filters for visible entities, converts them into a `FeatureCollection`, and updates the `GeoJsonSource` associated with the map's current style.

It includes robust logic to handle cases where the source may have been detached (e.g., after a style reload) by attempting to re-add it if necessary before updating the data. Any failures during the update process are logged without crashing the application.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `entities` | `List<MarkerEntityInterface<Feature>>` | A list of marker entities to render. Only entities where `visible` is `true` and `marker` is not `null` will be drawn. |
| `style` | `org.maplibre.android.maps.Style` | The active MapLibre `Style` object to which the source and layer are attached. |

#### Returns

This method does not return a value.

## Example

```kotlin
// Assume maplibreMap and style are available from a MapLibre callback,
// and you have a list of marker entities.

// 1. Create an instance of MarkerLayer
val markerLayer = MarkerLayer(sourceId = "my-marker-source", layerId = "my-marker-layer")

// 2. Add the source and layer to the map style (typically done once after the map is ready)
style.addSource(markerLayer.source)
style.addLayer(markerLayer.layer)

// 3. Prepare your marker data.
//    Each feature must have properties that the SymbolLayer is configured to use.
//    (This is a simplified representation of MarkerEntityInterface and Feature creation)
val markerFeature1 = Feature.fromGeometry(Point.fromLngLat(10.0, 52.0))
markerFeature1.addStringProperty(MapLibreMarkerOverlayRenderer.Prop.ICON_ID, "icon-id-1")
// ... add other required properties like Z_INDEX and ICON_ANCHOR

val markerFeature2 = Feature.fromGeometry(Point.fromLngLat(10.1, 52.1))
markerFeature2.addStringProperty(MapLibreMarkerOverlayRenderer.Prop.ICON_ID, "icon-id-2")
// ... add other required properties

// Assuming a concrete implementation of MarkerEntityInterface
val myEntities: List<MarkerEntityInterface<Feature>> = listOf(
    MyMarkerEntity(visible = true, marker = markerFeature1),
    MyMarkerEntity(visible = true, marker = markerFeature2),
    MyMarkerEntity(visible = false, marker = someOtherFeature) // This one will be filtered out
)

// 4. Call draw to render the markers on the map.
//    This would typically be called whenever your marker data changes.
markerLayer.draw(myEntities, style)
```