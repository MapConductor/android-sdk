# MapboxMapViewControllerInterface

## Description

The `MapboxMapViewControllerInterface` provides a comprehensive interface for controlling and interacting with a Mapbox map view. It serves as a high-level controller that unifies various map functionalities into a single, easy-to-use API.

This interface extends several capability interfaces, enabling the management of various map elements such as markers, polylines, polygons, circles, ground images, and raster layers. In addition to these common map features, it offers methods specific to the Mapbox implementation, such as changing the map's visual style (design type) and listening for those changes.

---

## Methods

### setMapDesignType

Sets or updates the visual design (style) of the map. This allows for dynamically changing the map's appearance, for example, switching between street, satellite, or dark mode styles.

**Signature**
```kotlin
fun setMapDesignType(value: MapboxDesignType)
```

**Parameters**

| Parameter | Type | Description |
|-----------|--------------------|---------------------------------------------|
| `value` | `MapboxDesignType` | The desired map design type to apply. |

**Returns**

This method does not return any value.

---

### setMapDesignTypeChangeListener

Registers a listener to receive notifications when the map's design type has finished changing. This is useful for performing actions after a new map style has been fully loaded and rendered.

**Signature**
```kotlin
fun setMapDesignTypeChangeListener(listener: MapboxMapDesignTypeChangeHandler)
```

**Parameters**

| Parameter | Type | Description |
|-----------|-----------------------------------|------------------------------------------------------------------------------------|
| `listener` | `MapboxMapDesignTypeChangeHandler` | An object that implements the `MapboxMapDesignTypeChangeHandler` to handle the change event. |

**Returns**

This method does not return any value.

---

## Example

The following example demonstrates how to set a listener for map design changes and then trigger a change to a new design type.

```kotlin
// Assume 'mapboxController' is an instance of MapboxMapViewControllerInterface
lateinit var mapboxController: MapboxMapViewControllerInterface

// 1. Register a listener to be notified when the map style has finished loading.
mapboxController.setMapDesignTypeChangeListener(object : MapboxMapDesignTypeChangeHandler {
    override fun onMapDesignTypeChanged(newType: MapboxDesignType) {
        // This block is executed after the new map style is fully loaded.
        println("Map design successfully changed to: ${newType.name}")
        // You can now safely interact with the new map style.
    }
})

// 2. Set the map to a new design type (e.g., Satellite).
// This will trigger the listener's onMapDesignTypeChanged method upon completion.
println("Changing map design to Satellite...")
mapboxController.setMapDesignType(MapboxDesignType.SATELLITE)
```