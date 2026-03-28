# LineSegmentUtils

The `LineSegmentUtils` object provides utility functions for working with line segments on a sphere, such as calculating bounding boxes and checking for intersections with regions.

---

## createSegmentBounds

Calculates the geographical bounding box (`GeoRectBounds`) for a line segment defined by two points. This function can compute bounds for both simple rhumb lines and more complex geodesic paths.

### Signature

```kotlin
fun createSegmentBounds(
    point1: GeoPointInterface,
    point2: GeoPointInterface,
    geodesic: Boolean = false,
): GeoRectBounds
```

### Description

This function creates a `GeoRectBounds` that fully encloses the line segment between `point1` and `point2`.

-   When `geodesic` is `false` (the default), the method creates a simple rectangular bounding box that contains the two endpoints. This is suitable for short distances or when a rhumb line is assumed.
-   When `geodesic` is `true`, the method approximates the bounds of a great-circle path by sampling multiple points along the geodesic curve. This provides a more accurate bounding box for long segments that may curve significantly across the globe, potentially crossing the antimeridian or poles.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `point1` | `GeoPointInterface` | The first endpoint of the segment. |
| `point2` | `GeoPointInterface` | The second endpoint of the segment. |
| `geodesic` | `Boolean` | **(Optional)** If `true`, calculates the bounds for a geodesic (great-circle) path. If `false` (default), it creates a simple bounding box containing only the two endpoints. |

### Returns

A `GeoRectBounds` object that encloses the line segment.

### Example

```kotlin
// Assuming GeoPoint and GeoRectBounds are implemented
val sanFrancisco = GeoPoint(-122.4194, 37.7749)
val newYork = GeoPoint(-74.0060, 40.7128)

// Calculate simple (non-geodesic) bounds
val simpleBounds = LineSegmentUtils.createSegmentBounds(sanFrancisco, newYork)

// Calculate more accurate geodesic bounds
val geodesicBounds = LineSegmentUtils.createSegmentBounds(sanFrancisco, newYork, geodesic = true)

println("Simple Bounds: $simpleBounds")
println("Geodesic Bounds: $geodesicBounds")
```

---

## segmentIntersectsRegion

Determines if a line segment potentially intersects a given rectangular region (`GeoRectBounds`).

### Signature

```kotlin
fun segmentIntersectsRegion(
    start: GeoPointInterface,
    end: GeoPointInterface,
    region: GeoRectBounds,
    geodesic: Boolean = false,
): Boolean
```

### Description

This function performs an optimized check to see if the line segment from `start` to `end` intersects the specified `region`. It works by first calculating the bounding box of the segment (using `createSegmentBounds`) and then testing if that segment's bounding box intersects with the provided `region`.

**Note:** This is an approximation that checks for bounding box intersection, not a precise line-polygon intersection. It may return `true` in cases where the segment's bounding box intersects the region, but the segment itself does not. However, it will never return `false` if an intersection does occur.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `start` | `GeoPointInterface` | The starting point of the line segment. |
| `end` | `GeoPointInterface` | The ending point of the line segment. |
| `region` | `GeoRectBounds` | The rectangular region to check for intersection against. |
| `geodesic` | `Boolean` | **(Optional)** If `true`, the segment is treated as a geodesic path when calculating its bounds. Defaults to `false`. |

### Returns

Returns `true` if the segment's bounding box intersects the given `region`. Returns `false` if there is no intersection or if the input `region` is empty.

### Example

```kotlin
// Assuming GeoPoint and GeoRectBounds are implemented
val startPoint = GeoPoint(-122.4, 37.7) // Near San Francisco
val endPoint = GeoPoint(-74.0, 40.7)   // Near New York

// A region covering the state of Colorado
val coloradoBounds = GeoRectBounds().apply {
    extend(GeoPoint(-109.0, 41.0))
    extend(GeoPoint(-102.0, 37.0))
}

// A region covering the state of California
val californiaBounds = GeoRectBounds().apply {
    extend(GeoPoint(-124.4, 42.0))
    extend(GeoPoint(-114.1, 32.5))
}

// Check for intersection with Colorado (should be true for a geodesic path)
val intersectsColorado = LineSegmentUtils.segmentIntersectsRegion(
    startPoint, 
    endPoint, 
    coloradoBounds, 
    geodesic = true
)
println("Segment intersects Colorado bounds: $intersectsColorado") // Expected: true

// Check for intersection with California (should be true)
val intersectsCalifornia = LineSegmentUtils.segmentIntersectsRegion(
    startPoint, 
    endPoint, 
    californiaBounds, 
    geodesic = true
)
println("Segment intersects California bounds: $intersectsCalifornia") // Expected: true
```