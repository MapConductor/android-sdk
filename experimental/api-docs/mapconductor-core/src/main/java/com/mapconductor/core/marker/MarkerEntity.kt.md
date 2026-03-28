Of course. Here is the high-quality SDK documentation for the provided code snippet.

---

### `MarkerEntityInterface<ActualMarker>`

#### Signature

```kotlin
interface MarkerEntityInterface<ActualMarker>
```

#### Description

Defines the contract for a marker entity within the map system. This generic interface acts as a wrapper around a platform-specific marker object (`ActualMarker`), providing a consistent way to manage its state and lifecycle.

#### Properties

| Property      | Type                  | Description                                                                                                                                    |
| :------------ | :-------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------- |
| `marker`      | `ActualMarker?`       | The underlying, platform-specific marker object (e.g., a Google Maps `Marker`). It is `null` if the marker has not been created or has been removed. |
| `state`       | `MarkerState`         | An immutable object representing the desired state of the marker, including its position, icon, anchor, etc.                                     |
| `fingerPrint` | `MarkerFingerPrint`   | A unique identifier derived from the `state`. It is used internally to efficiently detect changes and optimize rendering.                         |
| `visible`     | `Boolean`             | Controls the visibility of the marker on the map. Setting this to `false` hides the marker, and `true` shows it.                                 |
| `isRendered`  | `Boolean`             | A flag indicating whether the marker is currently rendered on the map canvas.                                                                  |

---

### `MarkerEntity<ActualMarker>`

#### Signature

```kotlin
class MarkerEntity<ActualMarker>(
    override var marker: ActualMarker?,
    override val state: MarkerState,
    override var visible: Boolean = true,
    override var isRendered: Boolean = false,
) : MarkerEntityInterface<ActualMarker>
```

#### Description

The default implementation of `MarkerEntityInterface`. This class encapsulates all the information required to manage a single marker on the map, including its platform-specific instance, its desired state, and its current visibility and render status.

#### Parameters

| Parameter    | Type            | Description                                                                                                                            |
| :----------- | :-------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `marker`     | `ActualMarker?` | The actual marker object from the underlying map SDK. Can be `null` if the marker hasn't been instantiated yet.                          |
| `state`      | `MarkerState`   | The state object containing all configuration for the marker (e.g., position, icon). The `fingerPrint` is automatically generated from this state. |
| `visible`    | `Boolean`       | (Optional) The initial visibility of the marker. Defaults to `true`.                                                                   |
| `isRendered` | `Boolean`       | (Optional) The initial rendered state of the marker. Defaults to `false`.                                                              |

#### Example

This example demonstrates how to create and use a `MarkerEntity`. We'll assume the existence of `MapboxMarker` as our platform-specific marker type and a `MarkerState` object.

```kotlin
// Assume these classes exist for context
// data class LatLng(val latitude: Double, val longitude: Double)
// class MapboxMarker { /* Platform-specific marker implementation */ }
// data class MarkerState(val position: LatLng, val title: String) {
//     fun fingerPrint(): MarkerFingerPrint = MarkerFingerPrint(this.hashCode().toString())
// }
// data class MarkerFingerPrint(val id: String)

// 1. Define the state for our marker
val markerState = MarkerState(
    position = LatLng(40.7128, -74.0060),
    title = "New York City"
)

// 2. Create a MarkerEntity instance. Initially, the platform marker is null.
val markerEntity = MarkerEntity<MapboxMarker>(
    marker = null,
    state = markerState,
    visible = true
)

// 3. Access its properties
println("Is marker visible? ${markerEntity.visible}") // Output: Is marker visible? true
println("Marker fingerprint: ${markerEntity.fingerPrint.id}") // Output: A hash-based ID

// After the map renderer creates the actual marker, it can be assigned.
val actualMapboxMarker = MapboxMarker()
markerEntity.marker = actualMapboxMarker
markerEntity.isRendered = true

// You can later update the visibility
markerEntity.visible = false
println("Is marker visible now? ${markerEntity.visible}") // Output: Is marker visible now? false
```