package com.mapconductor.example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.OnMarkerEventHandler

/**
 * Example of a map component that defers initialization until sidebar is closed.
 *
 * Usage in your pages:
 * ```kotlin
 * SidebarAwareMapComponent(
 *     modifier = Modifier.fillMaxSize(),
 *     mapViewState = currentMapViewState,
 *     isSidebarExpanded = isSidebarExpanded, // From NavigationViewModel
 *     onMapClick = { position -> /* handle */ },
 *     onMarkerClick = { marker -> /* handle */ }
 * ) {
 *     // Your map content (markers, circles, etc.)
 *     markers.forEach { Marker(it) }
 * }
 * ```
 */
@Composable
fun SidebarAwareMapComponent(
    modifier: Modifier = Modifier,
    mapViewState: MapViewState<*>?,
    isSidebarExpanded: Boolean,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    content: @Composable (() -> Unit)? = null,
) {
    mapViewState?.let { currentMapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = currentMapViewState,
            // Only initialize map when sidebar is closed
            onMapClick = onMapClick,
            onMarkerClick = onMarkerClick,
            shouldInitialize = !isSidebarExpanded,
        ) {
            content?.invoke()
        }
    }
}

/**
 * Alternative approach: Initialize after a delay once sidebar is closed
 */
@Composable
fun DelayedInitMapComponent(
    modifier: Modifier = Modifier,
    mapViewState: MapViewState<*>?,
    isSidebarExpanded: Boolean,
    initDelayMs: Long = 300L, // Small delay for smoother UX
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    content: @Composable (() -> Unit)? = null,
) {
    // You can implement a delayed initialization using LaunchedEffect
    // if you want the map to initialize a few milliseconds after sidebar closes
    LaunchedEffect(!isSidebarExpanded) {
        if (!isSidebarExpanded) {
            kotlinx.coroutines.delay(initDelayMs)
        }
    }

    mapViewState?.let { currentMapViewState ->
        MapViewContainer(
            modifier = modifier,
            state = currentMapViewState,
            onMapClick = onMapClick,
            onMarkerClick = onMarkerClick,
            shouldInitialize = !isSidebarExpanded,
        ) {
            content?.invoke()
        }
    }
}
