Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

# PolylineEntity<ActualPolyline>

A generic wrapper class that represents a polyline on the map. It encapsulates the native map polyline object (`ActualPolyline`), its descriptive state (`PolylineState`), and derived properties like its fingerprint and geographic bounds.

This class provides a consistent interface for managing polylines regardless of the underlying map provider and optimizes performance by caching computed properties like the bounding box.

## Signature

```kotlin
class PolylineEntity<ActualPolyline>(
    override val polyline: ActualPolyline,
    override val state: PolylineState,
) : PolylineEntityInterface<ActualPolyline>
```

## Constructor

### PolylineEntity()

Creates a new instance of `PolylineEntity`.

#### Signature

```kotlin
PolylineEntity(
    polyline: ActualPolyline, 
    state: PolylineState
)
```

#### Parameters

| Parameter | Type | Description |
|-----------|------------------|-------------|
| `polyline` | `ActualPolyline` | The native polyline object from the specific map SDK. |
| `state` | `PolylineState` | An object containing the descriptive state of the polyline, such as its points, color, width, and geodesic property. |

## Properties

### polyline

The underlying native polyline object provided by the map's SDK. This allows for direct interaction with the map-specific object if needed.

#### Signature

```kotlin
val polyline: ActualPolyline
```

#### Returns

The native `ActualPolyline` object.

### state

The state object that defines the polyline's properties, including its vertices (`points`), color, width, visibility, and whether it should be rendered as a geodesic line.

#### Signature

```kotlin
val state: PolylineState
```

#### Returns

The `PolylineState` object associated with this entity.

### fingerPrint

A unique fingerprint generated from the `PolylineState`. This is useful for efficiently comparing polyline states to detect changes without performing a deep comparison of all properties.

#### Signature

```kotlin
val fingerPrint: PolylineFingerPrint
```

#### Returns

A `PolylineFingerPrint` object representing the current state.

### bounds

Calculates and returns the geographic bounding box (`GeoRectBounds`) that completely encloses the polyline. The calculation method depends on the `geodesic` property in the `PolylineState`:

-   **Non-geodesic:** The bounds are calculated by including all the vertex points of the polyline.
-   **Geodesic:** The bounds are calculated by sampling intermediate points along each great-circle arc between vertices. This ensures the bounding box correctly accounts for the curvature of the Earth, which can cause the line to extend beyond the simple bounding box of its vertices (e.g., a line crossing the 180th meridian or near the poles).

The result is cached for performance. The cache is invalidated and the bounds are recalculated only when the polyline's points or its `geodesic` property change.

#### Signature

```kotlin
val bounds: GeoRectBounds
```

#### Returns

The calculated `GeoRectBounds` for the polyline.

## Example

```kotlin
// Assume the existence of these classes for the example
// data class GeoPoint(val latitude: Double, val longitude: Double)
// data class PolylineState(val id: String, val points: List<GeoPoint>, val geodesic: Boolean)
// class MockNativePolyline { /* ... */ }

// 1. Define the state for a geodesic polyline
val polylineState = PolylineState(
    id = "p1",
    points = listOf(
        GeoPoint(latitude = 40.7128, longitude = -74.0060), // New York
        GeoPoint(latitude = 48.8566, longitude = 2.3522)    // Paris
    ),
    geodesic = true
)

// 2. Create a mock native polyline object
val nativePolyline = MockNativePolyline()

// 3. Instantiate the PolylineEntity
val polylineEntity = PolylineEntity(
    polyline = nativePolyline,
    state = polylineState
)

// 4. Access the computed properties
// The bounds will be calculated to include the geodesic curve between NYC and Paris.
val geoBounds = polylineEntity.bounds 
println("Polyline Bounds: $geoBounds")

// Access the fingerprint for change detection
val fingerprint = polylineEntity.fingerPrint
println("Polyline Fingerprint: $fingerprint")

// Accessing bounds again will return the cached value without recalculation
val cachedBounds = polylineEntity.bounds 
```