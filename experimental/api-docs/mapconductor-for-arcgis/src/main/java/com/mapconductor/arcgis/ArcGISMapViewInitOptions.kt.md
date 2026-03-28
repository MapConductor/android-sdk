# ArcGISMapViewInitOptions

A data class that holds configuration options for initializing an `ArcGISMapView`. It allows for the customization of the map's initial appearance and 3D capabilities, such as the basemap style and elevation data sources.

## Signature

```kotlin
data class ArcGISMapViewInitOptions(
    val basemapStyle: BasemapStyle,
    val elevationSources: List<String>,
)
```

## Parameters

| Parameter          | Type                  | Description                                                                                                                            |
| :----------------- | :-------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `basemapStyle`     | `BasemapStyle`        | The initial style for the map's basemap. This determines the visual theme, such as imagery, streets, or topography.                      |
| `elevationSources` | `List<String>`        | A list of URLs pointing to elevation services. These sources are used to create a 3D surface for the map, enabling terrain visualization. |

## Example

The following example demonstrates how to create an instance of `ArcGISMapViewInitOptions` to configure a map with an imagery basemap and a 3D terrain surface.

```kotlin
import com.arcgismaps.mapping.BasemapStyle
import com.mapconductor.arcgis.map.ArcGISMapViewInitOptions

// Define the URL for the elevation data source
val elevationSourceUrl = "https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer"

// Create an instance of ArcGISMapViewInitOptions
val mapViewOptions = ArcGISMapViewInitOptions(
    basemapStyle = BasemapStyle.ARCGIS_IMAGERY,
    elevationSources = listOf(elevationSourceUrl)
)

// These options can then be passed to an ArcGISMapView component during its initialization.
```