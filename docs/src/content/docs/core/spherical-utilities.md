---
title: "Spherical Utilities"
---

The spherical utilities provide accurate geographic calculations using the spherical Earth model. These functions are essential for distance measurements, path calculations, and geographic computations.

## Overview

The `Spherical` object contains utility functions for calculating distances, headings, and positions on Earth's surface. It uses the spherical Earth model with Earth's radius based on the WGS84 ellipsoid.

```kotlin
import com.mapconductor.core.spherical.Spherical
```

## Core Constants

- **Earth Radius**: 6,378,137 meters (WGS84 ellipsoid semi-major axis)
- **Coordinate System**: WGS84 geographic coordinates (latitude/longitude)

## Distance Calculations

### Distance Between Points

```kotlin
fun computeDistanceBetween(from: GeoPoint, to: GeoPoint): Double
```

Calculate the shortest distance between two points using the haversine formula:

```kotlin
val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val newYork = GeoPointImpl.fromLatLong(40.7128, -74.0060)

val distanceMeters = Spherical.computeDistanceBetween(sanFrancisco, newYork)
val distanceKm = distanceMeters / 1000.0
val distanceMiles = distanceMeters / 1609.344

println("Distance: ${distanceKm.toInt()} km (${distanceMiles.toInt()} miles)")
```

### Path Length

```kotlin
fun computeLength(path: List<GeoPoint>): Double
```

Calculate the total length of a path consisting of multiple points:

```kotlin
val routePoints = listOf(
    GeoPointImpl.fromLatLong(37.7749, -122.4194), // San Francisco
    GeoPointImpl.fromLatLong(37.7849, -122.4094), // North Beach
    GeoPointImpl.fromLatLong(37.7949, -122.3994), // Russian Hill
    GeoPointImpl.fromLatLong(37.8049, -122.3894)  // Fisherman's Wharf
)

val totalDistance = Spherical.computeLength(routePoints)
println("Route length: ${(totalDistance / 1000).toInt()} km")
```

## Direction and Heading

### Bearing Calculation

```kotlin
fun computeHeading(from: GeoPoint, to: GeoPoint): Double
```

Calculate the initial bearing (direction) from one point to another:

```kotlin
val start = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val destination = GeoPointImpl.fromLatLong(40.7128, -74.0060)

val bearing = Spherical.computeHeading(start, destination)
println("Head ${bearing.toInt()}° from San Francisco to reach New York")

// Convert to compass direction
val compassDirection = when {
    bearing >= -22.5 && bearing < 22.5 -> "North"
    bearing >= 22.5 && bearing < 67.5 -> "Northeast"
    bearing >= 67.5 && bearing < 112.5 -> "East"
    bearing >= 112.5 && bearing < 157.5 -> "Southeast"
    bearing >= 157.5 || bearing < -157.5 -> "South"
    bearing >= -157.5 && bearing < -112.5 -> "Southwest"
    bearing >= -112.5 && bearing < -67.5 -> "West"
    else -> "Northwest"
}
```

## Position Calculation

### Offset from Point

```kotlin
fun computeOffset(origin: GeoPoint, distance: Double, heading: Double): GeoPointImpl
```

Calculate a new position given distance and direction from an origin:

```kotlin
val origin = GeoPointImpl.fromLatLong(37.7749, -122.4194)

// Points around the origin
val north1km = Spherical.computeOffset(origin, 1000.0, 0.0)   // 1km north
val east1km = Spherical.computeOffset(origin, 1000.0, 90.0)   // 1km east
val south1km = Spherical.computeOffset(origin, 1000.0, 180.0) // 1km south
val west1km = Spherical.computeOffset(origin, 1000.0, 270.0)  // 1km west

// Create a square around the origin
val squarePoints = listOf(north1km, east1km, south1km, west1km, north1km)
```

### Reverse Calculation

```kotlin
fun computeOffsetOrigin(to: GeoPoint, distance: Double, heading: Double): GeoPointImpl?
```

Calculate the origin point given destination, distance, and original heading:

```kotlin
val destination = GeoPointImpl.fromLatLong(37.7849, -122.4094)
val distance = 1000.0  // 1km
val originalHeading = 45.0  // Northeast

val origin = Spherical.computeOffsetOrigin(destination, distance, originalHeading)
origin?.let { point ->
    println("Started from: ${point.latitude}, ${point.longitude}")
}
```

## Interpolation

### Great Circle Interpolation

```kotlin
fun interpolate(from: GeoPoint, to: GeoPoint, fraction: Double): GeoPointImpl
```

Interpolate along the great circle path between two points:

```kotlin
val start = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val end = GeoPointImpl.fromLatLong(40.7128, -74.0060)

// Create waypoints along the great circle route
val waypoints = (0..10).map { i ->
    val fraction = i / 10.0
    Spherical.interpolate(start, end, fraction)
}

// Use in a route animation
@Composable
fun AnimatedRoute() {
    var currentWaypoint by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (currentWaypoint < waypoints.size - 1) {
            delay(1000)
            currentWaypoint++
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Show route
        Polyline(
            points = waypoints,
            strokeColor = Color.Blue,
            strokeWidth = 3.dp
        )

        // Moving marker
        if (currentWaypoint < waypoints.size) {
            Marker(
                position = waypoints[currentWaypoint],
                icon = DefaultIcon(fillColor = Color.Red)
            )
        }
    }
}
```

### Linear Interpolation

```kotlin
fun linearInterpolate(from: GeoPoint, to: GeoPoint, fraction: Double): GeoPointImpl
```

Fast linear interpolation without considering Earth's curvature:

```kotlin
val start = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val end = GeoPointImpl.fromLatLong(37.7849, -122.4094)  // Short distance

// For small distances, linear interpolation is faster and sufficiently accurate
val midpoint = Spherical.linearInterpolate(start, end, 0.5)
```

## Area Calculations

### Polygon Area

```kotlin
fun computeArea(path: List<GeoPoint>): Double
fun computeSignedArea(path: List<GeoPoint>): Double
```

Calculate the area of a closed polygon:

```kotlin
val polygonPoints = listOf(
    GeoPointImpl.fromLatLong(37.7749, -122.4194),
    GeoPointImpl.fromLatLong(37.7849, -122.4094),
    GeoPointImpl.fromLatLong(37.7849, -122.4294),
    GeoPointImpl.fromLatLong(37.7749, -122.4294),
    GeoPointImpl.fromLatLong(37.7749, -122.4194)  // Close the polygon
)

val area = Spherical.computeArea(polygonPoints)
val areaKm2 = area / 1_000_000  // Convert to square kilometers

println("Polygon area: ${areaKm2.toInt()} km²")

// Signed area tells you orientation
val signedArea = Spherical.computeSignedArea(polygonPoints)
val orientation = if (signedArea > 0) "Counter-clockwise" else "Clockwise"
println("Polygon orientation: $orientation")
```

## Practical Examples

### Proximity Detection

```kotlin
@Composable
fun ProximityAlert() {
    val targetLocation = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val alertRadius = 500.0  // 500 meters

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var isNearTarget by remember { mutableStateOf(false) }

    // Update proximity when user location changes
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            val distance = Spherical.computeDistanceBetween(location, targetLocation)
            isNearTarget = distance <= alertRadius
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Target location
        Marker(
            position = targetLocation,
            icon = DefaultIcon(
                fillColor = if (isNearTarget) Color.Green else Color.Red,
                label = "Target"
            )
        )

        // Alert radius
        Circle(
            center = targetLocation,
            radiusMeters = alertRadius,
            strokeColor = Color.Blue,
            fillColor = Color.Blue.copy(alpha = 0.2f)
        )

        // User location if available
        userLocation?.let { location ->
            Marker(
                position = location,
                icon = DefaultIcon(fillColor = Color.Blue, label = "You")
            )
        }
    }

    if (isNearTarget) {
        Text(
            text = "You are near the target!",
            color = Color.Green,
            fontWeight = FontWeight.Bold
        )
    }
}
```

### Route Progress Tracking

```kotlin
@Composable
fun RouteProgress() {
    val route = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194),
        GeoPointImpl.fromLatLong(37.7849, -122.4094),
        GeoPointImpl.fromLatLong(37.7949, -122.3994)
    )

    var currentPosition by remember { mutableStateOf(route.first()) }
    val totalDistance = Spherical.computeLength(route)

    // Calculate progress along route
    fun calculateProgress(position: GeoPoint): Double {
        var distanceToPosition = 0.0
        var minDistanceToRoute = Double.MAX_VALUE
        var bestSegmentProgress = 0.0

        // Find closest point on route
        for (i in 0 until route.size - 1) {
            val segmentStart = route[i]
            val segmentEnd = route[i + 1]
            val segmentLength = Spherical.computeDistanceBetween(segmentStart, segmentEnd)

            // Find closest point on this segment
            var closestFraction = 0.0
            var minSegmentDistance = Double.MAX_VALUE

            for (fraction in 0..100) {
                val testFraction = fraction / 100.0
                val testPoint = Spherical.interpolate(segmentStart, segmentEnd, testFraction)
                val distance = Spherical.computeDistanceBetween(position, testPoint)

                if (distance < minSegmentDistance) {
                    minSegmentDistance = distance
                    closestFraction = testFraction
                }
            }

            if (minSegmentDistance < minDistanceToRoute) {
                minDistanceToRoute = minSegmentDistance
                bestSegmentProgress = distanceToPosition + (segmentLength * closestFraction)
            }

            distanceToPosition += segmentLength
        }

        return bestSegmentProgress / totalDistance
    }

    val progress = calculateProgress(currentPosition)

    Column {
        Text("Route Progress: ${(progress * 100).toInt()}%")
        LinearProgressIndicator(progress = progress.toFloat())

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
        MapView(state = mapViewState) {
            // Show route
            Polyline(
                points = route,
                strokeColor = Color.Blue,
                strokeWidth = 4.dp
            )

            // Current position
            Marker(
                position = currentPosition,
                icon = DefaultIcon(fillColor = Color.Red, label = "Current")
            )
        }
    }
}
```

### Geofencing

```kotlin
@Composable
fun GeofenceExample() {
    val geofenceCenter = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val geofenceRadius = 1000.0  // 1km

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var insideGeofence by remember { mutableStateOf(false) }

    // Check geofence status
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            val distance = Spherical.computeDistanceBetween(location, geofenceCenter)
            insideGeofence = distance <= geofenceRadius
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        // Geofence boundary
        Circle(
            center = geofenceCenter,
            radiusMeters = geofenceRadius,
            strokeColor = if (insideGeofence) Color.Green else Color.Red,
            strokeWidth = 3.dp,
            fillColor = Color.Blue.copy(alpha = 0.1f)
        )

        userLocation?.let { location ->
            Marker(
                position = location,
                icon = DefaultIcon(
                    fillColor = if (insideGeofence) Color.Green else Color.Red,
                    label = if (insideGeofence) "Inside" else "Outside"
                )
            )
        }
    }
}
```

## Performance Considerations

1. **Choose Appropriate Method**: Use `linearInterpolate` for short distances and frequent calculations
2. **Cache Results**: Store computed distances and bearings when possible
3. **Batch Calculations**: Process multiple points together to reduce function call overhead
4. **Precision vs Performance**: Consider if full spherical accuracy is needed for your use case

## Best Practices

1. **Coordinate Validation**: Ensure input coordinates are valid before calculations
2. **Error Handling**: Check for null results from `computeOffsetOrigin`
3. **Unit Consistency**: All distances are in meters, all angles in degrees
4. **Earth Model**: Remember this uses spherical Earth model, not the more accurate ellipsoidal model
5. **Precision**: Results are accurate for most mapping applications but may have small errors for high-precision surveying