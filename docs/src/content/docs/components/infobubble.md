---
title: "InfoBubble"
---

InfoBubble is a component that displays custom content in a speech bubble attached to a marker on the map. It provides a way to show detailed information about markers without cluttering the map interface.

## Overview

InfoBubble creates a floating overlay that appears above a specific marker, with customizable styling and content. The bubble automatically positions itself relative to the marker and follows the marker's position when the map is panned or zoomed.

## Composable Function

```kotlin
@Composable
fun MapViewScope.InfoBubble(
    marker: MarkerState,
    bubbleColor: Color = Color.White,
    borderColor: Color = Color.Black,
    contentPadding: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    tailSize: Dp = 8.dp,
    content: @Composable () -> Unit
)
```

## Parameters

- **`marker`**: The `MarkerState` to which the bubble is attached
- **`bubbleColor`**: Background color of the bubble (default: White)
- **`borderColor`**: Border color of the bubble (default: Black)
- **`contentPadding`**: Internal padding around the content (default: 8dp)
- **`cornerRadius`**: Radius of the bubble's rounded corners (default: 4dp)
- **`tailSize`**: Size of the speech bubble tail pointing to the marker (default: 8dp)
- **`content`**: Composable content to display inside the bubble

## Basic Usage

### Simple Text Bubble

```kotlin
@Composable
fun SimpleInfoBubbleExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    MapView(
        state = mapViewState,
        onMapClick = { selectedMarker = null },
        onMarkerClick = { markerState -> selectedMarker = markerState }
    ) {
        val markerState = MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Blue, label = "SF"),
            extra = "San Francisco - The Golden Gate City",
            id = "sf-marker"
        )

        Marker(markerState)

        // Show info bubble for selected marker
        selectedMarker?.let { marker ->
            InfoBubble(marker = marker) {
                Text(
                    text = marker.extra as? String ?: "No information",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}
```

### Custom Styled Bubble

```kotlin
@Composable
fun StyledInfoBubbleExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }
    val isDarkTheme = isSystemInDarkTheme()

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    MapView(
        state = mapViewState,
        onMapClick = { selectedMarker = null },
        onMarkerClick = { markerState -> selectedMarker = markerState }
    ) {
        val markerState = MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Green, label = "POI"),
            extra = "Point of Interest",
            id = "poi-marker"
        )

        Marker(markerState)

        selectedMarker?.let { marker ->
            InfoBubble(
                marker = marker,
                bubbleColor = if (isDarkTheme) Color.Black else Color.White,
                borderColor = if (isDarkTheme) Color.Gray else Color.Black,
                contentPadding = 12.dp,
                cornerRadius = 8.dp,
                tailSize = 10.dp
            ) {
                Text(
                    text = marker.extra as? String ?: "",
                    color = if (isDarkTheme) Color.White else Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
```

## Advanced Usage

### Rich Content Bubble

```kotlin
data class LocationInfo(
    val name: String,
    val description: String,
    val rating: Float,
    val imageUrl: String? = null
) : java.io.Serializable

@Composable
fun RichContentBubbleExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    MapView(
        state = mapViewState,
        onMapClick = { selectedMarker = null },
        onMarkerClick = { markerState -> selectedMarker = markerState }
    ) {
        val locationInfo = LocationInfo(
            name = "Golden Gate Park",
            description = "A large urban park with gardens, museums, and recreational areas.",
            rating = 4.5f
        )

        val markerState = MarkerState(
            position = GeoPointImpl.fromLatLong(37.7694, -122.4862),
            icon = DefaultIcon(fillColor = Color.Green, label = "🌳"),
            extra = locationInfo,
            id = "park-marker"
        )

        Marker(markerState)

        selectedMarker?.let { marker ->
            val info = marker.extra as? LocationInfo
            info?.let {
                InfoBubble(
                    marker = marker,
                    bubbleColor = Color.White,
                    borderColor = Color.Gray,
                    contentPadding = 16.dp,
                    cornerRadius = 12.dp
                ) {
                    Column(
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text(
                            text = info.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = info.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (index < info.rating.toInt()) Color.Yellow else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = " ${info.rating}/5",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
```

### Interactive Bubble with Actions

```kotlin
@Composable
fun InteractiveBubbleExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }
    val context = LocalContext.current

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    MapView(
        state = mapViewState,
        onMapClick = { selectedMarker = null },
        onMarkerClick = { markerState -> selectedMarker = markerState }
    ) {
        val storeInfo = StoreInfo(
            name = "Coffee Shop",
            address = "123 Main St, San Francisco",
            phone = "+1 (555) 123-4567",
            type = "coffee"
        )

        val markerState = MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color(0xFF8B4513), label = "☕"),
            extra = storeInfo,
            id = "coffee-shop-marker"
        )

        Marker(markerState)

        selectedMarker?.let { marker ->
            val store = marker.extra as? StoreInfo
            store?.let {
                InfoBubble(
                    marker = marker,
                    bubbleColor = Color.White,
                    borderColor = Color.Black,
                    contentPadding = 12.dp,
                    cornerRadius = 8.dp
                ) {
                    Column(
                        modifier = Modifier.width(250.dp)
                    ) {
                        Text(
                            text = store.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = store.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    // Handle call action
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${store.phone}")
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call")
                                Text("Call", modifier = Modifier.padding(start = 4.dp))
                            }

                            Button(
                                onClick = {
                                    // Handle directions action
                                    val position = marker.position
                                    val gmmIntentUri = Uri.parse("google.navigation:q=${position.latitude},${position.longitude}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = "Directions")
                                Text("Go", modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

data class StoreInfo(
    val name: String,
    val address: String,
    val phone: String,
    val type: String
) : java.io.Serializable
```

### Multiple Bubbles Management

```kotlin
@Composable
fun MultipleBubblesExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarkers by remember { mutableStateOf(setOf<String>()) }

    val markerData = remember {
        listOf(
            Triple(GeoPointImpl.fromLatLong(37.7749, -122.4194), "Restaurant A", Color.Red),
            Triple(GeoPointImpl.fromLatLong(37.7849, -122.4094), "Hotel B", Color.Blue),
            Triple(GeoPointImpl.fromLatLong(37.7649, -122.4294), "Shop C", Color.Green)
        )
    }

    val markerStates = remember {
        markerData.mapIndexed { index, (position, name, color) ->
            MarkerState(
                id = "marker_$index",
                position = position,
                icon = DefaultIcon(fillColor = color, label = "${index + 1}"),
                extra = name
            )
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    MapView(
        state = mapViewState,
        onMapClick = {
            selectedMarkers = emptySet() // Clear all selections
        },
        onMarkerClick = { markerState ->
            selectedMarkers = if (selectedMarkers.contains(markerState.id)) {
                selectedMarkers - markerState.id // Deselect
            } else {
                selectedMarkers + markerState.id // Select
            }
        }
    ) {
        markerStates.forEach { markerState ->
            Marker(markerState)

            // Show bubble if marker is selected
            if (selectedMarkers.contains(markerState.id)) {
                InfoBubble(
                    marker = markerState,
                    bubbleColor = Color.White,
                    borderColor = Color.Black
                ) {
                    Column {
                        Text(
                            text = markerState.extra as String,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to close",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
```

## Positioning and Behavior

### Automatic Positioning

InfoBubble automatically positions itself above the associated marker:

- The bubble's tail points to the center of the marker
- The bubble adjusts its position to stay visible within the map viewport
- The bubble follows the marker when the map is panned or zoomed

### Custom Positioning

While InfoBubble automatically handles positioning, you can influence it through marker icon anchoring:

```kotlin
// Marker with bottom-center anchor - bubble appears above
val markerWithBottomAnchor = MarkerState(
    position = position,
    icon = ImageDefaultIcon(
        drawable = customIcon,
        anchor = Offset(0.5f, 1.0f) // Bottom center
    )
)

// Marker with center anchor - bubble appears above center
val markerWithCenterAnchor = MarkerState(
    position = position,
    icon = ImageDefaultIcon(
        drawable = customIcon,
        anchor = Offset(0.5f, 0.5f) // Center
    )
)
```

## Lifecycle Management

InfoBubble automatically manages its lifecycle:

```kotlin
// Bubble appears when component is composed
selectedMarker?.let { marker ->
    InfoBubble(marker = marker) {
        // Content
    }
}

// Bubble disappears when component is removed from composition
// This happens automatically when selectedMarker becomes null
```

### Manual Lifecycle Control

```kotlin
@Composable
fun ManualLifecycleExample() {
    var showBubble by remember { mutableStateOf(false) }
    val markerState = remember { /* marker state */ }

    LaunchedEffect(showBubble) {
        if (showBubble) {
            delay(3000) // Show for 3 seconds
            showBubble = false
        }
    }

    // Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView
    MapView(state = mapViewState) {
        Marker(markerState)

        if (showBubble) {
            InfoBubble(marker = markerState) {
                Text("Auto-hiding bubble")
            }
        }
    }
}
```

## Styling and Theming

### Dark Mode Support

```kotlin
@Composable
fun DarkModeInfoBubble(marker: MarkerState) {
    val isDarkTheme = isSystemInDarkTheme()

    InfoBubble(
        marker = marker,
        bubbleColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color.White,
        borderColor = if (isDarkTheme) Color(0xFF555555) else Color.Black,
        contentPadding = 12.dp,
        cornerRadius = 8.dp
    ) {
        Text(
            text = marker.extra as? String ?: "",
            color = if (isDarkTheme) Color.White else Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
```

### Custom Themes

```kotlin
data class BubbleTheme(
    val backgroundColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val contentPadding: Dp,
    val cornerRadius: Dp,
    val tailSize: Dp
) : java.io.Serializable

val DefaultBubbleTheme = BubbleTheme(
    backgroundColor = Color.White,
    borderColor = Color.Black,
    textColor = Color.Black,
    contentPadding = 8.dp,
    cornerRadius = 4.dp,
    tailSize = 8.dp
)

val DarkBubbleTheme = BubbleTheme(
    backgroundColor = Color(0xFF2D2D2D),
    borderColor = Color(0xFF555555),
    textColor = Color.White,
    contentPadding = 12.dp,
    cornerRadius = 8.dp,
    tailSize = 10.dp
)

@Composable
fun ThemedInfoBubble(
    marker: MarkerState,
    theme: BubbleTheme = DefaultBubbleTheme,
    content: @Composable () -> Unit
) {
    InfoBubble(
        marker = marker,
        bubbleColor = theme.backgroundColor,
        borderColor = theme.borderColor,
        contentPadding = theme.contentPadding,
        cornerRadius = theme.cornerRadius,
        tailSize = theme.tailSize,
        content = content
    )
}
```

## Performance Considerations

### Efficient Updates

```kotlin
// Good: Use stable keys for markers
val markerStates = remember(markersData) {
    markersData.map { data ->
        MarkerState(
            id = "marker_${data.id}", // Stable ID
            position = data.position,
            extra = data
        )
    }
}

// Avoid: Creating new marker states on each recomposition
val markerStates = markersData.map { data ->
    MarkerState(position = data.position, extra = data) // New instance each time
}
```

### Memory Management

```kotlin
// Clear selected markers when leaving screen
DisposableEffect(Unit) {
    onDispose {
        selectedMarker = null // Clears InfoBubble
    }
}
```

## Best Practices

### Design Guidelines

1. **Keep Content Concise**: InfoBubbles should provide essential information without overwhelming users
2. **Use Appropriate Sizing**: Limit bubble width to maintain readability on mobile devices
3. **Provide Clear Actions**: If including buttons, make their purpose obvious
4. **Consider Touch Targets**: Ensure interactive elements meet minimum touch target sizes

### User Experience

1. **Dismiss Behavior**: Allow users to dismiss bubbles by tapping the map or marker
2. **Loading States**: Show loading indicators for content that requires network requests
3. **Error Handling**: Gracefully handle missing or invalid data
4. **Accessibility**: Provide content descriptions for screen readers

### Implementation Tips

```kotlin
// Good: Stable marker references
val markerState = remember(markerId) {
    MarkerState(id = markerId, position = position)
}

// Good: Efficient content updates
LaunchedEffect(selectedMarkerId) {
    if (selectedMarkerId != null) {
        // Load additional data if needed
    }
}

// Avoid: Heavy computations in bubble content
InfoBubble(marker = marker) {
    // Avoid expensive operations here
    val processedData = remember(marker.extra) {
        processData(marker.extra) // Move to remember
    }
    DisplayContent(processedData)
}
```

## Troubleshooting

### Common Issues

1. **Bubble Not Appearing**: Verify marker is properly composed and InfoBubble is inside MapViewScope
2. **Bubble Not Dismissing**: Check that the conditional rendering properly responds to state changes
3. **Poor Performance**: Limit number of simultaneous bubbles and optimize content composition
4. **Layout Issues**: Use appropriate sizing constraints and test on different screen sizes

### Debug Mode

```kotlin
// Enable debug logging for InfoBubble positioning
InfoBubble(
    marker = marker,
    bubbleColor = Color.Yellow.copy(alpha = 0.8f), // Highlight for debugging
    borderColor = Color.Red
) {
    Column {
        Text("Marker ID: ${marker.id}")
        Text("Position: ${marker.position}")
        // Your actual content
    }
}
```