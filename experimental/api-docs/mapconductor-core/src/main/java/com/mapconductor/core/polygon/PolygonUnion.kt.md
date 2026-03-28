Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

# Polygon Hole Utilities

This document provides details on extension functions for the `PolygonState` class, designed to merge and normalize the polygon's interior holes.

---

## `unionHoles()`

Merges overlapping holes within a `PolygonState` into single, non-overlapping holes.

### Signature

```kotlin
fun PolygonState.unionHoles(): PolygonState
```

### Description

The `unionHoles()` function is a non-mutating operation that processes the `holes` of a `PolygonState` object. It identifies any overlapping holes and combines them into a new set of distinct, non-overlapping holes.

This operation is useful for cleaning up complex polygons that may have been generated with intersecting inner boundaries. The function returns a **new** `PolygonState` instance containing the merged holes, leaving the original object unmodified.

Key characteristics:
- **Planar Geometry**: The union calculation is performed on a 2D plane (longitude/latitude). For very large polygons or those near the Earth's poles, the results may differ from what would be expected with geodesic calculations.
- **Winding Order**: The resulting holes are normalized to have a clockwise winding order, which is a common convention for polygon holes in many rendering systems.
- **Error Handling**: If the polygon has one or zero holes, or if the union process fails for any reason, the function safely returns the original, unmodified `PolygonState` instance.

### Parameters

This is an extension function and does not take any direct parameters. It operates on the `PolygonState` instance it is called on.

### Returns

| Type           | Description                                                                                                                                                           |
|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `PolygonState` | A new `PolygonState` instance with merged holes. Returns the original `PolygonState` if it has fewer than two holes or if the union process fails for any reason. |

### Example

```kotlin
// Assume a polygonState with two overlapping holes.
// The `holes` property is a List<List<GeoPointInterface>>.
val polygonWithOverlappingHoles: PolygonState = createPolygonWithTwoHoles()

println("Number of holes before: ${polygonWithOverlappingHoles.holes.size}")
// Expected output: Number of holes before: 2

// Union the holes to create a new polygon state
val newPolygonState = polygonWithOverlappingHoles.unionHoles()

// The original state is not modified
println("Number of holes in original after: ${polygonWithOverlappingHoles.holes.size}")
// Expected output: Number of holes in original after: 2

// The new state has the merged holes
println("Number of holes in new state: ${newPolygonState.holes.size}")
// Expected output: Number of holes in new state: 1
```

---

## `union()`

A concise alias for the `unionHoles()` function.

### Signature

```kotlin
fun PolygonState.union(): PolygonState
```

### Description

This function is a convenient shorthand for `unionHoles()`. It offers the exact same functionality and behavior but with a shorter name for more readable callsites.

### Parameters

None.

### Returns

| Type           | Description                                                                                                                                                           |
|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `PolygonState` | A new `PolygonState` instance with merged holes. Returns the original `PolygonState` if it has fewer than two holes or if the union process fails for any reason. |

### Example

```kotlin
// Assume a polygonState with overlapping holes
val polygonState: PolygonState = createPolygonWithTwoHoles()

// Use the alias to union the holes
val resultState = polygonState.union()

// resultState now contains the single, merged hole
println("Number of holes in result: ${resultState.holes.size}")
// Expected output: Number of holes in result: 1
```

---

## `unionHolesInPlace()`

Performs the hole union operation and modifies the original `PolygonState` object directly.

### Signature

```kotlin
fun PolygonState.unionHolesInPlace(): PolygonState
```

### Description

This function provides an in-place, mutating alternative to `unionHoles()`. It calculates the union of any overlapping holes and then updates the `holes` property of the original `PolygonState` object with the result.

This method is more memory-efficient if you do not need to preserve the polygon's original state, as it avoids allocating a new `PolygonState` object.

**Note**: For this function to work as expected, the `holes` property of your `PolygonState` class must be a mutable variable (`var`).

### Parameters

None.

### Returns

| Type           | Description                                                                                                                            |
|----------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `PolygonState` | The original, now-mutated `PolygonState` instance. Returns the same instance unmodified if there was nothing to union or if an error occurred. |

### Example

```kotlin
// Assume a polygonState where the `holes` property is a `var`
val polygonState: PolygonState = createMutablePolygonWithTwoHoles()

println("Number of holes before: ${polygonState.holes.size}")
// Expected output: Number of holes before: 2

val returnedState = polygonState.unionHolesInPlace()

// The original state object has been modified
println("Number of holes after: ${polygonState.holes.size}")
// Expected output: Number of holes after: 1

// The returned object is the same instance as the original
val isSameInstance = returnedState === polygonState
println("Is the returned object the same instance? $isSameInstance")
// Expected output: Is the returned object the same instance? true
```