package com.mapconductor.simplemapapp.docdemo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import com.mapconductor.compose.raster.RasterLayer
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.rememberMapLibreMapViewState

/**
 * /cms/raster-layer — overlaying raster tiles.
 *
 * The page's sample keeps one RasterLayerState and reassigns its source and
 * opacity, so the layer is updated rather than rebuilt. Filmed over Mt Fuji,
 * where the GSI relief tiles differ from the standard ones enough to read at
 * video size: the slider sweeps the opacity, the switch swaps the source, and
 * the base map underneath never reloads.
 *
 * One departure from the printed snippet, which would not compile as written:
 * RasterLayerState takes its source as a constructor argument, so the initial
 * source is given here and only reassigned in the effect.
 */

private val RELIEF = "https://cyberjapandata.gsi.go.jp/xyz/relief/{z}/{x}/{y}.png"
private val STANDARD = "https://cyberjapandata.gsi.go.jp/xyz/std/{z}/{x}/{y}.png"

private fun gsi(template: String) =
    RasterLayerSource.UrlTemplate(
        template = template,
        tileSize = 256,
        minZoom = 5,
        maxZoom = 15,
    )

@Composable
fun RasterLayerDemo(modifier: Modifier = Modifier) {
    val mapViewState =
        rememberMapLibreMapViewState(
            mapDesign = MapLibreDesign.OsmBrightEn,
            cameraPosition =
                MapCameraPosition(
                    // Mt Fuji: relief tiles and standard tiles look nothing alike here.
                    position = GeoPoint.fromLatLong(35.3606, 138.7274),
                    zoom = 10.0,
                ),
        )

    var opacity by remember { mutableFloatStateOf(0.75f) }
    var relief by remember { mutableStateOf(true) }

    val layerState = remember { RasterLayerState(id = "gsi-raster", source = gsi(RELIEF)) }

    // Both source and opacity can be reassigned; the layer is not rebuilt.
    LaunchedEffect(relief, opacity) {
        layerState.source = gsi(if (relief) RELIEF else STANDARD)
        layerState.opacity = opacity
    }

    Column(modifier.fillMaxSize()) {
        DemoCaption(
            "RasterLayer over a vector base map",
            "GSI tiles — source and opacity reassigned in place",
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            MapLibreMapView(state = mapViewState, modifier = Modifier.fillMaxSize()) {
                RasterLayer(layerState)
            }
            DemoReadout(
                listOf(
                    "source   ${if (relief) "relief" else "std"}",
                    "opacity  %.2f".format(opacity),
                ),
                Modifier.align(Alignment.TopStart).padding(12.dp),
            )
        }
        DemoControls {
            Column(Modifier.weight(1f)) {
                Text("opacity", color = Color.White, fontSize = 15.sp)
                Slider(value = opacity, onValueChange = { opacity = it })
            }
            Row(
                Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "relief  ",
                    color = Color.White,
                    fontSize = 15.sp,
                )
                Switch(checked = relief, onCheckedChange = { relief = it })
            }
        }
    }
}
