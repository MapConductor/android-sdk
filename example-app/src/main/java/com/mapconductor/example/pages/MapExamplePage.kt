package com.mapconductor.example.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.AppViewModel
import com.mapconductor.example.toast.ToastHost
import kotlin.random.Random
import android.os.Bundle

@Composable
fun MapExamplePage(viewModel: AppViewModel) {
    var markerCount by remember { mutableIntStateOf(5) }

    val exampleMarkers = remember(markerCount) {
        generateExampleMarkers(markerCount)
    }

    val mapViewState = viewModel.mapViewState.collectAsState().value

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
//            MapArea(
//                mapViewState = mapViewState,
//                markers = exampleMarkers,
//                onDirectionButtonClick = { state ->
//                    state.icon?.let {
//                        state.icon = (it as? DefaultIcon)?.copy(
//                            fillColor = Color.Red,
//                        ) ?: it
//                    }
//                    state.animation = MarkerAnimation.Bounce
//                    viewModel.showToast("Direction button clicked for ${state.id}")
//                },
//                infoBubbleState = viewModel.infoBubbleState,
//                onMapClickHandler = { geoPoint ->
//                    viewModel.onMapClick(geoPoint)
//                    viewModel.showToast("Map clicked at: ${geoPoint.toUrlValue()}")
//                },
//                onMarkerClickHandler = { marker ->
//                    viewModel.onMarkerClick(marker)
//                    viewModel.showToast("Marker clicked: ${marker.id}")
//                },
//                onCircleClickHandler = viewModel::onCircleClick,
//                selectedMarker = viewModel.selectedMarker,
//            )

            // Control panel
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(paddingValues),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Map Controls",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Markers: $markerCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        OutlinedButton(
                            onClick = { if (markerCount > 0) markerCount-- },
                            modifier = Modifier.width(60.dp)
                        ) {
                            Text("-")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { markerCount++ },
                            modifier = Modifier.width(60.dp)
                        ) {
                            Text("+")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.cameraReset()
                            viewModel.showToast("Camera reset to initial position")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }

            // Floating action buttons
            FloatingActionButton(
                onClick = {
                    markerCount += 5
                    viewModel.showToast("Added 5 random markers")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add markers")
            }

            FloatingActionButton(
                onClick = {
                    markerCount = 0
                    viewModel.showToast("Cleared all markers")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(start = 16.dp, end = 16.dp, bottom = 88.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear markers")
            }

            ToastHost(
                messages = viewModel.messages.collectAsState().value,
                onDismiss = { viewModel.removeToast(it) },
            )
        }
    }
}

private fun generateExampleMarkers(count: Int): List<MarkerState> {
    val baseLatitude = 21.382314
    val baseLongitude = -157.933097
    val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan)

    return (0 until count).map { index ->
        val randomLat = baseLatitude + (Random.nextDouble(-0.1, 0.1))
        val randomLng = baseLongitude + (Random.nextDouble(-0.1, 0.1))
        val randomColor = colors[index % colors.size]

        MarkerState(
            id = "example_marker_$index",
            position = GeoPoint.fromLatLong(randomLat, randomLng),
            icon = DefaultIcon(
                fillColor = randomColor,
                strokeColor = Color.White
            ),
            extra = Bundle().apply {
                putString("title", "Example Marker ${index + 1}")
                putString("snippet", "This is an example marker with random position")
            },
            draggable = true
        )
    }
}
