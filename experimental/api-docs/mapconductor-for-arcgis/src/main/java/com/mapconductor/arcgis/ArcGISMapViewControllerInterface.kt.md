Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# ArcGISMapViewControllerInterface

The primary interface for controlling and interacting with an ArcGIS map view.

## Description

`ArcGISMapViewControllerInterface` serves as the main controller for an ArcGIS map instance. It extends the generic `MapViewControllerInterface` and incorporates a comprehensive set of capabilities for managing various map features and overlays.

This interface allows you to:
- Manage the lifecycle and core properties of the map view.
- Add, remove, and update markers, polylines, polygons, circles, ground images, and raster layers.
- Control ArcGIS-specific features, such as the map's design type (basemap).
- Listen for changes to the map's design type.

It inherits functionality from the following interfaces:
- `MapViewControllerInterface`
- `MarkerCapableInterface`
- `PolylineCapableInterface`
- `PolygonCapableInterface`
- `CircleCapableInterface`
- `GroundImageCapableInterface`
- `RasterLayerCapableInterface`

---

## Type Aliases

### ArcGISDesignTypeChangeHandler

A function type that defines the signature for a listener that is invoked when the map's design type changes.

**Signature**
```kotlin
typealias ArcGISDesignTypeChangeHandler = (ArcGISDesignTypeInterface) -> Unit
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `designType` | `ArcGISDesignTypeInterface` | The new design type that has been applied to the map. |

---

## Functions

### setMapDesignType

Sets the visual design (basemap) of the map.

**Signature**
```kotlin
fun setMapDesignType(value: ArcGISDesignTypeInterface)
```

**Description**

This function updates the current basemap of the ArcGIS map to the specified design type. Use this to dynamically change the map's appearance, for example, switching between satellite, street, and topographic views.

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `value` | `ArcGISDesignTypeInterface` | The new map design type to apply. |

**Returns**

This method does not return a value.

**Example**

```kotlin
// Assuming 'mapController' is an instance of ArcGISMapViewControllerInterface
// and 'ArcGISTopographicDesign' implements ArcGISDesignTypeInterface.

val newDesign = ArcGISTopographicDesign()
mapController.setMapDesignType(newDesign)
```

---

### setMapDesignTypeChangeListener

Registers a listener to be notified when the map's design type changes.

**Signature**
```kotlin
fun setMapDesignTypeChangeListener(listener: ArcGISDesignTypeChangeHandler)
```

**Description**

This function sets a callback that will be executed whenever the map's design type is updated, either programmatically via `setMapDesignType` or through other UI interactions. This is useful for reacting to basemap changes within your application.

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `listener` | `ArcGISDesignTypeChangeHandler` | A lambda or function reference that will be invoked when the map design changes. The new `ArcGISDesignTypeInterface` is passed as an argument to this listener. |

**Returns**

This method does not return a value.

**Example**

```kotlin
// Assuming 'mapController' is an instance of ArcGISMapViewControllerInterface.

mapController.setMapDesignTypeChangeListener { newDesignType ->
    // Log the change or update the UI to reflect the new basemap.
    println("Map design changed to: ${newDesignType::class.simpleName}")
    
    // Example: Update a UI label with the name of the new design.
    updateBasemapLabel(newDesignType.name)
}
```