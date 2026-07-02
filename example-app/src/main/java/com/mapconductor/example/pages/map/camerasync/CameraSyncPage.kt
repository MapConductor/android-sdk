package com.mapconductor.example.pages.map.camerasync

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.polygon.Polygon
import com.mapconductor.compose.polyline.Polyline
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.IconSelectMenu
import java.util.Locale
import android.os.SystemClock
import kotlinx.coroutines.launch

private data class CameraLocationInfo(
    val name: String,
    val bounds: GeoRectBounds,
    val center: GeoPoint,
    val zoom: Double,
)

private enum class ActiveMapPane {
    Left,
    Right,
}

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

    // Use rememberUpdatedState to ensure callbacks always reference the current state,
    // not the state captured when the callback lambda was created.
    val currentLeftState by rememberUpdatedState(leftState)
    val currentRightState by rememberUpdatedState(rightState)

    var leftCameraPosition by remember { mutableStateOf(initCameraPosition) }
    var rightCameraPosition by remember { mutableStateOf(initCameraPosition) }
    val mainScope = rememberCoroutineScope()

    // Guard against feedback loops for programmatic sync:
    // when we move a map by code, ignore the next camera callbacks that correspond to that same target.
    var programmaticLeftKey by remember { mutableStateOf<Long?>(null) }
    var programmaticLeftTarget by remember { mutableStateOf<MapCameraPosition?>(null) }
    var programmaticLeftUntilMs by remember { mutableStateOf(0L) }
    var programmaticLeftSinceMs by remember { mutableStateOf(0L) }
    var programmaticRightKey by remember { mutableStateOf<Long?>(null) }
    var programmaticRightTarget by remember { mutableStateOf<MapCameraPosition?>(null) }
    var programmaticRightUntilMs by remember { mutableStateOf(0L) }
    var programmaticRightSinceMs by remember { mutableStateOf(0L) }
    val programmaticTtlMs = 1200L
    val programmaticGraceMs = 250L

    // Move-time sync throttling to keep the UI responsive and avoid overwhelming SDKs.
    var lastLeftMoveSyncAtMs by remember { mutableStateOf(0L) }
    var lastRightMoveSyncAtMs by remember { mutableStateOf(0L) }
    val moveSyncIntervalMs = 33L // ~30fps

    fun cameraKey(camera: MapCameraPosition): Long {
        val latE5 = (camera.position.latitude * 1e5).toInt()
        val lonE5 = (camera.position.longitude * 1e5).toInt()
        val zoom100 = (camera.zoom * 100).toInt()
        val bearing10 = (camera.bearing * 10).toInt()
        return (((latE5 * 31 + lonE5) * 31 + zoom100) * 31 + bearing10).toLong()
    }

    fun markProgrammaticMove(
        pane: ActiveMapPane,
        target: MapCameraPosition,
        nowMs: Long,
    ) {
        val key = cameraKey(target)
        when (pane) {
            ActiveMapPane.Left -> {
                programmaticLeftKey = key
                programmaticLeftTarget = target
                programmaticLeftSinceMs = nowMs
                programmaticLeftUntilMs = nowMs + programmaticTtlMs
            }
            ActiveMapPane.Right -> {
                programmaticRightKey = key
                programmaticRightTarget = target
                programmaticRightSinceMs = nowMs
                programmaticRightUntilMs = nowMs + programmaticTtlMs
            }
        }
    }

    fun clearProgrammaticMove(pane: ActiveMapPane) {
        when (pane) {
            ActiveMapPane.Left -> {
                programmaticLeftKey = null
                programmaticLeftTarget = null
                programmaticLeftSinceMs = 0L
                programmaticLeftUntilMs = 0L
            }
            ActiveMapPane.Right -> {
                programmaticRightKey = null
                programmaticRightTarget = null
                programmaticRightSinceMs = 0L
                programmaticRightUntilMs = 0L
            }
        }
    }

    fun bearingDeltaDeg(
        a: Double,
        b: Double,
    ): Double {
        val d = ((a - b) % 360.0 + 360.0) % 360.0
        return if (d > 180.0) 360.0 - d else d
    }

    fun isCloseToTarget(
        camera: MapCameraPosition,
        target: MapCameraPosition,
    ): Boolean {
        val dLat = kotlin.math.abs(camera.position.latitude - target.position.latitude)
        val dLon = kotlin.math.abs(camera.position.longitude - target.position.longitude)
        val dZoom = kotlin.math.abs(camera.zoom - target.zoom)
        val dBearing = bearingDeltaDeg(camera.bearing, target.bearing)
        val dTilt = kotlin.math.abs(camera.tilt - target.tilt)

        // Tolerances are intentionally a bit loose: different SDKs can't represent the exact same camera.
        return dLat < 0.0012 && dLon < 0.0012 && dZoom < 0.75 && dBearing < 12.0 && dTilt < 6.0
    }

    fun isProgrammaticMove(
        pane: ActiveMapPane,
        camera: MapCameraPosition,
        nowMs: Long,
    ): Boolean {
        val (key, target, until) =
            when (pane) {
                ActiveMapPane.Left -> Triple(programmaticLeftKey, programmaticLeftTarget, programmaticLeftUntilMs)
                ActiveMapPane.Right -> Triple(programmaticRightKey, programmaticRightTarget, programmaticRightUntilMs)
            }
        val k = key ?: return false
        if (nowMs > until) return false
        if (cameraKey(camera) == k) return true
        return target?.let { isCloseToTarget(camera, it) } ?: false
    }

    Scaffold { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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

                    Text(
                        text = "Camera Sync",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxLines = 2,
            ) {
                locations.forEach { location ->
                    Button(
                        onClick = {
                            val now = SystemClock.uptimeMillis()
                            val position =
                                MapCameraPosition(
                                    position = location.center,
                                    zoom = location.zoom,
                                    bearing = 0.0,
                                    tilt = 0.0,
                                )
                            currentLeftState.moveCameraTo(position, durationMillis = 1000)
                            currentRightState.moveCameraTo(position, durationMillis = 1000)
                            leftCameraPosition = position
                            rightCameraPosition = position
                            // Both maps will emit camera callbacks; treat them as programmatic during the animation.
                            markProgrammaticMove(ActiveMapPane.Left, position, now)
                            markProgrammaticMove(ActiveMapPane.Right, position, now)
                            programmaticLeftUntilMs = now + 1000L + programmaticTtlMs
                            programmaticRightUntilMs = now + 1000L + programmaticTtlMs
                        },
                    ) {
                        Text(text = location.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 10.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val stackVertically = maxHeight > maxWidth

                if (stackVertically) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CameraSyncMapPane(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            menuContent = {
                                IconSelectMenu(
                                    itemList = leftMenuItems,
                                    selectedIndex = leftSelectedIndex,
                                    onSelect = { index, _ -> leftSelectedIndex = index },
                                )
                            },
                            mapViewState = leftState,
                            label = "Source Camera",
                            cameraPosition = leftCameraPosition,
                            onCameraMove = { position ->
                                mainScope.launch {
                                    val now = SystemClock.uptimeMillis()
                                    // Ignore feedback from programmatic moves (but stop ignoring if the user deviates).
                                    if (programmaticLeftKey != null) {
                                        if (now > programmaticLeftUntilMs) {
                                            clearProgrammaticMove(ActiveMapPane.Left)
                                        } else {
                                            val age = now - programmaticLeftSinceMs
                                            if (age <= programmaticGraceMs ||
                                                isProgrammaticMove(ActiveMapPane.Left, position, now)
                                            ) {
                                                leftCameraPosition = position
                                                return@launch
                                            }
                                            clearProgrammaticMove(ActiveMapPane.Left)
                                        }
                                    }

                                    if (now - lastLeftMoveSyncAtMs < moveSyncIntervalMs) return@launch
                                    lastLeftMoveSyncAtMs = now

                                    leftCameraPosition = position
                                    rightCameraPosition = position
                                    markProgrammaticMove(ActiveMapPane.Right, position, now)
                                    currentRightState.moveCameraTo(position, durationMillis = 0)
                                }
                            },
                            onCameraMoveEnd = { position ->
                                mainScope.launch {
                                    val now = SystemClock.uptimeMillis()
                                    if (programmaticLeftKey != null) {
                                        if (now > programmaticLeftUntilMs) {
                                            clearProgrammaticMove(ActiveMapPane.Left)
                                        } else {
                                            val age = now - programmaticLeftSinceMs
                                            if (age <= programmaticGraceMs ||
                                                isProgrammaticMove(ActiveMapPane.Left, position, now)
                                            ) {
                                                leftCameraPosition = position
                                                return@launch
                                            }
                                            clearProgrammaticMove(ActiveMapPane.Left)
                                        }
                                    }
                                    leftCameraPosition = position
                                    rightCameraPosition = position
                                    markProgrammaticMove(ActiveMapPane.Right, position, now)
                                    currentRightState.moveCameraTo(position, durationMillis = 0)
                                }
                            },
                            boundsPolylines = boundsPolylines,
                            referenceRectangles = referenceRectangles,
                        )

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outline),
                        )

                        CameraSyncMapPane(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            menuContent = {
                                IconSelectMenu(
                                    itemList = rightMenuItems,
                                    selectedIndex = rightSelectedIndex,
                                    onSelect = { index, _ -> rightSelectedIndex = index },
                                )
                            },
                            mapViewState = rightState,
                            label = "Synced Camera",
                            cameraPosition = rightCameraPosition,
                            onCameraMove = { position ->
                                mainScope.launch {
                                    val now = SystemClock.uptimeMillis()
                                    if (programmaticRightKey != null) {
                                        if (now > programmaticRightUntilMs) {
                                            clearProgrammaticMove(ActiveMapPane.Right)
                                        } else {
                                            val age = now - programmaticRightSinceMs
                                            if (age <= programmaticGraceMs ||
                                                isProgrammaticMove(ActiveMapPane.Right, position, now)
                                            ) {
                                                rightCameraPosition = position
                                                return@launch
                                            }
                                            clearProgrammaticMove(ActiveMapPane.Right)
                                        }
                                    }

                                    if (now - lastRightMoveSyncAtMs < moveSyncIntervalMs) return@launch
                                    lastRightMoveSyncAtMs = now

                                    rightCameraPosition = position
                                    leftCameraPosition = position
                                    markProgrammaticMove(ActiveMapPane.Left, position, now)
                                    currentLeftState.moveCameraTo(position, durationMillis = 0)
                                }
                            },
                            onCameraMoveEnd = { position ->
                                mainScope.launch {
                                    val now = SystemClock.uptimeMillis()
                                    if (programmaticRightKey != null) {
                                        if (now > programmaticRightUntilMs) {
                                            clearProgrammaticMove(ActiveMapPane.Right)
                                        } else {
                                            val age = now - programmaticRightSinceMs
                                            if (age <= programmaticGraceMs ||
                                                isProgrammaticMove(ActiveMapPane.Right, position, now)
                                            ) {
                                                rightCameraPosition = position
                                                return@launch
                                            }
                                            clearProgrammaticMove(ActiveMapPane.Right)
                                        }
                                    }
                                    rightCameraPosition = position
                                    leftCameraPosition = position
                                    markProgrammaticMove(ActiveMapPane.Left, position, now)
                                    currentLeftState.moveCameraTo(position, durationMillis = 0)
                                }
                            },
                            boundsPolylines = boundsPolylines,
                            referenceRectangles = referenceRectangles,
                        )
                    }
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        CameraSyncMapPane(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            menuContent = {
                                IconSelectMenu(
                                    itemList = leftMenuItems,
                                    selectedIndex = leftSelectedIndex,
                                    onSelect = { index, _ -> leftSelectedIndex = index },
                                )
                            },
                            mapViewState = leftState,
                            label = "Source Camera",
                            cameraPosition = leftCameraPosition,
                            onCameraMove = { position ->
                                mainScope.launch {
                                    val now = SystemClock.uptimeMillis()
                                    if (programmaticLeftKey != null) {
                                        if (now > programmaticLeftUntilMs) {
                                            clearProgrammaticMove(ActiveMapPane.Left)
                                        } else {
                                            val age = now - programmaticLeftSinceMs
                                            if (age <= programmaticGraceMs ||
                                                isProgrammaticMove(ActiveMapPane.Left, position, now)
                                            ) {
                                                leftCameraPosition = position
                                                return@launch
                                            }
                                            clearProgrammaticMove(ActiveMapPane.Left)
                                        }
                                    }

                                    if (now - lastLeftMoveSyncAtMs < moveSyncIntervalMs) return@launch
                                    lastLeftMoveSyncAtMs = now

                                    leftCameraPosition = position
                                    rightCameraPosition = position
                                    markProgrammaticMove(ActiveMapPane.Right, position, now)
                                    currentRightState.moveCameraTo(position, durationMillis = 0)
                                }
                            },
                            onCameraMoveEnd = { position ->
                                mainScope.launch {
                                    val now = SystemClock.uptimeMillis()
                                    if (programmaticLeftKey != null) {
                                        if (now > programmaticLeftUntilMs) {
                                            clearProgrammaticMove(ActiveMapPane.Left)
                                        } else {
                                            val age = now - programmaticLeftSinceMs
                                            if (age <= programmaticGraceMs ||
                                                isProgrammaticMove(ActiveMapPane.Left, position, now)
                                            ) {
                                                leftCameraPosition = position
                                                return@launch
                                            }
                                            clearProgrammaticMove(ActiveMapPane.Left)
                                        }
                                    }
                                    leftCameraPosition = position
                                    rightCameraPosition = position
                                    markProgrammaticMove(ActiveMapPane.Right, position, now)
                                    currentRightState.moveCameraTo(position, durationMillis = 0)
                                }
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
                            menuContent = {
                                IconSelectMenu(
                                    itemList = rightMenuItems,
                                    selectedIndex = rightSelectedIndex,
                                    onSelect = { index, _ -> rightSelectedIndex = index },
                                )
                            },
                            mapViewState = rightState,
                            label = "Synced Camera",
                            cameraPosition = rightCameraPosition,
                            onCameraMove = { position ->
                                mainScope.launch {
                                    val now = SystemClock.uptimeMillis()
                                    if (programmaticRightKey != null) {
                                        if (now > programmaticRightUntilMs) {
                                            clearProgrammaticMove(ActiveMapPane.Right)
                                        } else {
                                            val age = now - programmaticRightSinceMs
                                            if (age <= programmaticGraceMs ||
                                                isProgrammaticMove(ActiveMapPane.Right, position, now)
                                            ) {
                                                rightCameraPosition = position
                                                return@launch
                                            }
                                            clearProgrammaticMove(ActiveMapPane.Right)
                                        }
                                    }

                                    if (now - lastRightMoveSyncAtMs < moveSyncIntervalMs) return@launch
                                    lastRightMoveSyncAtMs = now

                                    rightCameraPosition = position
                                    leftCameraPosition = position
                                    markProgrammaticMove(ActiveMapPane.Left, position, now)
                                    currentLeftState.moveCameraTo(position, durationMillis = 0)
                                }
                            },
                            onCameraMoveEnd = { position ->
                                mainScope.launch {
                                    val now = SystemClock.uptimeMillis()
                                    if (programmaticRightKey != null) {
                                        if (now > programmaticRightUntilMs) {
                                            clearProgrammaticMove(ActiveMapPane.Right)
                                        } else {
                                            val age = now - programmaticRightSinceMs
                                            if (age <= programmaticGraceMs ||
                                                isProgrammaticMove(ActiveMapPane.Right, position, now)
                                            ) {
                                                rightCameraPosition = position
                                                return@launch
                                            }
                                            clearProgrammaticMove(ActiveMapPane.Right)
                                        }
                                    }
                                    rightCameraPosition = position
                                    leftCameraPosition = position
                                    markProgrammaticMove(ActiveMapPane.Left, position, now)
                                    currentLeftState.moveCameraTo(position, durationMillis = 0)
                                }
                            },
                            boundsPolylines = boundsPolylines,
                            referenceRectangles = referenceRectangles,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraSyncMapPane(
    modifier: Modifier,
    menuContent: (@Composable () -> Unit)? = null,
    mapViewState: MapViewStateInterface<*>,
    label: String,
    cameraPosition: MapCameraPosition,
    onCameraMoveStart: (() -> Unit)? = null,
    onCameraMove: ((MapCameraPosition) -> Unit)? = null,
    onCameraMoveEnd: (MapCameraPosition) -> Unit,
    boundsPolylines: List<PolylineState>,
    referenceRectangles: List<PolygonState>,
) {
    Box(modifier = modifier) {
        // Base: Map (full available size). Overlays are drawn on top to maximize map area.
        MapViewContainer(
            modifier = Modifier.fillMaxSize(),
            state = mapViewState,
            onCameraMoveStart = { onCameraMoveStart?.invoke() },
            onCameraMove = { pos -> onCameraMove?.invoke(pos) },
            onCameraMoveEnd = onCameraMoveEnd,
        ) {
            boundsPolylines.forEach { Polyline(it) }
            referenceRectangles.forEach { Polygon(it) }
        }

        // Top overlay: SDK selector (interactive)
        if (menuContent != null) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .fillMaxWidth(0.72f),
            ) {
                menuContent()
            }
        }

        // Bottom overlay: camera state display (read-only)
        CameraInfoCard(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
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
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = "Lat: ${String.format(fmt, "%.5f", position.position.latitude)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Lng: ${String.format(fmt, "%.5f", position.position.longitude)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Zoom: ${String.format(fmt, "%.2f", position.zoom)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Tilt: ${String.format(fmt, "%.1f°", position.tilt)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Bearing: ${String.format(fmt, "%.1f°", position.bearing)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Alt: ${String.format(fmt, "%.0f m", position.position.altitude)}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun defaultLocations(): List<CameraLocationInfo> =
    listOf(
        CameraLocationInfo(
            name = "Tokyo",
            bounds =
                GeoRectBounds(
                    southWest = GeoPoint(latitude = 35.62, longitude = 139.70, altitude = 0.0),
                    northEast = GeoPoint(latitude = 35.74, longitude = 139.84, altitude = 0.0),
                ),
            center = GeoPoint(latitude = 35.6812, longitude = 139.7671, altitude = 0.0),
            zoom = 12.0,
        ),
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
