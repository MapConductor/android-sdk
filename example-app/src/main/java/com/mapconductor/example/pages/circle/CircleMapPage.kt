package com.mapconductor.example.pages.circle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.rememberArcGISMapViewState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.toFixed
import com.mapconductor.example.R
import com.mapconductor.example.toast.ToastHost
import com.mapconductor.example.ui.IconItem
import com.mapconductor.example.ui.IconSelectMenu
import com.mapconductor.example.ui.MessageCard
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.here.HereMapDesign
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.rememberMapboxMapViewState

@Composable
fun CircleMapPage(
    viewModel: CirclePageViewModel,
    onToggleSidebar: () -> Unit = {},
) {
    // ---------- Map States ---------------
    val googleMapState =
        rememberGoogleMapViewState(
            mapDesign = GoogleMapDesign.Normal,
            cameraPosition = viewModel.initCameraPosition,
        )
    val mapboxMapState =
        rememberMapboxMapViewState(
            mapDesign = MapboxMapDesign.Standard,
            cameraPosition = viewModel.initCameraPosition,
        )
    val hereMapState =
        rememberHereMapViewState(
            mapDesign = HereMapDesign.NormalDay,
            cameraPosition = viewModel.initCameraPosition,
        )
    val elevationSources =
        listOf(
            "https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer",
        )
    val arcGISMapState =
        rememberArcGISMapViewState(
            mapDesign = ArcGISDesign.Streets.withElevationSources(elevationSources),
            cameraPosition = viewModel.initCameraPosition,
        )

    val menuItems =
        listOf(
            IconItem(
                key = "googlemap",
                label = "Google Map",
                lightIconResId = R.drawable.google_maps_logo,
                darkIconResId = R.drawable.google_maps_logo,
                value = googleMapState,
            ),
            IconItem(
                key = "mapbox",
                label = "Mapbox",
                lightIconResId = R.drawable.mapbox_logo_black,
                darkIconResId = R.drawable.mapbox_logo_white,
                value = mapboxMapState,
            ),
            IconItem(
                key = "heremap",
                label = "Here",
                lightIconResId = R.drawable.here_logo_black,
                darkIconResId = R.drawable.here_logo_white,
                value = hereMapState,
            ),
            IconItem(
                key = "arcgis",
                label = "ArcGIS",
                lightIconResId = R.drawable.arcgis_logo_black,
                darkIconResId = R.drawable.arcgis_logo_white,
                value = arcGISMapState,
            ),
        )

    var selectedIndex by rememberSaveable { mutableIntStateOf(2) }
    LaunchedEffect(selectedIndex) {
        viewModel.changeState(menuItems.elementAt(selectedIndex).value)
    }

    val mapViewState = viewModel.mapViewState.collectAsState().value

    Scaffold(
        bottomBar = {
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize().padding(
                    start = paddingValues.calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
                    end = paddingValues.calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
                    bottom = paddingValues.calculateBottomPadding(),
                ),
        ) {
            CircleMapComponent(
                mapViewState = mapViewState,
                viewModel = viewModel,
                onMapClickHandler = viewModel::onMapClick,
                onMarkerClickHandler = viewModel::onMarkerClick,
                onCircleClickHandler = viewModel::onCircleClick,
                onMarkerDrag = viewModel::onMarkerDrag,
            )

            // Top controls
            Card(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = paddingValues.calculateTopPadding(),
                            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 10.dp,
                            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 10.dp,
                        ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open menu",
                        modifier =
                            Modifier
                                .clickable(onClick = onToggleSidebar)
                                .size(32.dp)
                                .padding(end = 10.dp),
                    )
                    IconSelectMenu(
                        modifier = Modifier.weight(0.7f),
                        itemList = menuItems,
                        selectedIndex = selectedIndex,
                        onSelect = { index, _ ->
                            selectedIndex = index
                        },
                    )
                }
            }

            // Message Card
            MessageCard(
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            bottom = paddingValues.calculateBottomPadding() + 16.dp,
                            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                        ),
                title = "Messages",
            ) {
                DebugPanel(mapViewState!!.mapCameraPosition.collectAsState().value)
            }

            ToastHost(
                messages = viewModel.messages.collectAsState().value,
                onDismiss = { viewModel.removeToast(it) },
            )
        }
    }
}

@Composable
fun BoxScope.DebugPanel(camera: MapCameraPosition?) {
    Column(
        modifier =
            Modifier
                .align(Alignment.TopEnd)
                .background(
                    Color(
                        red = 0.9f,
                        green = 0.9f,
                        blue = 0.9f,
                        alpha = 0.75f,
                    ),
                ).wrapContentHeight()
                .fillMaxWidth(),
    ) {
        Text("LatLng: (${camera?.position?.toUrlValue()})", color = Color.Black)
        Text("Zoom: ${camera?.zoom?.toFixed(2)}", color = Color.Black)
        Text("bearing: ${camera?.bearing?.toInt()}", color = Color.Black)
        Text("tilt: ${camera?.tilt?.toInt()}", color = Color.Black)
    }
}
