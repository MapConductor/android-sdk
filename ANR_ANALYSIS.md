# ANR Analysis and Solutions

## Problem
Map initialization takes 10 seconds and causes ANR with 1400+ markers, even with background service.

## Root Causes

### 1. Synchronous Data Loading (`PostOfficePage.kt:25-43`)
- All 1400+ markers created in single `LaunchedEffect`
- Blocks main thread during marker state creation

### 2. Immediate Spatial Processing (`RemoteSpatialMarkerRenderingStrategy.kt`)
- `onAdd()` processes all markers synchronously
- No progressive rendering or chunking

### 3. Bulk Overlay Rendering (`OverlayProvider.kt:68-74`)
- Entire marker collection rendered at once
- No batching or rate limiting

## Solutions

### 1. Progressive Loading
```kotlin
// In PostOfficePage.kt
LaunchedEffect(Unit) {
    val chunks = postOffices.chunked(50) // Process 50 at a time
    chunks.forEach { chunk ->
        val markerStates = chunk.map { createMarkerState(it) }
        viewModel.addMarkers(markerStates) // Add incrementally
        delay(16) // One frame delay
    }
}
```

### 2. Async Spatial Processing
```kotlin
// In RemoteSpatialMarkerRenderingStrategy.kt
override suspend fun onAdd(data: List<MarkerState>, viewport: GeoRectBounds, renderer: MarkerOverlayRenderer<ActualMarker>): Boolean {
    return withContext(Dispatchers.Default) {
        val chunks = data.chunked(100)
        chunks.forEach { chunk ->
            val visibleMarkers = chunk.filter { viewport.contains(it.position) }
            withContext(Dispatchers.Main) {
                renderer.onAdd(visibleMarkers)
            }
            yield() // Allow other coroutines
        }
        true
    }
}
```

### 3. Debounced Rendering
```kotlin
// In OverlayProvider.kt
LaunchedEffect(Unit) {
    typedOverlay.flow
        .debounce(100) // Debounce updates
        .collect { items ->
            if (items.isNotEmpty()) {
                // Process in chunks
                items.chunked(50).forEach { chunk ->
                    typedOverlay.render(chunk, controller)
                    yield()
                }
            }
        }
}
```

### 4. Viewport-Based Loading
Only load markers visible in current viewport plus buffer zone.

### 5. Background Initialization
Move heavy initialization to background thread with progress updates.