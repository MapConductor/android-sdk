package com.mapconductor.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapconductor.core.map.MapViewState

/**
 * Example demonstrating deferred MapView initialization.
 * 
 * This shows how to defer map initialization until after UI elements
 * like sidebars are closed or specific user interactions occur.
 * 
 * Benefits:
 * - Faster app startup (map initialization can be expensive)
 * - Better UX (no black screen flash during complex UI transitions)
 * - Conditional loading based on user preferences or network state
 */
@Composable
fun DeferredMapExample(
    mapViewState: MapViewState<*>?,
    modifier: Modifier = Modifier,
) {
    var shouldInitializeMap by remember { mutableStateOf(false) }
    var isSidebarClosed by remember { mutableStateOf(false) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Main map content - only initialize when ready
        mapViewState?.let { currentMapViewState ->
            MapViewContainer(
                modifier = Modifier.fillMaxSize(),
                state = currentMapViewState,
                shouldInitialize = shouldInitializeMap, // Key parameter for deferred init
                onMapClick = { /* Handle map clicks */ },
            ) {
                // Map content (markers, circles, etc.)
            }
        }
        
        // Example sidebar or overlay UI
        if (!isSidebarClosed) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Sidebar Content")
                    Text("Map will initialize after closing this sidebar")
                    
                    Button(
                        onClick = {
                            isSidebarClosed = true
                            shouldInitializeMap = true // Trigger map initialization
                        }
                    ) {
                        Text("Close Sidebar & Initialize Map")
                    }
                }
            }
        }
        
        // Alternative: Initialize map based on other conditions
        if (!shouldInitializeMap && isSidebarClosed) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Ready to load map!")
                    Button(
                        onClick = { shouldInitializeMap = true }
                    ) {
                        Text("Load Map Now")
                    }
                }
            }
        }
    }
}