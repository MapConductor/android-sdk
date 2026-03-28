# Spherical

## Overview

The `Spherical` object provides a collection of static utility functions for performing spherical geometry calculations on Earth's surface. It uses a spherical model of the Earth for all computations, which is suitable for most mapping applications.

These functions are essential for tasks like calculating distances between two points, determining the heading from one point to another, finding a destination point given a starting point and an offset, and calculating the area or length of a path.

This implementation is a Kotlin port of the spherical geometry utilities found in the Cordova Google Maps plugin, adapted to use the `GeoPointInterface` for coordinate representation.

---

## `computeDistanceBetween`

Calculates the great-circle distance between two geographical points using the haversine formula.

### Signature

```kotlin
fun computeDistanceBetween(
    from: GeoPointInterface,
    to: GeoPointInterface
): Double
```

### Description

This function returns the distance, in meters, between two `GeoPointInterface` locations. The calculation is based on the haversine formula, which accounts for the Earth's curvature, providing an accurate distance over a sphere.

### Parameters

| Parameter | Type                | Description              |
| :-------- | :------------------ | :----------------------- |
| `from`    | `GeoPointInterface` | The starting point.      |
| `to`      | `GeoPointInterface` | The ending point.        |

### Returns

| Type     | Description            |
| :------- | :--------------------- |
| `Double` | The distance in meters. |

### Example

```kotlin
// Assuming GeoPoint implements GeoPointInterface
val sanFrancisco = GeoPoint(latitude = 37.7749, longitude = -122.4194)
val newYork = GeoPoint(latitude = 40.7128, longitude = -74.0060)

val distance = Spherical.computeDistanceBetween(sanFrancisco, newYork)
// distance is approximately 4128541.9 meters
println("Distance: $distance meters")
```

---

## `computeHeading`

Calculates the initial bearing (heading) from a starting point to a destination point.

### Signature

```kotlin
fun computeHeading(
    from: GeoPointInterface,
    to: GeoPointInterface
): Double
```

### Description

This function returns the heading from one `GeoPointInterface` to another. The heading is expressed in degrees clockwise from true North and is normalized to the range `(-180, 180]`. A heading of 0 is North, 90 is East, 180 is South, and -90 is West.

### Parameters

| Parameter | Type                | Description              |
| :-------- | :------------------ | :----------------------- |
| `from`    | `GeoPointInterface` | The starting point.      |
| `to`      | `GeoPointInterface` | The ending point.        |

### Returns

| Type     | Description                                       |
| :------- | :------------------------------------------------ |
| `Double` | The heading in degrees within the range `(-180, 180]`. |

### Example

```kotlin
val sanFrancisco = GeoPoint(latitude = 37.7749, longitude = -122.4194)
val newYork = GeoPoint(latitude = 40.7128, longitude = -74.0060)

val heading = Spherical.computeHeading(sanFrancisco, newYork)
// heading is approximately 67.2 degrees
println("Heading: $heading degrees")
```

---

## `computeOffset`

Calculates a destination point given a starting point, a distance, and a heading.

### Signature

```kotlin
fun computeOffset(
    origin: GeoPointInterface,
    distance: Double,
    heading: Double
): GeoPoint
```

### Description

This function returns a new `GeoPoint` that is a specified distance and heading away from an origin point. It is useful for projecting a point on the map.

### Parameters

| Parameter  | Type                | Description                                           |
| :--------- | :------------------ | :---------------------------------------------------- |
| `origin`   | `GeoPointInterface` | The starting point.                                   |
| `distance` | `Double`            | The distance to travel in meters.                     |
| `heading`  | `Double`            | The direction to travel in degrees (0=N, 90=E, 180=S). |

### Returns

| Type       | Description                               |
| :--------- | :---------------------------------------- |
| `GeoPoint` | The new `GeoPoint` at the calculated offset. |

### Example

```kotlin
val startPoint = GeoPoint(latitude = 40.7128, longitude = -74.0060)
val distanceMeters = 10000.0 // 10 km
val headingDegrees = 45.0    // Northeast

val destination = Spherical.computeOffset(startPoint, distanceMeters, headingDegrees)
// destination is approximately GeoPoint(latitude=40.7763, longitude=-73.9205)
println("New Position: ${destination.latitude}, ${destination.longitude}")
```

---

## `computeOffsetOrigin`

Calculates the origin point given a destination, the distance traveled, and the original heading.

### Signature

```kotlin
fun computeOffsetOrigin(
    to: GeoPointInterface,
    distance: Double,
    heading: Double
): GeoPoint?
```

### Description

This function is the inverse of `computeOffset`. It determines the starting point from which one would have traveled a certain distance at a specific heading to arrive at the destination `to`. It calculates this by reversing the heading and applying the `computeOffset` function.

### Parameters

| Parameter  | Type                | Description                               |
| :--------- | :------------------ | :---------------------------------------- |
| `to`       | `GeoPointInterface` | The destination point.                    |
| `distance` | `Double`            | The distance traveled in meters.          |
| `heading`  | `Double`            | The original heading in degrees.          |

### Returns

| Type        | Description                                                              |
| :---------- | :----------------------------------------------------------------------- |
| `GeoPoint?` | The original `GeoPoint` position, or `null` if a solution is not available. |

### Example

```kotlin
val destination = GeoPoint(latitude = 40.7763, longitude = -73.9205)
val distanceMeters = 10000.0
val headingDegrees = 45.0

val origin = Spherical.computeOffsetOrigin(destination, distanceMeters, headingDegrees)
// origin is approximately GeoPoint(latitude=40.7128, longitude=-74.0060)
if (origin != null) {
    println("Original Position: ${origin.latitude}, ${origin.longitude}")
}
```

---

## `computeLength`

Calculates the total length of a path defined by a list of points.

### Signature

```kotlin
fun computeLength(path: List<GeoPointInterface>): Double
```

### Description

This function sums the great-circle distances between consecutive points in a list to calculate the total length of the path. If the path has fewer than two points, the length is 0.

### Parameters

| Parameter | Type                    | Description                               |
| :-------- | :---------------------- | :---------------------------------------- |
| `path`    | `List<GeoPointInterface>` | A list of points defining the path.       |

### Returns

| Type     | Description                     |
| :------- | :------------------------------ |
| `Double` | The total length in meters.     |

### Example

```kotlin
val path = listOf(
    GeoPoint(latitude = 37.7749, longitude = -122.4194), // San Francisco
    GeoPoint(latitude = 34.0522, longitude = -118.2437), // Los Angeles
    GeoPoint(latitude = 32.7157, longitude = -117.1611)  // San Diego
)

val totalLength = Spherical.computeLength(path)
// totalLength is approximately 735,930 meters
println("Total path length: $totalLength meters")
```

---

## `computeArea`

Calculates the non-negative area of a closed path on the Earth's surface.

### Signature

```kotlin
fun computeArea(path: List<GeoPointInterface>): Double
```

### Description

This function computes the area of a closed polygon defined by a list of points. It returns the absolute (non-negative) value of the area. The path is assumed to be closed; the function does not automatically connect the last point to the first. For accurate results, the last point in the list should be the same as the first.

### Parameters

| Parameter | Type                    | Description                               |
| :-------- | :---------------------- | :---------------------------------------- |
| `path`    | `List<GeoPointInterface>` | A list of points defining the polygon.    |

### Returns

| Type     | Description                     |
| :------- | :------------------------------ |
| `Double` | The area in square meters.      |

### Example

```kotlin
// A square-like polygon
val polygon = listOf(
    GeoPoint(latitude = 40.0, longitude = -75.0),
    GeoPoint(latitude = 41.0, longitude = -75.0),
    GeoPoint(latitude = 41.0, longitude = -74.0),
    GeoPoint(latitude = 40.0, longitude = -74.0),
    GeoPoint(latitude = 40.0, longitude = -75.0) // Closing the polygon
)

val area = Spherical.computeArea(polygon)
println("Area: $area square meters")
```

---

## `computeSignedArea`

Calculates the signed area of a closed path, which can be used to determine the path's orientation.

### Signature

```kotlin
fun computeSignedArea(path: List<GeoPointInterface>): Double
```

### Description

This function computes the signed area of a closed polygon. The sign of the result indicates the orientation of the vertices:
- **Positive value:** The vertices are in a counter-clockwise order.
- **Negative value:** The vertices are in a clockwise order.

The magnitude of the result is the area in square meters. The path is assumed to be closed.

### Parameters

| Parameter | Type                    | Description                               |
| :-------- | :---------------------- | :---------------------------------------- |
| `path`    | `List<GeoPointInterface>` | A list of points defining the polygon.    |

### Returns

| Type     | Description                                                              |
| :------- | :----------------------------------------------------------------------- |
| `Double` | The signed area in square meters. Positive for CCW, negative for CW.     |

### Example

```kotlin
// Counter-clockwise polygon
val ccwPolygon = listOf(
    GeoPoint(latitude = 40.0, longitude = -75.0),
    GeoPoint(latitude = 41.0, longitude = -75.0),
    GeoPoint(latitude = 41.0, longitude = -74.0),
    GeoPoint(latitude = 40.0, longitude = -74.0)
)

val signedAreaCCW = Spherical.computeSignedArea(ccwPolygon)
// signedAreaCCW will be a positive value
println("Counter-clockwise signed area: $signedAreaCCW")

// Clockwise polygon
val cwPolygon = ccwPolygon.reversed()
val signedAreaCW = Spherical.computeSignedArea(cwPolygon)
// signedAreaCW will be a negative value with the same magnitude
println("Clockwise signed area: $signedAreaCW")
```

---

## `sphericalInterpolate`

Interpolates between two points along a great-circle path using Spherical Linear Interpolation (Slerp).

### Signature

```kotlin
fun sphericalInterpolate(
    from: GeoPointInterface,
    to: GeoPointInterface,
    fraction: Double
): GeoPoint
```

### Description

This method provides a highly accurate interpolation between two points on the globe by following the shortest path along the Earth's surface (a great-circle arc). It is ideal for interpolating over medium to long distances where Earth's curvature is significant. For very close points, it falls back to linear interpolation for stability.

### Parameters

| Parameter  | Type                | Description                                                              |
| :--------- | :------------------ | :----------------------------------------------------------------------- |
| `from`     | `GeoPointInterface` | The starting point.                                                      |
| `to`       | `GeoPointInterface` | The ending point.                                                        |
| `fraction` | `Double`            | The interpolation fraction, from 0.0 (at `from`) to 1.0 (at `to`).       |

### Returns

| Type       | Description                               |
| :--------- | :---------------------------------------- |
| `GeoPoint` | The interpolated `GeoPoint` position.     |

### Example

```kotlin
val start = GeoPoint(latitude = 37.7749, longitude = -122.4194) // San Francisco
val end = GeoPoint(latitude = 40.7128, longitude = -74.0060)   // New York

// Find the point halfway between SF and NY along the great circle
val midpoint = Spherical.sphericalInterpolate(start, end, 0.5)

println("Midpoint: ${midpoint.latitude}, ${midpoint.longitude}")
```

---

## `linearInterpolate`

Performs a simple linear interpolation between two points on a flat 2D plane.

### Signature

```kotlin
fun linearInterpolate(
    from: GeoPointInterface,
    to: GeoPointInterface,
    fraction: Double
): GeoPoint
```

### Description

This method treats latitude and longitude as coordinates on a Cartesian plane and performs a simple linear interpolation. It is computationally faster than `sphericalInterpolate` but is less accurate, especially over long distances, as it does not account for Earth's curvature.

A key feature is its handling of longitude: it automatically interpolates along the shorter of the two paths (e.g., from 170°E to -170°W, it will cross the antimeridian instead of going the long way around).

Use this method for short distances or when performance is more critical than geographic accuracy.

### Parameters

| Parameter  | Type                | Description                                                              |
| :--------- | :------------------ | :----------------------------------------------------------------------- |
| `from`     | `GeoPointInterface` | The starting point.                                                      |
| `to`       | `GeoPointInterface` | The ending point.                                                        |
| `fraction` | `Double`            | The interpolation fraction, from 0.0 (at `from`) to 1.0 (at `to`).       |

### Returns

| Type       | Description                               |
| :--------- | :---------------------------------------- |
| `GeoPoint` | The linearly interpolated `GeoPoint` position. |

### Example

```kotlin
// Interpolating across the antimeridian
val start = GeoPoint(latitude = 60.0, longitude = 175.0)
val end = GeoPoint(latitude = 60.0, longitude = -175.0)

// Find the point halfway between them (should be near 180 degrees longitude)
val midpoint = Spherical.linearInterpolate(start, end, 0.5)

// midpoint.longitude will be approximately 180.0 (or -180.0)
println("Midpoint: ${midpoint.latitude}, ${midpoint.longitude}")
```