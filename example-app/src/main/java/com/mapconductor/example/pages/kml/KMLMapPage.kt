package com.mapconductor.example.pages.kml

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.info.InfoBubble
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import com.mapconductor.kml.KMLFeature
import com.mapconductor.kml.KMLLayer
import com.mapconductor.kml.KMLLayerState
import com.mapconductor.kml.KMLParser
import com.mapconductor.utils.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val KML_ASSET = "sample.kml"

@Composable
fun KMLMapPage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition =
        remember {
            MapCameraPosition(
                position = GeoPoint(35.685, 139.76),
                zoom = 13.0,
            )
        }
    var mapViewState by remember { mutableStateOf<MapViewStateInterface<*>?>(null) }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { mapViewState = it },
    ) { paddingValues ->
        KMLMapComponent(mapViewState = mapViewState)

        MessageCard(
            title = "KML Layer",
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
        ) {
            Text("Parsed from $KML_ASSET. Tap a feature to inspect its properties.")
        }
    }
}

@Composable
private fun KMLMapComponent(mapViewState: MapViewStateInterface<*>?) {
    val context = LocalContext.current
    var selectedFeature by remember { mutableStateOf<KMLFeature?>(null) }
    var tappedPosition by remember { mutableStateOf<GeoPoint?>(null) }
    var features by remember { mutableStateOf<List<KMLFeature>>(emptyList()) }
    var isDataLoading by remember { mutableStateOf(true) }

    val layerState =
        remember {
            KMLLayerState(
                // Fallback style used when a placemark carries no KML <Style>.
                strokeColor = android.graphics.Color.argb(255, 250, 36, 29),
                fillColor = android.graphics.Color.argb(96, 250, 36, 29),
                strokeWidth = 3f,
                pointRadius = 8f,
                onLoadStart = { isDataLoading = true },
                onLoadComplete = { _ -> isDataLoading = false },
                onClick = { feature, position ->
                    selectedFeature = feature
                    tappedPosition = GeoPoint.from(position)
                },
            )
        }

    LaunchedEffect(Unit) {
        try {
            layerState.onLoadStart?.invoke()
            features =
                withContext(Dispatchers.IO) {
                    context.assets.open(KML_ASSET).use { KMLParser.parse(it) }
                }
            layerState.onLoadComplete?.invoke(null)
        } catch (e: Throwable) {
            layerState.onLoadComplete?.invoke(e)
        }
    }

    mapViewState?.let { state ->
        MapViewContainer(
            state = state,
            onMapClick = { clicked ->
                if (!layerState.processClick(clicked, pixelTolerance = 12.0, zoom = state.cameraPosition.zoom)) {
                    tappedPosition = null
                    selectedFeature = null
                }
            },
        ) {
            KMLLayer(state = layerState, features = features)

            tappedPosition?.let { position ->
                InfoBubble(position = position) {
                    selectedFeature?.properties?.let { properties ->
                        PropertyTable(properties)
                    }
                }
            }
        }
    }

    if (isDataLoading) {
        LoadingDialog(
            title = "Loading KML",
            message = "Parsing $KML_ASSET...",
        )
    }
}

@Composable
private fun PropertyTable(properties: Map<String, Any?>) {
    Column(
        modifier =
            Modifier
                .width(400.dp)
                .heightIn(max = 300.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        Row(modifier = Modifier.background(Color(0xFFE0E0E0))) {
            PropertyTableCell(text = "Property", weight = 0.35f)
            PropertyTableCell(text = "Value", weight = 0.65f)
        }

        properties.forEach { (key, value) ->
            Row(modifier = Modifier.fillMaxWidth()) {
                PropertyTableCell(text = key, weight = 0.35f)
                PropertyTableCell(text = value?.toString().orEmpty(), weight = 0.65f)
            }
        }
    }
}

@Composable
private fun RowScope.PropertyTableCell(
    text: String,
    weight: Float,
) {
    Text(
        text = text,
        color = Color.Black,
        modifier =
            Modifier
                .border(1.dp, Color.Gray)
                .weight(weight)
                .padding(8.dp),
    )
}
