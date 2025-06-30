package com.mapconductor.example

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.arcgismaps.data.DrawingTool
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.rememberArcGISMapViewState
import com.mapconductor.core.icons.Default
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.core.toFixed
import com.mapconductor.example.toast.ToastHost
import com.mapconductor.example.ui.IconItem
import com.mapconductor.example.ui.IconSelectMenu
import com.mapconductor.example.ui.theme.AppTheme
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.here.HereMapDesign
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.rememberMapboxMapViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoAppScreen(appViewModel: AppViewModel) {
    // ---------- GoogleMaps ---------------
    val googleMapState =
        rememberGoogleMapViewState(
            mapDesign = GoogleMapDesign.Normal,
            cameraPosition = appViewModel.initCameraPosition,
        )
    // ---------- Mapbox ---------------
    val mapboxMapState =
        rememberMapboxMapViewState(
            mapDesign = MapboxMapDesign.Standard,
            cameraPosition = appViewModel.initCameraPosition,
        )
    // ---------- Here ---------------
    val hereMapState =
        rememberHereMapViewState(
            mapDesign = HereMapDesign.NormalDay,
            cameraPosition = appViewModel.initCameraPosition,
        )

    // ---------- ArcGIS ---------------
    val elevationSources =
        listOf(
            "https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer",
        )
    val arcGISMapState =
        rememberArcGISMapViewState(
            mapDesign = ArcGISDesign.Streets.withElevationSources(elevationSources),
            cameraPosition = appViewModel.initCameraPosition,
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
    val context = LocalContext.current

    var selectedIndex by rememberSaveable { mutableIntStateOf(1) }
    LaunchedEffect(selectedIndex) {
        appViewModel.changeState(menuItems.elementAt(selectedIndex).value)
    }

    AppCompatResources.getDrawable(context, R.drawable.coffee_svg)
//    val icon = MarkerIcon(
//        iconDrawable = drawable,
//        fillColor = Color(0x6f, 0x4e, 0x37).toArgb(),
//        strokeColor = Color.LightGray.toArgb()
//    )
//    val icon = MarkerIcon.Default()
//    val icon = MarkerIcon.StarInCircle()
//    val icon = MarkerIcon.HeartInCircle()
//    val icon = MarkerIcon.SquareMarker()
//    val icon = MarkerIcon.Triangle()
    val image = AppCompatResources.getDrawable(context, R.drawable.def)
//    val icon = MarkerIcon.ImageInCircle(image = image!!)

    val markerList =
        remember {
            appViewModel.markerList.map {
                it.copy(
                    draggable = true,
                )
            }
        }
    val mapViewState = appViewModel.mapViewState.collectAsState().value
    val camera = mapViewState?.mapCameraPosition?.collectAsState()?.value

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            IconSelectMenu(
                                modifier = Modifier.weight(0.2f),
                                itemList = menuItems,
                                selectedIndex = selectedIndex,
                                onSelect = { index, _ ->
                                    selectedIndex = index
                                },
                            )
                            Button(
                                modifier = Modifier.weight(0.1f),
                                onClick = appViewModel::flyTo,
                            ) {
                                Text("Fly to!")
                            }
                        }
                    },
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding),
            ) {
                MapArea(
                    mapViewState = mapViewState,
                    markers = markerList,
                    onDirectionButtonClick = { state ->
                        state.icon =
                            MarkerIcon.Default(
                                fillColor = Color.Blue.toArgb(),
                            )

                        state.animation = MarkerAnimation.Bounce
//                        val intent = appViewModel.createIntentForDirection(state)
//                        context.startActivity(intent)
                    },
                    infoBubbleState = appViewModel.infoBubbleState,
                    onMapClickHandler = appViewModel::onMapClick,
                    onMarkerClickHandler = appViewModel::onMarkerClick,
                    selectedMarker = appViewModel.selectedMarker,
                )
                DebugPanel(
                    camera = camera,
                )
                ToastHost(
                    messages = appViewModel.messages.collectAsState().value,
                    onDismiss = { appViewModel.removeToast(it) },
                )
            }
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
