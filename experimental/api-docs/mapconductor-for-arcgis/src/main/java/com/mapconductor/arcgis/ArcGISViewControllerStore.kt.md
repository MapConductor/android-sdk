Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# ArcGIS Module Utilities

This document provides details on the utility components available in the `com.mapconductor.arcgis` package, including a specialized type alias and a singleton controller store.

## ArcGISMapViewHolderInterface

A type alias for a `MapViewHolderInterface` that is pre-configured for use with ArcGIS 3D scenes.

### Signature

```kotlin
typealias ArcGISMapViewHolderInterface = MapViewHolderInterface<WrapSceneView, SceneView>
```

### Description

`ArcGISMapViewHolderInterface` simplifies the implementation of map view holders for ArcGIS 3D maps. It is an alias for the generic `MapViewHolderInterface`, with its type parameters specialized to `WrapSceneView` and `SceneView`.

This allows developers to work with a consistent interface for managing views that contain an ArcGIS `SceneView`, abstracting away the more complex generic signature.

-   **`WrapSceneView`**: The MapConductor wrapper view that contains the ArcGIS scene.
-   **`SceneView`**: The native ArcGIS SDK view for displaying 3D scenes.

### Example

By using this type alias, you can create cleaner and more readable view holder classes.

```kotlin
import com.mapconductor.arcgis.ArcGISMapViewHolderInterface

// Implement the interface using the type alias
class MyCustomSceneViewHolder : ArcGISMapViewHolderInterface {

    override fun onMapReady(mapView: WrapSceneView, nativeMapView: SceneView) {
        // The map and native scene view are ready to be used.
        println("SceneView is ready for interaction.")
        nativeMapView.arcGISScene?.let { scene ->
            println("Scene loaded: ${scene.basemap?.name}")
        }
    }

    override fun onMapDestroyed() {
        // Clean up resources when the view is destroyed.
        println("SceneView is being destroyed.")
    }
}
```

## ArcGISViewControllerStore

A singleton object that provides global access to an `ArcGISMapViewController` instance.

### Signature

```kotlin
object ArcGISViewControllerStore : StaticHolder<ArcGISMapViewController>()
```

### Description

`ArcGISViewControllerStore` acts as a centralized, static repository for a single `ArcGISMapViewController` instance. It inherits from `StaticHolder`, which manages the storage and lifecycle of the object.

The primary purpose of this store is to provide a convenient, application-wide access point to the main map view controller, eliminating the need to pass the controller instance through multiple layers of the application.

### Example

You can set and retrieve the `ArcGISMapViewController` instance from anywhere in your application.

```kotlin
import com.mapconductor.arcgis.ArcGISViewControllerStore
import com.mapconductor.arcgis.map.ArcGISMapViewController

// --- In your Activity or Fragment where the controller is created ---

// Create and store the controller instance
val mapViewController = ArcGISMapViewController(context)
ArcGISViewControllerStore.instance = mapViewController


// --- In another part of your application ---

// Retrieve the globally accessible controller instance
val controller = ArcGISViewControllerStore.instance

// Use the controller to interact with the map
controller?.panToLocation(someLocation)
```