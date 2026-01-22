package com.mapconductor.example.pages.map.camerasync

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.polygon.Polygon
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.IconSelectMenu
import java.util.Locale

private data class CameraLocationInfo(
    val name: String,
    val bounds: GeoRectBounds,
    val center: GeoPoint,
    val zoom: Double,
)

@Composable
fun CameraSyncPage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        remember {
            MapCameraPosition(
                position = GeoPoint(latitude = 35.6812, longitude = 139.7671, altitude = 0.0), // Tokyo
                zoom = 12.0,
                bearing = 0.0,
                tilt = 0.0,
            )
        }

    val locations = remember { defaultLocations() }
    val boundsPolylines = remember(locations) { locations.map(::boundsPolyline) }
    val referenceRectangles = remember(locations) { referenceRectangles(locations) }

    val leftMenuItems = DefaultMapViewItems(initCameraPosition)
    val rightMenuItems = DefaultMapViewItems(initCameraPosition)

    // Default to Mapbox vs ArcGIS for zoom calibration (Google Maps may be unavailable in some dev setups).
    var leftSelectedIndex by rememberSaveable { mutableIntStateOf(0) } // Mapbox
    var rightSelectedIndex by rememberSaveable { mutableIntStateOf(1) } // Here

    @Suppress("UNCHECKED_CAST")
    val leftState = leftMenuItems[leftSelectedIndex].value as MapViewStateInterface<*>
    @Suppress("UNCHECKED_CAST")
    val rightState = rightMenuItems[rightSelectedIndex].value as MapViewStateInterface<*>

    var leftCameraPosition by remember { mutableStateOf(initCameraPosition) }
    var rightCameraPosition by remember { mutableStateOf(initCameraPosition) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle sidebar",
                    modifier =
                        Modifier
                            .clickable(onClick = onToggleSidebar)
                            .size(32.dp)
                            .padding(end = 4.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Left (Source)",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconSelectMenu(
                        itemList = leftMenuItems,
                        selectedIndex = leftSelectedIndex,
                        onSelect = { index, _ -> leftSelectedIndex = index },
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Right (Synced)",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconSelectMenu(
                        itemList = rightMenuItems,
                        selectedIndex = rightSelectedIndex,
                        onSelect = { index, _ -> rightSelectedIndex = index },
                    )
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            locations.forEach { location ->
                Button(
                    onClick = {
                        val position =
                            MapCameraPosition(
                                position = location.center,
                                zoom = location.zoom,
                                bearing = 0.0,
                                tilt = 0.0,
                            )
                        leftState.moveCameraTo(position, durationMillis = 1000)
                        rightState.moveCameraTo(position, durationMillis = 1000)
                        leftCameraPosition = position
                        rightCameraPosition = position
                    },
                ) {
                    Text(text = location.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 10.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            CameraSyncMapPane(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                mapViewState = leftState,
                label = "Source Camera",
                cameraPosition = leftCameraPosition,
                onCameraMoveEnd = { position ->
                    leftCameraPosition = position
                    rightState.moveCameraTo(position, durationMillis = 0)
                },
                boundsPolylines = boundsPolylines,
                referenceRectangles = referenceRectangles,
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline),
            )

            CameraSyncMapPane(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                mapViewState = rightState,
                label = "Synced Camera",
                cameraPosition = rightCameraPosition,
                onCameraMoveEnd = { position -> rightCameraPosition = position },
                boundsPolylines = boundsPolylines,
                referenceRectangles = referenceRectangles,
            )
        }
    }
}

@Composable
private fun CameraSyncMapPane(
    modifier: Modifier,
    mapViewState: MapViewStateInterface<*>,
    label: String,
    cameraPosition: MapCameraPosition,
    onCameraMoveEnd: (MapCameraPosition) -> Unit,
    boundsPolylines: List<PolylineState>,
    referenceRectangles: List<PolygonState>,
) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            MapViewContainer(
                modifier = Modifier.fillMaxSize(),
                state = mapViewState,
                onCameraMoveEnd = onCameraMoveEnd,
            ) {
                boundsPolylines.forEach { Polyline(it) }
                referenceRectangles.forEach { Polygon(it) }
            }
        }

        CameraInfoCard(
            modifier = Modifier.fillMaxWidth(),
            label = label,
            position = cameraPosition,
        )
    }
}

@Composable
private fun CameraInfoCard(
    modifier: Modifier,
    label: String,
    position: MapCameraPosition,
) {
    val fmt = remember { Locale.US }
    Card(modifier = modifier.padding(10.dp)) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(text = "Lat: ${String.format(fmt, "%.5f", position.position.latitude)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Lng: ${String.format(fmt, "%.5f", position.position.longitude)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Zoom: ${String.format(fmt, "%.2f", position.zoom)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Tilt: ${String.format(fmt, "%.1f°", position.tilt)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Bearing: ${String.format(fmt, "%.1f°", position.bearing)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Alt: ${String.format(fmt, "%.0f m", position.position.altitude ?: 0.0)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun defaultLocations(): List<CameraLocationInfo> =
    listOf(
        CameraLocationInfo(
            name = "French Southern and Antarctic Lands",
            bounds =
                GeoRectBounds(
                    southWest = GeoPoint(latitude = -49.5, longitude = 50.0, altitude = 0.0),
                    northEast = GeoPoint(latitude = -37.5, longitude = 77.0, altitude = 0.0),
                ),
            center = GeoPoint(latitude = -43.5, longitude = 63.5, altitude = 0.0),
            zoom = 4.0,
        ),
        CameraLocationInfo(
            name = "Finland",
            bounds =
                GeoRectBounds(
                    southWest = GeoPoint(latitude = 59.8, longitude = 19.1, altitude = 0.0),
                    northEast = GeoPoint(latitude = 70.1, longitude = 31.6, altitude = 0.0),
                ),
            center = GeoPoint(latitude = 64.95, longitude = 25.35, altitude = 0.0),
            zoom = 5.0,
        ),
        CameraLocationInfo(
            name = "Iceland",
            bounds =
                GeoRectBounds(
                    southWest = GeoPoint(latitude = 63.3, longitude = -24.5, altitude = 0.0),
                    northEast = GeoPoint(latitude = 66.6, longitude = -13.5, altitude = 0.0),
                ),
            center = GeoPoint(latitude = 64.95, longitude = -19.0, altitude = 0.0),
            zoom = 6.0,
        ),
        CameraLocationInfo(
            name = "Kiribati",
            bounds =
                GeoRectBounds(
                    southWest = GeoPoint(latitude = -11.5, longitude = -174.5, altitude = 0.0),
                    northEast = GeoPoint(latitude = 5.0, longitude = -147.0, altitude = 0.0),
                ),
            center = GeoPoint(latitude = -3.25, longitude = -160.75, altitude = 0.0),
            zoom = 4.5,
        ),
        CameraLocationInfo(
            name = "Oahu Island",
            bounds =
                GeoRectBounds(
                    southWest = GeoPoint(latitude = 21.25, longitude = -158.3, altitude = 0.0),
                    northEast = GeoPoint(latitude = 21.7, longitude = -157.65, altitude = 0.0),
                ),
            center = GeoPoint(latitude = 21.475, longitude = -157.975, altitude = 0.0),
            zoom = 9.5,
        ),
    )

private fun boundsPolyline(location: CameraLocationInfo): PolylineState {
    val sw = location.bounds.southWest ?: return PolylineState(points = emptyList())
    val ne = location.bounds.northEast ?: return PolylineState(points = emptyList())
    val points =
        listOf(
            sw,
            GeoPoint(latitude = sw.latitude, longitude = ne.longitude, altitude = 0.0),
            ne,
            GeoPoint(latitude = ne.latitude, longitude = sw.longitude, altitude = 0.0),
            sw,
        )
    return PolylineState(
        points = points,
        strokeColor = Color.Red,
        strokeWidth = 3.dp,
        geodesic = true,
    )
}

private fun referenceRectangles(locations: List<CameraLocationInfo>): List<PolygonState> {
    val size = 1.0 // approx 100km at equator
    return locations.mapIndexed { index, location ->
        val lat = location.center.latitude
        val lng = location.center.longitude
        val points =
            listOf(
                GeoPoint(latitude = lat - size / 2.0, longitude = lng - size / 2.0, altitude = 0.0),
                GeoPoint(latitude = lat - size / 2.0, longitude = lng + size / 2.0, altitude = 0.0),
                GeoPoint(latitude = lat + size / 2.0, longitude = lng + size / 2.0, altitude = 0.0),
                GeoPoint(latitude = lat + size / 2.0, longitude = lng - size / 2.0, altitude = 0.0),
                GeoPoint(latitude = lat - size / 2.0, longitude = lng - size / 2.0, altitude = 0.0),
            )
        PolygonState(
            points = points,
            id = "camera_sync_reference_$index",
            strokeColor = Color.Blue,
            strokeWidth = 2.dp,
            fillColor = Color.Blue.copy(alpha = 0.1f),
            geodesic = false,
            zIndex = 1,
        )
    }
}
