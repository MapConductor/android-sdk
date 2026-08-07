package com.mapconductor.example.pages.map.uisettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapconductor.core.map.MapUISettings
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.tomtom.TomTomMapViewState
import android.util.Log
import kotlinx.coroutines.delay

/**
 * Exercises [MapUISettings] against a live map.
 *
 * Four switches flip the gesture flags; after each change the page reads the
 * values back off the provider's own map object and logs them under the
 * `MCUISettings` tag, so a device run can be verified from logcat without having
 * to touch the screen. On start it also cycles None -> Default automatically.
 */
@Composable
fun UISettingsMapPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { UISettingsMapPageViewModel() }
    // A UI test presets the flags through an intent extra rather than tapping the
    // switches, which is far more reliable than driving a Compose Switch by coordinate.
    var settings by remember {
        mutableStateOf(
            if (com.mapconductor.example.MainActivity.gesturesExtra == "none") {
                MapUISettings.None
            } else {
                MapUISettings.Default
            },
        )
    }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { _ ->
        val mapViewState = viewModel.mapViewState.collectAsState().value

        mapViewState?.let { state ->
            state.uiSettings = settings
            MapViewContainer(
                state = state,
                onCameraMove = { c ->
                    Log.i("MCCamera", "%.5f,%.5f".format(c.position.latitude, c.position.longitude))
                },
            )

            // Read back after every change, so manual toggles are verifiable too.
            androidx.compose.runtime.LaunchedEffect(settings, state) {
                delay(800)
                report("settings=$settings", state)
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                GestureSwitch("scrollGesture", settings.scrollGesture) {
                    settings = settings.copy(scrollGesture = it)
                }
                GestureSwitch("zoomGesture", settings.zoomGesture) {
                    settings = settings.copy(zoomGesture = it)
                }
                GestureSwitch("rotateGesture", settings.rotateGesture) {
                    settings = settings.copy(rotateGesture = it)
                }
                GestureSwitch("tiltGesture", settings.tiltGesture) {
                    settings = settings.copy(tiltGesture = it)
                }
            }
        }
    }
}

@Composable
private fun GestureSwitch(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.padding(end = 12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** Reads the gesture flags back off the native map so logcat shows ground truth. */
private fun report(
    phase: String,
    state: Any,
) {
    val holder = (state as? com.mapconductor.core.map.MapViewStateInterface<*>)?.getMapViewHolder()
    val map = holder?.map
    val actual =
        when (state) {
            is TomTomMapViewState -> {
                val ttMap = map as? com.tomtom.sdk.map.display.TomTomMap
                if (ttMap == null) {
                    "map unavailable"
                } else {
                    "scroll=${ttMap.isScrollEnabled} zoom=${ttMap.isZoomEnabled} " +
                        "rotate=${ttMap.isRotationEnabled} tilt=${ttMap.isTiltEnabled}"
                }
            }
            is com.mapconductor.googlemaps.GoogleMapViewState -> {
                val ui = (map as? com.google.android.gms.maps.GoogleMap)?.uiSettings
                if (ui == null) {
                    "map unavailable"
                } else {
                    "scroll=${ui.isScrollGesturesEnabled} zoom=${ui.isZoomGesturesEnabled} " +
                        "rotate=${ui.isRotateGesturesEnabled} tilt=${ui.isTiltGesturesEnabled}"
                }
            }
            is com.mapconductor.maplibre.MapLibreViewState -> {
                val ui = (map as? org.maplibre.android.maps.MapLibreMap)?.uiSettings
                if (ui == null) {
                    "map unavailable"
                } else {
                    "scroll=${ui.isScrollGesturesEnabled} zoom=${ui.isZoomGesturesEnabled} " +
                        "rotate=${ui.isRotateGesturesEnabled} tilt=${ui.isTiltGesturesEnabled}"
                }
            }
            else -> "applied (no native reader for ${state::class.simpleName})"
        }
    Log.i("MCUISettings", "$phase | ${state::class.simpleName} | $actual")
}
