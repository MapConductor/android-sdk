---
title: "Advanced Examples"
---

This section covers advanced patterns and techniques for building sophisticated mapping applications with MapConductor.

## Complex Interactions

### Multi-Selection with Info Panel

```kotlin
@Composable
fun MultiSelectionExample() {
    var selectedMarkers by remember { mutableStateOf<Set<String>>(emptySet()) }

    val markers = remember {
        listOf(
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(fillColor = Color.Blue, label = "A"),
                extra = "Location A"
            ),
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
                icon = DefaultIcon(fillColor = Color.Green, label = "B"),
                extra = "Location B"
            ),
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7649, -122.4294),
                icon = DefaultIcon(fillColor = Color.Red, label = "C"),
                extra = "Location C"
            )
        )
    }

    Row {
        // Map
        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
            modifier = Modifier.weight(1f),
            state = mapViewState,
            onMarkerClick = { markerState ->
                selectedMarkers = if (markerState.id in selectedMarkers) {
                    selectedMarkers - markerState.id
                } else {
                    selectedMarkers + markerState.id
                }
            }
        ) {
            markers.forEach { marker ->
                val isSelected = marker.id in selectedMarkers
                Marker(
                    state = marker.copy(
                        icon = DefaultIcon(
                            fillColor = if (isSelected) Color.Yellow else marker.icon?.let {
                                when (marker.extra) {
                                    "Location A" -> Color.Blue
                                    "Location B" -> Color.Green
                                    else -> Color.Red
                                }
                            } ?: Color.Gray,
                            label = marker.extra?.toString()?.last()?.toString() ?: "?",
                            scale = if (isSelected) 1.3f else 1.0f
                        )
                    )
                )
            }
        }

        // Info Panel
        Card(modifier = Modifier.width(200.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Selected: ${selectedMarkers.size}")
                selectedMarkers.forEach { markerId ->
                    val marker = markers.find { it.id == markerId }
                    marker?.let {
                        Text("• ${it.extra}")
                    }
                }
            }
        }
    }
}
```

### Dynamic Layer Management

```kotlin
@Composable
fun LayerManagementExample() {
    var visibleLayers by remember { mutableStateOf(setOf("markers", "areas")) }

    val layerConfig = mapOf(
        "markers" to "Show Markers",
        "areas" to "Show Areas",
        "routes" to "Show Routes",
        "heatmap" to "Show Heatmap"
    )

    Column {
        // Layer controls
        Card(modifier = Modifier.padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Layers", style = MaterialTheme.typography.h6)
                layerConfig.forEach { (layerId, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = layerId in visibleLayers,
                            onCheckedChange = { checked ->
                                visibleLayers = if (checked) {
                                    visibleLayers + layerId
                                } else {
                                    visibleLayers - layerId
                                }
                            }
                        )
                        Text(label)
                    }
                }
            }
        }

        // Map with conditional layers
        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
            // Markers layer
            if ("markers" in visibleLayers) {
                repeat(10) { i ->
                    Marker(
                        position = GeoPointImpl.fromLatLong(
                            37.7749 + i * 0.01,
                            -122.4194 + i * 0.01
                        ),
                        icon = DefaultIcon(fillColor = Color.Blue, label = "$i")
                    )
                }
            }

            // Areas layer
            if ("areas" in visibleLayers) {
                repeat(3) { i ->
                    Circle(
                        center = GeoPointImpl.fromLatLong(
                            37.7749 + i * 0.02,
                            -122.4194 + i * 0.02
                        ),
                        radius = 500.0 + i * 200,
                        strokeColor = Color.Red,
                        fillColor = Color.Red.copy(alpha = 0.2f)
                    )
                }
            }

            // Routes layer
            if ("routes" in visibleLayers) {
                Polyline(
                    points = listOf(
                        GeoPointImpl.fromLatLong(37.7749, -122.4194),
                        GeoPointImpl.fromLatLong(37.7849, -122.4094),
                        GeoPointImpl.fromLatLong(37.7949, -122.3994)
                    ),
                    strokeColor = Color.Green,
                    strokeWidth = 4.dp
                )
            }

            // Heatmap layer (simulated with circles)
            if ("heatmap" in visibleLayers) {
                repeat(20) { i ->
                    Circle(
                        center = GeoPointImpl.fromLatLong(
                            37.7749 + Random.nextFloat() * 0.02,
                            -122.4194 + Random.nextFloat() * 0.02
                        ),
                        radius = 100.0,
                        strokeColor = Color.Transparent,
                        fillColor = Color.Red.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
```

## Performance Optimization

### Marker Clustering

```kotlin
data class MarkerCluster(
    val center: GeoPoint,
    val markers: List<MarkerState>,
    val radius: Double
) : java.io.Serializable

@Composable
fun MarkerClusteringExample() {
    var zoomLevel by remember { mutableStateOf(10f) }

    val allMarkers = remember {
        List(200) { i ->
            MarkerState(
                position = GeoPointImpl.fromLatLong(
                    37.7749 + (Random.nextFloat() - 0.5f) * 0.1,
                    -122.4194 + (Random.nextFloat() - 0.5f) * 0.1
                ),
                extra = "Marker $i"
            )
        }
    }

    val displayedItems = remember(zoomLevel) {
        if (zoomLevel > 13) {
            // Show individual markers at high zoom
            allMarkers.map { Either.Left(it) }
        } else {
            // Cluster at low zoom
            clusterMarkers(allMarkers, 1000.0 / zoomLevel).map { Either.Right(it) }
        }
    }

    Column {
        Slider(
            value = zoomLevel,
            onValueChange = { zoomLevel = it },
            valueRange = 8f..16f,
            modifier = Modifier.padding(16.dp)
        )
        Text("Zoom: ${zoomLevel.toInt()}")

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
            displayedItems.forEach { item ->
                when (item) {
                    is Either.Left -> {
                        // Individual marker
                        Marker(item.value)
                    }
                    is Either.Right -> {
                        // Cluster
                        val cluster = item.value
                        Marker(
                            position = cluster.center,
                            icon = DefaultIcon(
                                fillColor = Color.Red,
                                label = cluster.markers.size.toString(),
                                scale = 1.2f
                            ),
                            extra = "Cluster of ${cluster.markers.size} markers"
                        )
                    }
                }
            }
        }
    }
}

sealed class Either<out L, out R> : java.io.Serializable {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

fun clusterMarkers(markers: List<MarkerState>, clusterRadius: Double): List<MarkerCluster> {
    val clusters = mutableListOf<MarkerCluster>()
    val unclustered = markers.toMutableList()

    while (unclustered.isNotEmpty()) {
        val seed = unclustered.removeFirst()
        val clusterMembers = mutableListOf(seed)

        val iterator = unclustered.iterator()
        while (iterator.hasNext()) {
            val marker = iterator.next()
            if (calculateDistance(seed.position, marker.position) <= clusterRadius) {
                clusterMembers.add(marker)
                iterator.remove()
            }
        }

        val center = calculateCentroid(clusterMembers.map { it.position })
        clusters.add(MarkerCluster(center, clusterMembers, clusterRadius))
    }

    return clusters
}

fun calculateCentroid(points: List<GeoPoint>): GeoPoint {
    val avgLat = points.map { it.latitude }.average()
    val avgLng = points.map { it.longitude }.average()
    return GeoPointImpl.fromLatLong(avgLat, avgLng)
}
```

### Viewport-based Loading

```kotlin
@Composable
fun ViewportBasedLoadingExample() {
    var currentViewport by remember { mutableStateOf<GeoRectBounds?>(null) }
    var visibleMarkers by remember { mutableStateOf<List<MarkerState>>(emptyList()) }

    // Simulate large dataset
    val allMarkers = remember {
        List(1000) { i ->
            MarkerState(
                position = GeoPointImpl.fromLatLong(
                    37.5 + Random.nextFloat() * 0.5,
                    -122.7 + Random.nextFloat() * 0.5
                ),
                extra = "Marker $i"
            )
        }
    }

    LaunchedEffect(currentViewport) {
        currentViewport?.let { viewport ->
            visibleMarkers = allMarkers.filter { marker ->
                marker.position.latitude >= viewport.southwest.latitude &&
                marker.position.latitude <= viewport.northeast.latitude &&
                marker.position.longitude >= viewport.southwest.longitude &&
                marker.position.longitude <= viewport.northeast.longitude
            }
        }
    }

    Column {
        Text("Visible markers: ${visibleMarkers.size} / ${allMarkers.size}")

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
            state = mapViewState,
            onMapLoaded = {
                // Set initial viewport
                currentViewport = GeoRectBounds(
                    southwest = GeoPointImpl.fromLatLong(37.7, -122.5),
                    northeast = GeoPointImpl.fromLatLong(37.8, -122.3)
                )
            }
        ) {
            visibleMarkers.forEach { marker ->
                Marker(marker)
            }

            // Show viewport bounds
            currentViewport?.let { viewport ->
                val bounds = listOf(
                    viewport.southwest,
                    GeoPointImpl.fromLatLong(viewport.southwest.latitude, viewport.northeast.longitude),
                    viewport.northeast,
                    GeoPointImpl.fromLatLong(viewport.northeast.latitude, viewport.southwest.longitude),
                    viewport.southwest
                )

                Polyline(
                    points = bounds,
                    strokeColor = Color.Red,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
```

## Custom Components

### Custom Marker with Animation

```kotlin
@Composable
fun AnimatedMarkerExample() {
    var isAnimating by remember { mutableStateOf(false) }
    var markerScale by remember { mutableStateOf(1.0f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            while (isAnimating) {
                delay(100)
                markerScale = 1.0f + sin(System.currentTimeMillis() / 300.0).toFloat() * 0.3f
            }
        } else {
            markerScale = 1.0f
        }
    }

    Column {
        Button(onClick = { isAnimating = !isAnimating }) {
            Text(if (isAnimating) "Stop Animation" else "Start Animation")
        }

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(
                    fillColor = Color.Red,
                    label = "🎯",
                    scale = markerScale
                ),
                extra = "Animated marker"
            )
        }
    }
}
```

### Information Overlay System

```kotlin
@Composable
fun InformationOverlayExample() {
    var selectedFeature by remember { mutableStateOf<String?>(null) }
    var overlayPosition by remember { mutableStateOf<GeoPoint?>(null) }

    val features = mapOf(
        "restaurant" to GeoPointImpl.fromLatLong(37.7749, -122.4194),
        "hotel" to GeoPointImpl.fromLatLong(37.7849, -122.4094),
        "museum" to GeoPointImpl.fromLatLong(37.7649, -122.4294)
    )

    Box {
        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
            state = mapViewState,
            onMarkerClick = { markerState ->
                selectedFeature = markerState.extra as? String
                overlayPosition = markerState.position
            },
            onMapClick = {
                selectedFeature = null
                overlayPosition = null
            }
        ) {
            features.forEach { (type, position) ->
                Marker(
                    position = position,
                    icon = DefaultIcon(
                        fillColor = when (type) {
                            "restaurant" -> Color.Red
                            "hotel" -> Color.Blue
                            "museum" -> Color.Green
                            else -> Color.Gray
                        },
                        label = when (type) {
                            "restaurant" -> "🍽️"
                            "hotel" -> "🏨"
                            "museum" -> "🏛️"
                            else -> "?"
                        }
                    ),
                    extra = type
                )
            }
        }

        // Overlay information panel
        selectedFeature?.let { feature ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .width(200.dp),
                elevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = feature.capitalize(),
                        style = MaterialTheme.typography.h6
                    )
                    Text("Detailed information about this $feature")

                    Row {
                        Button(
                            onClick = { /* Navigate action */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Navigate")
                        }

                        Button(
                            onClick = { /* More info action */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Info")
                        }
                    }
                }
            }
        }
    }
}
```

## Data Integration

### Real-time Data Updates

```kotlin
@Composable
fun RealTimeDataExample() {
    var vehiclePositions by remember { mutableStateOf<Map<String, GeoPoint>>(emptyMap()) }
    var lastUpdate by remember { mutableStateOf<Long>(0) }

    // Simulate real-time vehicle tracking
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000) // Update every 2 seconds

            val newPositions = mutableMapOf<String, GeoPoint>()
            repeat(5) { i ->
                val vehicleId = "vehicle_$i"
                val currentPos = vehiclePositions[vehicleId] ?: GeoPointImpl.fromLatLong(
                    37.7749 + Random.nextFloat() * 0.02,
                    -122.4194 + Random.nextFloat() * 0.02
                )

                // Move vehicle slightly
                val newLat = currentPos.latitude + (Random.nextFloat() - 0.5f) * 0.002
                val newLng = currentPos.longitude + (Random.nextFloat() - 0.5f) * 0.002
                newPositions[vehicleId] = GeoPointImpl.fromLatLong(newLat, newLng)
            }

            vehiclePositions = newPositions
            lastUpdate = System.currentTimeMillis()
        }
    }

    Column {
        Text("Last update: ${Date(lastUpdate)}")
        Text("Vehicles: ${vehiclePositions.size}")

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
            vehiclePositions.forEach { (vehicleId, position) ->
                Marker(
                    position = position,
                    icon = DefaultIcon(
                        fillColor = Color.Blue,
                        label = "🚗",
                        scale = 1.2f
                    ),
                    extra = vehicleId
                )

                // Vehicle range circle
                Circle(
                    center = position,
                    radius = 200.0,
                    strokeColor = Color.Blue.copy(alpha = 0.5f),
                    fillColor = Color.Blue.copy(alpha = 0.1f)
                )
            }
        }
    }
}
```

### Geofencing

```kotlin
@Composable
fun GeofencingExample() {
    var userPosition by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7749, -122.4194))
    }
    var alerts by remember { mutableStateOf<List<String>>(emptyList()) }

    val geofences = listOf(
        Triple(
            GeoPointImpl.fromLatLong(37.7800, -122.4100),
            300.0,
            "Downtown Zone"
        ),
        Triple(
            GeoPointImpl.fromLatLong(37.7700, -122.4300),
            200.0,
            "Restricted Area"
        )
    )

    // Check geofence violations
    LaunchedEffect(userPosition) {
        val newAlerts = mutableListOf<String>()
        geofences.forEach { (center, radius, name) ->
            val distance = calculateDistance(userPosition, center)
            if (distance <= radius) {
                newAlerts.add("Entered: $name")
            }
        }
        alerts = newAlerts
    }

    Column {
        if (alerts.isNotEmpty()) {
            Card(
                backgroundColor = Color.Yellow.copy(alpha = 0.3f),
                modifier = Modifier.padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Geofence Alerts:")
                    alerts.forEach { alert ->
                        Text("• $alert")
                    }
                }
            }
        }

        // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(
            state = mapViewState,
            onMapClick = { geoPoint ->
                userPosition = geoPoint
            }
        ) {
            // Draw geofences
            geofences.forEach { (center, radius, name) ->
                Circle(
                    center = center,
                    radius = radius,
                    strokeColor = Color.Red,
                    strokeWidth = 2.dp,
                    fillColor = Color.Red.copy(alpha = 0.1f),
                    extra = name
                )

                Marker(
                    position = center,
                    icon = DefaultIcon(
                        fillColor = Color.Red,
                        label = name.first().toString(),
                        scale = 0.8f
                    )
                )
            }

            // User position
            Marker(
                position = userPosition,
                icon = DefaultIcon(
                    fillColor = Color.Blue,
                    label = "👤",
                    scale = 1.2f
                ),
                draggable = true
            )
        }
    }
}
```