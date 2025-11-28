---
title: "Basic Usage Examples"
---

This section provides practical examples of using MapConductor components in common scenarios.

## Getting Started

### Simple Map with Marker

```kotlin
@Composable
fun SimpleMapExample() {
    val mapViewState = rememberGoogleMapViewState()

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(label = "SF"),
            extra = "San Francisco"
        )
    }
}
```

### Multi-Provider Map

```kotlin
@Composable
fun MultiProviderExample() {
    var provider by remember { mutableStateOf("google") }

    val mapViewState = remember(provider) {
        when (provider) {
            "google" -> rememberGoogleMapViewState()
            "mapbox" -> rememberMapboxMapViewState()
            "here" -> rememberHereMapViewState()
            "arcgis" -> rememberArcGISMapViewState()
            else -> rememberGoogleMapViewState()
        }
    }

    Column {
        Row {
            Button(onClick = { provider = "google" }) { Text("Google") }
            Button(onClick = { provider = "mapbox" }) { Text("Mapbox") }
            Button(onClick = { provider = "here" }) { Text("HERE") }
            Button(onClick = { provider = "arcgis" }) { Text("ArcGIS") }
        }

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(label = provider.uppercase())
            )
        }
    }
}
```

## Interactive Examples

### Click and Add Markers

```kotlin
@Composable
fun ClickToAddMarkersExample() {
    var markers by remember { mutableStateOf<List<MarkerState>>(emptyList()) }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            val newMarker = MarkerState(
                position = geoPoint,
                icon = DefaultIcon(
                    label = "${markers.size + 1}",
                    fillColor = Color.Blue
                ),
                extra = "Marker ${markers.size + 1}"
            )
            markers = markers + newMarker
        },
        onMarkerClick = { markerState ->
            // Remove clicked marker
            markers = markers.filter { it.id != markerState.id }
        }
    ) {
        markers.forEach { marker ->
            Marker(marker)
        }
    }
}
```

### Route Planning

```kotlin
@Composable
fun RoutePlanningExample() {
    var waypoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            waypoints = waypoints + geoPoint
        }
    ) {
        // Draw route if we have multiple points
        if (waypoints.size >= 2) {
            Polyline(
                points = waypoints,
                strokeColor = Color.Blue,
                strokeWidth = 4.dp
            )
        }

        // Draw waypoint markers
        waypoints.forEachIndexed { index, point ->
            Marker(
                position = point,
                icon = DefaultIcon(
                    fillColor = when (index) {
                        0 -> Color.Green
                        waypoints.size - 1 -> Color.Red
                        else -> Color.Blue
                    },
                    label = "${index + 1}",
                    scale = 0.8f
                )
            )
        }
    }
}
```

## Area Management

### Zone Drawing

```kotlin
@Composable
fun ZoneDrawingExample() {
    var zones by remember { mutableStateOf<List<PolygonState>>(emptyList()) }
    var currentZone by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var isDrawing by remember { mutableStateOf(false) }

    Column {
        Row {
            Button(
                onClick = {
                    isDrawing = true
                    currentZone = emptyList()
                }
            ) {
                Text("Start Zone")
            }

            Button(
                onClick = {
                    if (currentZone.size >= 3) {
                        val closedZone = currentZone + currentZone.first()
                        zones = zones + PolygonState(
                            points = closedZone,
                            strokeColor = Color.Red,
                            fillColor = Color.Red.copy(alpha = 0.3f),
                            extra = "Zone ${zones.size + 1}"
                        )
                    }
                    isDrawing = false
                    currentZone = emptyList()
                }
            ) {
                Text("Finish Zone")
            }

            Button(onClick = { zones = emptyList() }) {
                Text("Clear All")
            }
        }

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
            state = mapViewState,
            onMapClick = { geoPoint ->
                if (isDrawing) {
                    currentZone = currentZone + geoPoint
                }
            }
        ) {
            // Draw completed zones
            zones.forEach { zone ->
                Polygon(zone)
            }

            // Draw current zone being created
            if (currentZone.size >= 3) {
                Polygon(
                    points = currentZone + currentZone.first(),
                    strokeColor = Color.Gray,
                    fillColor = Color.Gray.copy(alpha = 0.1f)
                )
            }

            // Draw current zone vertices
            currentZone.forEachIndexed { index, point ->
                Marker(
                    position = point,
                    icon = DefaultIcon(
                        fillColor = Color.Orange,
                        label = "${index + 1}",
                        scale = 0.6f
                    )
                )
            }
        }
    }
}
```

## Data Visualization

### Heat Map Simulation

```kotlin
@Composable
fun HeatMapExample() {
    val dataPoints = remember {
        List(50) {
            val lat = 37.7749 + (Random.nextFloat() - 0.5f) * 0.02
            val lng = -122.4194 + (Random.nextFloat() - 0.5f) * 0.02
            val intensity = Random.nextFloat()
            Triple(GeoPointImpl.fromLatLong(lat, lng), intensity, it)
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
        dataPoints.forEach { (point, intensity, id) ->
            Circle(
                center = point,
                radiusMeters = intensity * 200.0,
                strokeColor = Color.Transparent,
                fillColor = Color.Red.copy(alpha = intensity * 0.5f),
                extra = "Data point $id"
            )
        }
    }
}
```

### Clustering Simulation

```kotlin
@Composable
fun ClusteringExample() {
    val zoom by remember { mutableStateOf(10f) }

    val markers = remember {
        List(100) {
            val lat = 37.7749 + (Random.nextFloat() - 0.5f) * 0.1
            val lng = -122.4194 + (Random.nextFloat() - 0.5f) * 0.1
            GeoPointImpl.fromLatLong(lat, lng)
        }
    }

    // Simple clustering based on distance
    val clusteredMarkers = remember(zoom) {
        if (zoom < 12) {
            // Group nearby markers
            val clusters = mutableListOf<Pair<GeoPoint, Int>>()
            val processed = mutableSetOf<Int>()

            markers.forEachIndexed { index, marker ->
                if (index !in processed) {
                    val cluster = markers.filterIndexed { i, other ->
                        if (i !in processed) {
                            val distance = calculateDistance(marker, other)
                            distance < 1000 // 1km clustering
                        } else false
                    }
                    cluster.forEach { m ->
                        processed.add(markers.indexOf(m))
                    }
                    clusters.add(marker to cluster.size)
                }
            }
            clusters
        } else {
            markers.map { it to 1 }
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
        clusteredMarkers.forEach { (position, count) ->
            Marker(
                position = position,
                icon = DefaultIcon(
                    fillColor = if (count > 1) Color.Red else Color.Blue,
                    label = if (count > 1) count.toString() else "•",
                    scale = if (count > 1) 1.2f else 0.8f
                )
            )
        }
    }
}
```

## Real-time Updates

### Live Tracking

```kotlin
@Composable
fun LiveTrackingExample() {
    var currentPosition by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7749, -122.4194))
    }
    var trail by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    // Simulate movement
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val newLat = currentPosition.latitude + (Random.nextFloat() - 0.5f) * 0.001
            val newLng = currentPosition.longitude + (Random.nextFloat() - 0.5f) * 0.001
            val newPosition = GeoPointImpl.fromLatLong(newLat, newLng)

            trail = (trail + currentPosition).takeLast(20) // Keep last 20 points
            currentPosition = newPosition
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
        // Trail
        if (trail.size >= 2) {
            Polyline(
                points = trail,
                strokeColor = Color.Blue.copy(alpha = 0.7f),
                strokeWidth = 3.dp
            )
        }

        // Current position
        Marker(
            position = currentPosition,
            icon = DefaultIcon(
                fillColor = Color.Red,
                label = "📍",
                scale = 1.2f
            )
        )

        // Accuracy circle
        Circle(
            center = currentPosition,
            radiusMeters = 50.0,
            strokeColor = Color.Blue.copy(alpha = 0.5f),
            fillColor = Color.Blue.copy(alpha = 0.1f)
        )
    }
}
```

## Utility Functions

```kotlin
fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
    // Simplified distance calculation in meters
    val latDiff = point1.latitude - point2.latitude
    val lngDiff = point1.longitude - point2.longitude
    return sqrt(latDiff * latDiff + lngDiff * lngDiff) * 111000 // Rough conversion
}

fun GeoPointImpl.Companion.fromLatLong(lat: Double, lng: Double): GeoPointImpl {
    return GeoPointImpl(lat, lng)
}
```