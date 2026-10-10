package com.mapconductor.example.pages.vectorstyle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.VectorStyleSupportKey
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.pages.vectortile.VectorTileStyleLoader
import com.mapconductor.example.pages.vectortile.showProviderBasemap
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.utils.LoadingDialog
import com.mapconductor.vectorstyle.LayerRole
import com.mapconductor.vectorstyle.StyleRules
import com.mapconductor.vectorstyle.VectorStyle
import com.mapconductor.vectorstyle.VectorStyleSource
import com.mapconductor.vectortile.VectorTileRasteriser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The same style adjustment on every backend, through `MapView(style = ...)`.
 *
 * Two things are worth watching rather than just looking at:
 *
 * - **The provider switcher.** MapLibre, Mapbox and MapTiler are handed the
 *   document and then told the per-layer differences; the rest cannot read a
 *   style at all and say so in the diagnostics (until the rasteriser is
 *   wired in). Same rules, and the map that can take them looks the way the
 *   rules asked.
 * - **The road-colour slider.** Dragging it writes a new rule set on every
 *   frame, which on a vector backend sends differences and reloads nothing.
 *   If a drag makes the tiles flash, the in-place path is broken and the
 *   style is being reinstalled — which is the whole failure this design
 *   exists to prevent, and it is visible to the eye.
 */
@Composable
fun VectorStylePage(
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit = {},
) {
    var styleJson by remember { mutableStateOf<String?>(null) }
    var failure by remember { mutableStateOf<String?>(null) }
    var diagnostics by remember { mutableStateOf<List<String>>(emptyList()) }
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<*>?>(null) }
    /** Set once the map is up, which is when its capabilities are registered. */
    var loadedState by remember { mutableStateOf<MapViewStateInterface<*>?>(null) }
    var preset by remember { mutableStateOf(Preset.ROAD_EMPHASIS) }
    var roadLightness by remember { mutableFloatStateOf(1.0f) }
    // The style the app supplies, or the one the map happens to be drawing.
    // The second is the interesting case and the one that can fail: a backend
    // with no vector style has nothing to adjust, and says so.
    var fromCurrentDesign by remember { mutableStateOf(false) }

    val initCameraPosition =
        remember {
            MapCameraPosition(
                position = GeoPoint.fromLatLong(latitude = 35.68049, longitude = 139.76669),
                zoom = 12.0,
                bearing = 0.0,
                tilt = 0.0,
                paddings = null,
            )
        }

    /**
     * A style *is* the basemap, so the backend's own one is blanked while
     * one is drawn — but only where it has to be.
     *
     * Not for a backend that reads the style itself: it replaces its own
     * design, and blanking first would load a basemap to throw it away.
     *
     * And **not while adjusting the map's own style**: there the whole point
     * is the design the backend is already drawing, so blanking it is
     * self-defeating. On a backend that cannot read a style at all (Open
     * Mobile Maps draws raster XYZ tiles and has no style to read) that mode
     * cannot work, and leaving the basemap up shows the map it could not
     * adjust next to the message saying so — rather than a white screen.
     */
    LaunchedEffect(loadedState, fromCurrentDesign) {
        val loaded = loadedState ?: return@LaunchedEffect
        val readsStyles = loaded.serviceRegistry.has(VectorStyleSupportKey)
        showProviderBasemap(loaded, visible = readsStyles || fromCurrentDesign)
    }

    LaunchedEffect(Unit) {
        runCatching { withContext(Dispatchers.IO) { VectorTileStyleLoader.load() } }
            .onSuccess { styleJson = it }
            .onFailure { failure = it.message ?: it.toString() }
    }

    /**
     * What draws the style on a backend that cannot read one.
     *
     * Google Maps, MapKit, HERE, ArcGIS, Longdo, TomTom and the rest have no
     * vector renderer; this renders the style to tiles on the device. The
     * three that *can* read a style never call it.
     */
    val rasteriser = remember { VectorTileRasteriser() }

    val roadColor = Color(roadLightness, roadLightness, roadLightness)
    val rules =
        remember(preset, roadColor) {
            when (preset) {
                Preset.NONE -> StyleRules.NONE
                Preset.ROAD_EMPHASIS ->
                    StyleRules.build {
                        all { color = Color.Black }
                        role(LayerRole.LABEL) { visible = false }
                        role(LayerRole.ROAD_CASING) { color = Color(0xFF303030) }
                        role(LayerRole.ROAD) {
                            color = roadColor
                            widthScale = 1.4f
                        }
                    }
                Preset.NIGHT ->
                    StyleRules.build {
                        all { darken(0.55f) }
                        role(LayerRole.ROAD) { color = roadColor }
                        role(LayerRole.WATER) { color = Color(0xFF0B1B2B) }
                    }
                Preset.WATER_ONLY ->
                    StyleRules.build {
                        all { visible = false }
                        role(LayerRole.BACKGROUND) { visible = true; color = Color(0xFFF2F2F2) }
                        role(LayerRole.WATER) { visible = true; color = Color(0xFF2E6FB7) }
                    }
            }
        }

    // A new instance on every slider frame, by design: what decides whether
    // the map reinstalls is `MapViewStyle.key`, not identity. Same document
    // plus different rules is the cheap path.
    val style =
        if (fromCurrentDesign) {
            VectorStyle(
                document = VectorStyleSource.CurrentDesign,
                rules = rules,
                rasteriser = rasteriser,
                onDiagnostics = { diagnostics = it },
            )
        } else {
            styleJson?.let { json ->
                VectorStyle(
                    document = VectorStyleSource.Text(json),
                    rules = rules,
                    rasteriser = rasteriser,
                    onDiagnostics = { diagnostics = it },
                )
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
                MapViewContainer(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    style = style,
                    onStyleDiagnostics = { diagnostics = it },
                    // **On map-loaded, not when the state arrives.** The
                    // capability is registered while the controller is being
                    // built, so asking earlier says "cannot read a style"
                    // about MapLibre — and then `CurrentDesign` finds a blank
                    // design and reports that there is nothing to adjust.
                    onMapLoaded = { loadedState = it },
                )
            }

            Card(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        // Clear of the attribution band: this page draws
                        // OpenStreetMap, whose licence needs the credit
                        // readable.
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 28.dp),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Preset.entries.forEach { option ->
                            FilterChip(
                                selected = preset == option,
                                onClick = { preset = option },
                                label = { Text(option.label, fontSize = 12.sp) },
                            )
                        }
                    }
                    FilterChip(
                        selected = fromCurrentDesign,
                        onClick = { fromCurrentDesign = !fromCurrentDesign },
                        label = { Text("Adjust the map's own style", fontSize = 12.sp) },
                    )
                    Text("Road lightness ${"%.2f".format(roadLightness)}", fontSize = 12.sp)
                    Slider(
                        value = roadLightness,
                        onValueChange = { roadLightness = it },
                        valueRange = 0f..1f,
                    )
                    // The rule that matched nothing, the adjustment this
                    // backend would not take, the style that could not be
                    // read: the failures here all look like "it nearly
                    // worked" and nothing else reports them.
                    diagnostics.forEach { Text(it, fontSize = 11.sp, color = Color(0xFF7A4A00)) }
                    failure?.let { Text("style failed: $it", fontSize = 11.sp, color = Color.Red) }
                }
            }

            if (styleJson == null && failure == null && !fromCurrentDesign) {
                LoadingDialog(title = "Vector style", message = "Loading style…")
            }
        }
    }
}

private enum class Preset(
    val label: String,
) {
    ROAD_EMPHASIS("Roads"),
    NIGHT("Night"),
    WATER_ONLY("Water"),
    NONE("As authored"),
}
