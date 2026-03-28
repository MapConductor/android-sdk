Of course! Here is the high-quality SDK documentation for the provided code snippet.

# Interface: `HereMapViewControllerInterface`

Provides the main interface for controlling and interacting with a HERE Map view.

## Description

The `HereMapViewControllerInterface` serves as a central controller for a map instance. It aggregates functionalities for managing various map objects like markers, polygons, polylines, and more by inheriting from multiple `Capable` interfaces.

In addition to the common map functionalities, this interface provides specific methods for managing the visual design and style of the HERE map, allowing you to change the map's appearance (e.g., satellite, terrain) and listen for those changes.

This interface inherits from the following interfaces, gaining their capabilities:
*   `MapViewControllerInterface`
*   `MarkerCapableInterface`
*   `PolygonCapableInterface`
*   `PolylineCapableInterface`
*   `CircleCapableInterface`
*   `GroundImageCapableInterface`
*   `RasterLayerCapableInterface`

---

## Methods

### `setMapDesignType`

Sets the visual design for the map. This function allows you to dynamically change the map's appearance, for example, switching between a normal street map and a satellite view.

#### Signature
```kotlin
fun setMapDesignType(value: HereMapDesignType)
```

#### Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `value` | `HereMapDesignType` | The desired map design type to apply to the map. |

#### Returns
This function does not return any value.

#### Example
```kotlin
// Assuming 'mapController' is an instance of HereMapViewControllerInterface
// and HereMapDesignType is an enum with values like SATELLITE, NORMAL, etc.

// Change the map's visual style to satellite view
mapController.setMapDesignType(HereMapDesignType.SATELLITE)
```

---

### `setMapDesignTypeChangeListener`

Registers a listener that will be invoked whenever the map's design type changes. This is useful for reacting to user-initiated or programmatic changes to the map style.

#### Signature
```kotlin
fun setMapDesignTypeChangeListener(listener: HereMapDesignTypeChangeHandler)
```

#### Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `listener` | `HereMapDesignTypeChangeHandler` | A lambda function that will be called with the new `HereMapDesignType` when a change occurs. |

#### Returns
This function does not return any value.

#### Example
```kotlin
// Assuming 'mapController' is an instance of HereMapViewControllerInterface

// Define a listener to handle map design changes
val designChangeListener: HereMapDesignTypeChangeHandler = { newDesignType ->
    println("Map design has been updated to: $newDesignType")
    // You can update UI elements or perform other actions here
}

// Set the listener on the map controller
mapController.setMapDesignTypeChangeListener(designChangeListener)
```

---

## Type Aliases

### `HereMapDesignTypeChangeHandler`

A type alias for the lambda function used to handle map design type change events.

#### Signature
```kotlin
typealias HereMapDesignTypeChangeHandler = (HereMapDesignType) -> Unit
```

#### Description
This handler receives a single parameter, the new `HereMapDesignType`, and does not return a value. It is used with the `setMapDesignTypeChangeListener` method.