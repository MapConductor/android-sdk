Of course. Here is the high-quality SDK documentation for the given Kotlin code snippet, incorporating the feedback provided.

---

# ArcGISMapViewScope

## Signature
```kotlin
class ArcGISMapViewScope : MapViewScope()
```

## Description
`ArcGISMapViewScope` is a specialized scope class designed for configuring an ArcGIS map view. It extends the base `MapViewScope`, inheriting all its common map configuration capabilities, and adds functionalities exclusive to the ArcGIS Maps SDK.

This class provides a context-specific DSL (Domain-Specific Language) for map setup. When you configure an ArcGIS map, you operate within this scope, gaining access to both shared map properties (like camera position) and ArcGIS-specific features (like basemap styles or 3D settings).

## Properties
This class inherits all properties from its parent, `MapViewScope`. It also serves as the designated place for properties unique to the ArcGIS implementation.

| Property (Example) | Type | Description |
| :--- | :--- | :--- |
| `basemapStyle` | `ArcGISBasemapStyle` | Gets or sets the visual style of the ArcGIS basemap (e.g., Streets, Imagery, Topographic). |
| `is3DModeEnabled` | `Boolean` | A flag to enable or disable the 3D scene view for the map. |

*Note: The properties listed above are examples of ArcGIS-specific functionalities that would be defined in this class.*

## Methods
This class inherits all methods from `MapViewScope`. ArcGIS-specific methods for controlling the map's behavior are defined here.

## Example
The `ArcGISMapViewScope` is typically used within a map configuration block, such as a Jetpack Compose `ArcGISMap` composable. The lambda provided to the composable has `ArcGISMapViewScope` as its receiver, giving you direct access to its properties and methods.

```kotlin
import androidx.compose.runtime.Composable
import com.mapconductor.arcgis.map.ArcGISMap
import com.mapconductor.arcgis.map.model.ArcGISBasemapStyle
import com.mapconductor.core.model.LatLng
import com.mapconductor.core.model.CameraPosition

@Composable
fun MyMapScreen() {
    // The lambda block for ArcGISMap operates within the ArcGISMapViewScope.
    ArcGISMap(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- Inherited from MapViewScope ---
        // Set the initial camera position using a method from the base scope.
        setInitialCameraPosition(
            CameraPosition(
                target = LatLng(34.0522, -118.2437), // Los Angeles
                zoom = 12.0
            )
        )

        // --- Specific to ArcGISMapViewScope ---
        // Set an ArcGIS-specific basemap style.
        // This property or method would not be available in other map SDK scopes.
        basemapStyle = ArcGISBasemapStyle.ARCGIS_NAVIGATION

        // Enable 3D view, another hypothetical ArcGIS-specific feature.
        is3DModeEnabled = true
    }
}
```