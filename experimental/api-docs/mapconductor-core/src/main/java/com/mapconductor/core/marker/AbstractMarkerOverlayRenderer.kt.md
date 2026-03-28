Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# AbstractMarkerOverlayRenderer<MapViewHolderType, ActualMarker>

## Signature

```kotlin
abstract class AbstractMarkerOverlayRenderer<
    MapViewHolderType : MapViewHolderInterface<*, *>,
    ActualMarker,
>(
    val holder: MapViewHolderType,
    val coroutine: CoroutineScope,
    val dropAnimateDuration: Long = Settings.Default.markerDropAnimateDuration,
    val bounceAnimateDuration: Long = Settings.Default.markerBounceAnimateDuration,
) : MarkerOverlayRendererInterface<ActualMarker>
```

## Description

`AbstractMarkerOverlayRenderer` is an abstract base class designed for rendering markers on a map. It provides a robust framework for handling common marker animations, such as "drop" and "bounce," using Kotlin Coroutines and Flows.

As an abstract class, it must be subclassed to provide a concrete implementation for the specific map provider being used (e.g., Google Maps, Mapbox). The primary responsibility of the subclass is to implement the `setMarkerPosition` method, which handles the platform-specific logic for updating a marker's position.

### Type Parameters

| Name | Description |
| :--- | :--- |
| `MapViewHolderType` | The type of the map view holder, which must conform to `MapViewHolderInterface`. |
| `ActualMarker` | The type of the underlying, platform-specific marker object (e.g., `com.google.android.gms.maps.model.Marker`). |

### Constructor Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `holder` | `MapViewHolderType` | The map view holder instance that provides screen-to-geo coordinate conversion. |
| `coroutine` | `CoroutineScope` | The coroutine scope used to launch and manage the animation coroutines. |
| `dropAnimateDuration` | `Long` | The duration of the drop animation in milliseconds. Defaults to `Settings.Default.markerDropAnimateDuration`. |
| `bounceAnimateDuration` | `Long` | The duration of the bounce animation in milliseconds. Defaults to `Settings.Default.markerBounceAnimateDuration`. |

## Properties

| Property | Type | Description |
| :--- | :--- | :--- |
| `animateStartListener` | `OnMarkerEventHandler?` | An optional listener that is invoked when a marker animation begins. It receives the `MarkerState` of the animating marker. |
| `animateEndListener` | `OnMarkerEventHandler?` | An optional listener that is invoked when a marker animation completes. It receives the `MarkerState` of the animating marker. |

## Methods

### onAnimate

Triggers an animation for a given marker entity. This method reads the animation type specified in the entity's state and executes the corresponding animation function (`animateMarkerDrop` or `animateMarkerBounce`).

**Signature**
```kotlin
override suspend fun onAnimate(entity: MarkerEntityInterface<ActualMarker>)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `entity` | `MarkerEntityInterface<ActualMarker>` | The marker entity to be animated. The animation to perform is determined by `entity.state.getAnimation()`. |

### animateMarkerDrop

Performs a "drop" animation on a marker. The marker animates vertically from the top of the screen to its final geographical position using a linear interpolation.

**Signature**
```kotlin
fun animateMarkerDrop(entity: MarkerEntityInterface<ActualMarker>, duration: Long)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `entity` | `MarkerEntityInterface<ActualMarker>` | The marker entity to animate. |
| `duration` | `Long` | The total duration of the animation in milliseconds. |

### animateMarkerBounce

Performs a "bounce" animation on a marker. The marker animates from the top of the screen to its final geographical position, concluding with a bounce effect.

**Signature**
```kotlin
fun animateMarkerBounce(entity: MarkerEntityInterface<ActualMarker>, duration: Long)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `entity` | `MarkerEntityInterface<ActualMarker>` | The marker entity to animate. |
| `duration` | `Long` | The total duration of the animation in milliseconds. |

### zoomToMetersPerPixel

A utility function that calculates the distance in meters represented by a single pixel at a given map zoom level.

**Signature**
```kotlin
fun zoomToMetersPerPixel(zoom: Double, tileSize: Int): Double
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoom` | `Double` | The current zoom level of the map. |
| `tileSize` | `Int` | The size of the map tiles in pixels (e.g., 256). |

**Returns**
| Type | Description |
| :--- | :--- |
| `Double` | The number of meters per pixel at the specified zoom level. |

### setMarkerPosition (Abstract)

An abstract method that must be implemented by subclasses. This method is responsible for updating the geographical position of the actual, platform-specific marker object on the map.

**Signature**
```kotlin
abstract fun setMarkerPosition(
    markerEntity: MarkerEntityInterface<ActualMarker>,
    position: GeoPoint,
)
```

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `markerEntity` | `MarkerEntityInterface<ActualMarker>` | The marker entity containing the platform-specific marker object to update. |
| `position` | `GeoPoint` | The new geographical coordinates (`latitude`, `longitude`) to set for the marker. |

## Example

The following example demonstrates how to create a concrete implementation of `AbstractMarkerOverlayRenderer` for Google Maps.

```kotlin
import com.google.android.gms.maps.model.Marker as GoogleMarker
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

// Assume MapViewHolderType and MarkerEntityInterface are defined elsewhere
// For example, GoogleMapViewHolder and a corresponding MarkerEntity implementation

class GoogleMapMarkerRenderer(
    holder: GoogleMapViewHolder, // A concrete MapViewHolderType
    coroutine: CoroutineScope
) : AbstractMarkerOverlayRenderer<GoogleMapViewHolder, GoogleMarker>(
    holder = holder,
    coroutine = coroutine
) {
    /**
     * Implements the abstract method to update the position of a Google Maps marker.
     */
    override fun setMarkerPosition(
        markerEntity: MarkerEntityInterface<GoogleMarker>,
        position: GeoPoint
    ) {
        // Access the platform-specific marker from the entity
        val googleMarker = markerEntity.getActualMarker()
        
        // Convert GeoPoint to Google Maps LatLng and set the position
        googleMarker?.position = LatLng(position.latitude, position.longitude)
    }
}

// --- Usage ---

fun setupMarkerRenderer(mapViewHolder: GoogleMapViewHolder) {
    // Create an instance of the concrete renderer
    val markerRenderer = GoogleMapMarkerRenderer(
        holder = mapViewHolder,
        coroutine = CoroutineScope(Dispatchers.Main)
    )

    // Set up an animation end listener
    markerRenderer.animateEndListener = { markerState ->
        println("Animation finished for marker at: ${markerState.position}")
    }

    // To trigger an animation on a marker entity:
    // val myMarkerEntity: MarkerEntityInterface<GoogleMarker> = ...
    // myMarkerEntity.state.animate(MarkerAnimation.Drop)
    // CoroutineScope(Dispatchers.Main).launch {
    //     markerRenderer.onAnimate(myMarkerEntity)
    // }
}
```