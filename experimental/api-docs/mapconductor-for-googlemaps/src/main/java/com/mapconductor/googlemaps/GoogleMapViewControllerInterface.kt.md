Of course! Here is the high-quality SDK documentation for the provided code snippet.

# GoogleMapViewControllerInterface

## Description

The `GoogleMapViewControllerInterface` serves as the primary controller for a Google Map view. It provides a comprehensive API for interacting with the map, consolidating functionalities for managing various map objects and controlling the map's visual appearance.

This interface aggregates capabilities from several other interfaces, making it a central point for managing:
- Markers (`MarkerCapableInterface`)
- Polygons (`PolygonCapableInterface`)
- Polylines (`PolylineCapableInterface`)
- Circles (`CircleCapableInterface`)
- Ground Images (`GroundImageCapableInterface`)
- Raster Layers (`RasterLayerCapableInterface`)

In addition to these object management capabilities, it provides specific methods for controlling the Google Map's design type (e.g., standard, satellite, hybrid).

## Methods

### setMapDesignType

Sets the visual style or theme of the map.

**Signature**
```kotlin
fun setMapDesignType(value: GoogleMapDesignType)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `value` | `GoogleMapDesignType` | The new design type to apply to the map. |

<br/>

### setMapDesignTypeChangeListener

Registers a listener that is invoked whenever the map's design type changes. This allows you to react to style changes, for example, by updating UI elements.

**Signature**
```kotlin
fun setMapDesignTypeChangeListener(listener: GoogleMapDesignTypeChangeHandler)
```

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `listener` | `GoogleMapDesignTypeChangeHandler` | A callback function that will be executed with the new `GoogleMapDesignType` when the map style changes. |

## Type Aliases

### GoogleMapDesignTypeChangeHandler

A function type definition for a listener that handles map design type change events.

**Signature**
```kotlin
typealias GoogleMapDesignTypeChangeHandler = (GoogleMapDesignType) -> Unit
```

**Description**

This is a lambda or function that accepts a single parameter:
- `GoogleMapDesignType`: The new design type that has been applied to the map.

## Example

The following example demonstrates how to set a listener for map design changes and then trigger it by changing the map's design type.

```kotlin
// Assume 'mapController' is an available instance of GoogleMapViewControllerInterface
// and 'GoogleMapDesignType' is an enum with values like STANDARD, SATELLITE, etc.

// 1. Set up a listener to react to map design changes.
// The lambda will be called every time the design type is updated.
mapController.setMapDesignTypeChangeListener { newDesignType ->
    println("Map design type has been updated to: $newDesignType")
    // You can update other UI components or trigger logic based on the new style.
}

// 2. Change the map's design type to SATELLITE.
// This action will invoke the listener defined above.
println("Attempting to set map design to SATELLITE...")
mapController.setMapDesignType(GoogleMapDesignType.SATELLITE)

// Expected console output:
// Attempting to set map design to SATELLITE...
// Map design type has been updated to: SATELLITE
```