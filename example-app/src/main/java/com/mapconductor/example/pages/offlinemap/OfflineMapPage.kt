package com.mapconductor.example.pages.offlinemap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.compose.marker.Marker
import com.mapconductor.compose.polygon.Polygon
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.VectorStyleSupportKey
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.pages.vectortile.VectorTileStyleLoader
import com.mapconductor.example.pages.vectortile.vectorTileGeometryPixelRatio
import com.mapconductor.example.pages.vectortile.showProviderBasemap
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.vectortile.OfflinePackage
import com.mapconductor.vectortile.OfflinePackageDownloader
import com.mapconductor.vectortile.VectorTileLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A map that works with the network off.
 *
 * "Select area…" enters download mode: two draggable markers mark the
 * south-west and north-east corners of an area, and everything outside it is
 * shaded. "Download" then fetches the vector tiles, glyphs and sprite the
 * style needs to draw that area into a package on the device, and leaves
 * download mode. "Airplane mode" then cuts the layer off from the network:
 * inside the area the map is drawn from the package, outside it there is
 * nothing to draw -- which is how you can see the package working.
 */
@Composable
fun OfflineMapPage(
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val packageDir = remember { context.filesDir.resolve("offline-map") }

    var styleJson by remember { mutableStateOf<String?>(null) }
    var failure by remember { mutableStateOf<String?>(null) }
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<*>?>(null) }
    var offlinePackage by remember { mutableStateOf(OfflinePackage.open(packageDir)) }
    var progress by remember { mutableStateOf<OfflinePackageDownloader.Progress?>(null) }
    var airplane by remember { mutableStateOf(false) }
    // Download mode: the area markers and the shade are shown only while an
    // area is being chosen. The rest of the time the page is just the map.
    var selecting by remember { mutableStateOf(false) }
    var stats by remember { mutableStateOf<OfflinePackage.Stats?>(null) }
    var diagnostics by remember { mutableStateOf<List<String>>(emptyList()) }

    // Central Tokyo, an area a few kilometres across: small enough to fetch
    // in seconds, large enough to pan around inside. A launch extra
    // `offlineCenter` ("lat,lon") starts elsewhere, for trying other places.
    val initCameraPosition =
        remember {
            val requested =
                (context as? android.app.Activity)?.intent?.getStringExtra("offlineCenter")
                    ?.split(',')?.takeIf { it.size == 2 }
                    ?.let { runCatching { GeoPoint(it[0].trim().toDouble(), it[1].trim().toDouble()) }.getOrNull() }
            MapCameraPosition(
                position = requested ?: GeoPoint.fromLatLong(latitude = 35.6805, longitude = 139.7675),
                zoom = 13.0,
                bearing = 0.0,
                tilt = 0.0,
                paddings = null,
            )
        }
    var southWest by remember { mutableStateOf(GeoPoint(35.6650, 139.7450)) }
    var northEast by remember { mutableStateOf(GeoPoint(35.6960, 139.7900)) }

    // The mask: a wide ring with the area cut out of it, so the area is the
    // one clear patch on a darkened map. The ring follows the area -- a hole
    // has to lie inside its ring, and one that does not is not a hole but a
    // second, filled polygon (which is what the map drew when the ring was
    // left over Tokyo and the area was chosen in Los Angeles).
    val mask =
        remember {
            PolygonState(
                id = "offline-mask",
                points = maskRing(southWest, northEast),
                holes = listOf(rectangle(southWest, northEast)),
                fillColor = Color(0x80000000),
                strokeColor = Color.Transparent,
                strokeWidth = 0.dp,
                clickable = false,
                // Above the tiles: on backends that order tile overlays and
                // shapes in one z space, a shade at 0 sits under the map.
                zIndex = 10_000,
            )
        }
    fun cornerMoved(dragged: MarkerState) {
        val point = GeoPoint.from(dragged.position)
        if (dragged.id == "sw") southWest = point else northEast = point
        mask.points = maskRing(southWest, northEast)
        mask.holes = listOf(rectangle(southWest, northEast))
    }
    lateinit var swMarker: MarkerState
    lateinit var neMarker: MarkerState

    /** Enters download mode with the area centred on the map as it is now. */
    fun beginSelection() {
        val center = mapViewState?.cameraPosition?.position?.let { GeoPoint.from(it) } ?: GeoPoint(35.6805, 139.7675)
        southWest = GeoPoint(center.latitude - 0.0155, center.longitude - 0.0225)
        northEast = GeoPoint(center.latitude + 0.0155, center.longitude + 0.0225)
        swMarker.position = southWest
        neMarker.position = northEast
        mask.points = maskRing(southWest, northEast)
        mask.holes = listOf(rectangle(southWest, northEast))
        selecting = true
    }
    swMarker =
        remember {
            MarkerState(
                id = "sw",
                position = southWest,
                draggable = true,
                clickable = false,
                icon = ColorDefaultIcon(fillColor = Color(0xFF1D4ED8), strokeColor = Color.White, label = "SW", labelTextColor = Color.White),
                onDrag = ::cornerMoved,
                onDragEnd = ::cornerMoved,
            )
        }
    neMarker =
        remember {
            MarkerState(
                id = "ne",
                position = northEast,
                draggable = true,
                clickable = false,
                icon = ColorDefaultIcon(fillColor = Color(0xFFB91C1C), strokeColor = Color.White, label = "NE", labelTextColor = Color.White),
                onDrag = ::cornerMoved,
                onDragEnd = ::cornerMoved,
            )
        }

    // The style comes from the package when there is one -- it carries the
    // style it was made from -- and from the network only when there is not.
    // Opening the page with the device offline and a package on it must not
    // need the network for anything.
    LaunchedEffect(offlinePackage?.manifest?.createdAt) {
        if (styleJson != null) return@LaunchedEffect
        offlinePackage?.let { pkg ->
            styleJson = pkg.styleJson
            return@LaunchedEffect
        }
        runCatching { withContext(Dispatchers.IO) { VectorTileStyleLoader.load() } }
            .onSuccess { styleJson = it }
            .onFailure { failure = "style could not be fetched (offline with no package?): ${it.message ?: it}" }
    }

    // The style is the map here: the backend's own basemap is blanked
    // wherever the backend can, and the MapLibre-based backends take the
    // style directly (and read the package through the local server).
    LaunchedEffect(mapViewState) {
        val state = mapViewState ?: return@LaunchedEffect
        if (state.serviceRegistry.has(VectorStyleSupportKey)) return@LaunchedEffect
        showProviderBasemap(state, visible = false)
    }

    fun download() {
        val style = styleJson ?: return
        val bounds =
            OfflinePackage.Bounds(
                south = minOf(southWest.latitude, northEast.latitude),
                west = minOf(southWest.longitude, northEast.longitude),
                north = maxOf(southWest.latitude, northEast.latitude),
                east = maxOf(southWest.longitude, northEast.longitude),
            )
        scope.launch {
            offlinePackage = null
            runCatching {
                OfflinePackageDownloader.download(
                    styleJson = style,
                    bounds = bounds,
                    minZoom = 0,
                    maxZoom = 14,
                    directory = packageDir,
                    onProgress = { progress = it },
                )
            }.onSuccess {
                offlinePackage = it
                progress = null
                selecting = false
            }.onFailure {
                failure = "download failed: ${it.message}"
                progress = null
            }
        }
    }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { mapViewState = it },
    ) { paddings ->
        Box(
            modifier = modifier.fillMaxSize().padding(paddings),
            contentAlignment = Alignment.Center,
        ) {
            mapViewState?.let { state ->
                MapViewContainer(state = state) {
                    val style = styleJson
                    if (style != null) {
                        // Keyed on the mode so that flipping it rebuilds the
                        // layer: every tile and every source is then fetched
                        // afresh, and what the package cannot answer stays
                        // blank instead of lingering from a cache. That is the
                        // point of the demonstration.
                        key(airplane, offlinePackage) {
                            VectorTileLayer(
                                styleJson = style,
                                asBasemap = true,
                                geometryPixelRatio = vectorTileGeometryPixelRatio(state),
                                offlinePackage = offlinePackage,
                                online = !airplane,
                                onOfflineStats = { stats = it },
                                onDiagnostics = { diagnostics = it },
                            )
                        }
                    }
                    if (selecting) {
                        Polygon(mask)
                        Marker(swMarker)
                        Marker(neMarker)
                    }
                }
            }

            Card(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 28.dp),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (selecting) {
                            Button(
                                onClick = ::download,
                                enabled = styleJson != null && progress == null,
                                modifier = Modifier.testTag("downloadButton"),
                            ) { Text(if (progress == null) "Download" else "Downloading…", fontSize = 12.sp) }
                            OutlinedButton(
                                onClick = { selecting = false },
                                enabled = progress == null,
                                modifier = Modifier.testTag("cancelSelectButton"),
                            ) { Text("Cancel", fontSize = 12.sp) }
                        } else {
                            Button(
                                onClick = ::beginSelection,
                                enabled = styleJson != null,
                                modifier = Modifier.testTag("selectAreaButton"),
                            ) { Text("Select area…", fontSize = 12.sp) }
                            FilterChip(
                                selected = airplane,
                                onClick = { airplane = !airplane },
                                label = { Text(if (airplane) "✈ Airplane mode ON" else "✈ Airplane mode", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(),
                                modifier = Modifier.testTag("airplaneToggle"),
                            )
                            OutlinedButton(
                                onClick = {
                                    OfflinePackage.delete(packageDir)
                                    offlinePackage = null
                                    stats = null
                                },
                                enabled = offlinePackage != null,
                            ) { Text("Clear", fontSize = 12.sp) }
                        }
                    }
                    val pkg = offlinePackage
                    val status =
                        when {
                            progress != null -> "downloading: $progress"
                            selecting -> "drag SW/NE to choose the area, then Download"
                            pkg == null -> "no package: Select area…, then Download"
                            else ->
                                "package: ${pkg.manifest.tiles} tiles, ${pkg.manifest.glyphs} glyph ranges, " +
                                    "${pkg.manifest.sprites} sprite files, z${pkg.manifest.minZoom}-${pkg.manifest.maxZoom}, " +
                                    "${pkg.manifest.bytes / 1024} KB"
                        }
                    Text(status, fontSize = 12.sp, modifier = Modifier.testTag("offlineStatus"))
                    Text(
                        "mode=${if (airplane) "offline" else "online"} " +
                            "fetches: ${stats?.toString() ?: "-"}",
                        fontSize = 12.sp,
                        modifier = Modifier.testTag("offlineFetches"),
                    )
                    diagnostics.forEach { Text(it, fontSize = 11.sp, color = Color(0xFF7A4A00)) }
                    failure?.let { Text(it, fontSize = 11.sp, color = Color.Red) }
                }
            }
        }
    }
}

/**
 * The shade's outer ring: several degrees around the area, wherever it is,
 * kept inside the map's latitude and longitude range so it never wraps.
 */
private fun maskRing(
    a: GeoPoint,
    b: GeoPoint,
): List<GeoPoint> {
    val pad = 6.0
    val south = (minOf(a.latitude, b.latitude) - pad).coerceAtLeast(-85.0)
    val north = (maxOf(a.latitude, b.latitude) + pad).coerceAtMost(85.0)
    val west = (minOf(a.longitude, b.longitude) - pad).coerceAtLeast(-180.0)
    val east = (maxOf(a.longitude, b.longitude) + pad).coerceAtMost(180.0)
    return listOf(GeoPoint(south, west), GeoPoint(south, east), GeoPoint(north, east), GeoPoint(north, west))
}

/** The four corners between two opposite ones, as a ring. */
private fun rectangle(
    a: GeoPoint,
    b: GeoPoint,
): List<GeoPoint> {
    val south = minOf(a.latitude, b.latitude)
    val north = maxOf(a.latitude, b.latitude)
    val west = minOf(a.longitude, b.longitude)
    val east = maxOf(a.longitude, b.longitude)
    return listOf(GeoPoint(south, west), GeoPoint(south, east), GeoPoint(north, east), GeoPoint(north, west))
}
