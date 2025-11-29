package com.mapconductor.example.pages.map.visibleregion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.arcgis.map.ArcGISMapView
import com.mapconductor.arcgis.map.rememberArcGISMapViewState
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.spherical.WGS84Geodesic.computeDistanceBetween
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
@Composable
fun ZoomCalibrationPage(onToggleSidebar: () -> Unit = {}) {
    val testLocation = GeoPointImpl.fromLongLat(139.6917, 35.6895) // Tokyo Station
    val testZoomLevels =
        listOf(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0,
            8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0,
            15.0, 16.0, 17.0, 18.0, 10.0, 20.0,
        )

    var currentZoomLevel by rememberSaveable { mutableStateOf(0.0) }
    var googleMapResults by rememberSaveable { mutableStateOf<Map<Double, VisibleRegionInfo>>(emptyMap()) }
    var arcgisResults by rememberSaveable { mutableStateOf<Map<Double, VisibleRegionInfo>>(emptyMap()) }
    var measurementInProgress by remember { mutableStateOf(false) }
    var measurementMessage by remember { mutableStateOf("") }
    // Map view states with initial camera position
    val initialCameraPosition =
        MapCameraPositionImpl(
            position = testLocation,
            zoom = currentZoomLevel,
            bearing = 0.0,
            tilt = 0.0,
        )

    val googleMapViewState =
        rememberGoogleMapViewState(
            cameraPosition = initialCameraPosition,
        )
    val arcGisMapViewState =
        rememberArcGISMapViewState(
            cameraPosition = initialCameraPosition,
        )

    // Update camera positions when zoom level changes
    LaunchedEffect(currentZoomLevel) {
        val newCameraPosition =
            MapCameraPositionImpl(
                position = testLocation,
                zoom = currentZoomLevel,
                bearing = 0.0,
                tilt = 0.0,
            )
        googleMapViewState.moveCameraTo(newCameraPosition)
        arcGisMapViewState.moveCameraTo(newCameraPosition)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Zoom Level Calibration Tool",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Location: Tokyo Station (35.6895°N, 139.6917°E)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Current Zoom Level: ${String.format("%.1f", currentZoomLevel)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )

                Slider(
                    value = currentZoomLevel.toFloat(),
                    onValueChange = { currentZoomLevel = it.toDouble() },
                    valueRange = 0f..20f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Map views section
        Text(
            text = "Live Map Comparison (Zoom: ${String.format("%.1f", currentZoomLevel)})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(280.dp),
        ) {
            Column(
                modifier = Modifier.weight(1.5f),
            ) {
                Text(
                    text = "Google Maps",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                GoogleMapView(
                    state = googleMapViewState,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1.5f),
            ) {
                Text(
                    text = "ArcGIS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                ArcGISMapView(
                    state = arcGisMapViewState,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    // Extract visible region data from Google Maps view
                    googleMapViewState.cameraPosition.visibleRegion?.let { visibleRegion ->
                        val info = createVisibleRegionInfo(visibleRegion)
                        googleMapResults = googleMapResults + (currentZoomLevel to info)
                        measurementMessage =
                            String.format("Google Maps data captured: %.2f, %.2f km", info.widthKm, info.heightKm)
                        measurementInProgress = true
                    } ?: run {
                        measurementMessage =
                            "Google Maps visible region not available yet. Please wait for map to load."
                        measurementInProgress = true
                    }
                },
                enabled = !measurementInProgress,
                modifier = Modifier.weight(1f),
            ) {
                Text("Capture Google Maps")
            }

            Button(
                onClick = {
                    // Extract visible region data from ArcGIS view
                    arcGisMapViewState.cameraPosition.visibleRegion?.let { visibleRegion ->
                        val info = createVisibleRegionInfo(visibleRegion)
                        arcgisResults = arcgisResults + (currentZoomLevel to info)
                        measurementMessage =
                            "ArcGIS data captured: ${String.format("%.2f", info.widthKm)} × ${String.format("%.2f", info.heightKm)} km"
                        measurementInProgress = true
                    } ?: run {
                        measurementMessage = "ArcGIS visible region not available yet. Please wait for map to load."
                        measurementInProgress = true
                    }
                },
                enabled = !measurementInProgress,
                modifier = Modifier.weight(1f),
            ) {
                Text("Capture ArcGIS")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationResultsTable(
            testZoomLevels = testZoomLevels,
            googleMapResults = googleMapResults,
            arcgisResults = arcgisResults,
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationRecommendations(
            googleMapResults = googleMapResults,
            arcgisResults = arcgisResults,
        )

        // Clear all data button
        if (googleMapResults.isNotEmpty() || arcgisResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                OutlinedButton(
                    onClick = {
                        googleMapResults = emptyMap()
                        arcgisResults = emptyMap()
                        measurementMessage = "All calibration data cleared"
                        measurementInProgress = true
                    },
                ) {
                    Text("Clear All Data")
                }
            }
        }

        // Instructions for better calibration
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Calibration Instructions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text =
                        "• Adjust zoom level with the slider above\n" +
                            "• Wait for both maps to load completely\n" +
                            "• Tap 'Capture' buttons to automatically extract visible region data\n" +
                            "• Collect data for multiple zoom levels to improve accuracy\n" +
                            "• Compare the results in the table below",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                )
            }
        }

        if (measurementInProgress && measurementMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            if (measurementMessage.contains("captured")) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            },
                    ),
            ) {
                Text(
                    text = measurementMessage,
                    modifier = Modifier.padding(16.dp),
                    color =
                        if (measurementMessage.contains("captured")) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            measurementInProgress = false
                            measurementMessage = ""
                        },
                    ) {
                        Text("OK")
                    }
                }
            }
        }

        // Auto-dismiss successful measurements after 3 seconds
        LaunchedEffect(measurementMessage) {
            if (measurementMessage.contains("captured")) {
                kotlinx.coroutines.delay(3000)
                measurementInProgress = false
                measurementMessage = ""
            }
        }
    }
}

@Composable
fun CalibrationResultsTable(
    testZoomLevels: List<Double>,
    googleMapResults: Map<Double, VisibleRegionInfo>,
    arcgisResults: Map<Double, VisibleRegionInfo>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Calibration Results",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Table headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Zoom", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Google Maps (km)", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("ArcGIS (km)", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("Ratio", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            testZoomLevels.forEach { zoomLevel ->
                val googleData = googleMapResults[zoomLevel]
                val arcgisData = arcgisResults[zoomLevel]
                val ratio =
                    if (googleData != null && arcgisData != null) {
                        val googleArea = googleData.widthKm * googleData.heightKm
                        val arcgisArea = arcgisData.widthKm * arcgisData.heightKm
                        if (arcgisArea > 0) googleArea / arcgisArea else 0.0
                    } else {
                        null
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = String.format("%.1f", zoomLevel),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text =
                            googleData?.let {
                                "${String.format("%.1f", it.widthKm)} × ${String.format("%.1f", it.heightKm)}"
                            }
                                ?: "-",
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(2f),
                    )
                    Text(
                        text =
                            arcgisData?.let {
                                "${String.format("%.1f", it.widthKm)} × ${String.format("%.1f", it.heightKm)}"
                            }
                                ?: "-",
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(2f),
                    )
                    Text(
                        text = ratio?.let { String.format("%.2f", it) } ?: "-",
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun CalibrationRecommendations(
    googleMapResults: Map<Double, VisibleRegionInfo>,
    arcgisResults: Map<Double, VisibleRegionInfo>,
) {
    if (googleMapResults.isEmpty() || arcgisResults.isEmpty()) return

    val averageRatio = calculateAverageRatio(googleMapResults, arcgisResults)
    val currentZoom0Altitude = 198_506_928.2 // Use current value from ZoomAltitudeConverter
    val recommendedZoom0Altitude = currentZoom0Altitude / averageRatio // Invert the ratio!

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Calibration Recommendations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Average Area Ratio (ArcGIS/Google): ${String.format("%.3f", averageRatio)}",
                fontFamily = FontFamily.Monospace,
            )

            Text(
                text =
                    if (averageRatio > 1.0) {
                        "ArcGIS shows ${String.format("%.1f", (averageRatio - 1.0) * 100)}% larger area (zoom too low)"
                    } else {
                        "ArcGIS shows ${String.format("%.1f", (1.0 - averageRatio) * 100)}% smaller area (zoom too high)"
                    },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Text(
                text = "Current DEFAULT_ZOOM0_ALTITUDE: ${String.format("%.0f", currentZoom0Altitude)}",
                fontFamily = FontFamily.Monospace,
            )

            Text(
                text = "Recommended DEFAULT_ZOOM0_ALTITUDE: ${String.format("%.0f", recommendedZoom0Altitude)}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val percentChange = ((recommendedZoom0Altitude - currentZoom0Altitude) / currentZoom0Altitude) * 100
            Text(
                text = "Change: ${if (percentChange > 0) "+" else ""}${String.format("%.1f", percentChange)}%",
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                ) {
                    Text(
                        text = "Code to Update:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "const val DEFAULT_ZOOM0_ALTITUDE = ${String.format("%.1f", recommendedZoom0Altitude)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Text(
                        text = "// Location: ZoomAltitudeConverter.kt",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun calculateAverageRatio(
    googleMapResults: Map<Double, VisibleRegionInfo>,
    arcgisResults: Map<Double, VisibleRegionInfo>,
): Double {
    val ratios = mutableListOf<Double>()

    googleMapResults.forEach { (zoomLevel, googleData) ->
        arcgisResults[zoomLevel]?.let { arcgisData ->
            val googleArea = googleData.widthKm * googleData.heightKm
            val arcgisArea = arcgisData.widthKm * arcgisData.heightKm
            if (arcgisArea > 0 && googleArea > 0) {
                // Calculate the ratio of ArcGIS area to Google area
                // If ArcGIS shows larger area, ratio > 1.0 (zoom conversion too low)
                // If ArcGIS shows smaller area, ratio < 1.0 (zoom conversion too high)
                val areaRatio = arcgisArea / googleArea
                ratios.add(areaRatio)
            }
        }
    }

    return if (ratios.isEmpty()) 1.0 else ratios.average()
}

private fun createVisibleRegionInfo(visibleRegion: com.mapconductor.core.map.VisibleRegion): VisibleRegionInfo {
    val bounds = visibleRegion.bounds
    if (bounds.isEmpty || bounds.southWest == null || bounds.northEast == null) {
        return VisibleRegionInfo(
            bounds = "Empty bounds",
            corners = emptyList(),
            centerPoint = "N/A",
            widthKm = 0.0,
            heightKm = 0.0,
        )
    }

    val widthKm =
        computeDistanceBetween(
            bounds.southWest!!,
            GeoPointImpl(bounds.southWest!!.latitude, bounds.northEast!!.longitude),
        )
    val heightKm =
        computeDistanceBetween(
            bounds.southWest!!,
            GeoPointImpl(bounds.northEast!!.latitude, bounds.southWest!!.longitude),
        )

    return VisibleRegionInfo(
        bounds = "",
        corners = emptyList(),
        centerPoint = "",
        widthKm = widthKm,
        heightKm = heightKm,
    )
}
