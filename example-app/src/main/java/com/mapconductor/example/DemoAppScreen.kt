package com.mapconductor.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.rememberArcGISMapViewState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.toFixed
import com.mapconductor.example.navigation.NavigationViewModel
import com.mapconductor.example.pages.MapExamplePage
import com.mapconductor.example.pages.stores.StoreMapPage
import com.mapconductor.example.pages.stores.StoreMapPageViewModel
import com.mapconductor.example.ui.IconItem
import com.mapconductor.example.ui.IconSelectMenu
import com.mapconductor.example.ui.sidebar.Sidebar
import com.mapconductor.example.ui.sidebar.SidebarItem
import com.mapconductor.example.ui.theme.AppTheme
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.here.HereMapDesign
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.rememberMapboxMapViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoAppScreen(
    appViewModel: AppViewModel,
    storeMapPageViewModel: StoreMapPageViewModel,
) {
    val navigationViewModel: NavigationViewModel = viewModel()
    val currentPage by navigationViewModel.currentPage
    val isSidebarExpanded by navigationViewModel.isSidebarExpanded

    // ---------- Map States ---------------
    val googleMapState = rememberGoogleMapViewState(
        mapDesign = GoogleMapDesign.Normal,
        cameraPosition = appViewModel.initCameraPosition,
    )
    val mapboxMapState = rememberMapboxMapViewState(
        mapDesign = MapboxMapDesign.Standard,
        cameraPosition = appViewModel.initCameraPosition,
    )
    val hereMapState = rememberHereMapViewState(
        mapDesign = HereMapDesign.NormalDay,
        cameraPosition = appViewModel.initCameraPosition,
    )
    val elevationSources = listOf(
        "https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer",
    )
    val arcGISMapState = rememberArcGISMapViewState(
        mapDesign = ArcGISDesign.Streets.withElevationSources(elevationSources),
        cameraPosition = appViewModel.initCameraPosition,
    )

    val menuItems = listOf(
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

    var selectedIndex by rememberSaveable { mutableIntStateOf(3) }
    LaunchedEffect(selectedIndex) {
        appViewModel.changeState(menuItems.elementAt(selectedIndex).value)
    }

    val sidebarItems = listOf(
        SidebarItem(
            id = "map",
            title = "Map Demo",
            icon = Icons.Default.Home,
            route = "map"
        ),
        SidebarItem(
            id = "examples",
            title = "Map Examples",
            icon = Icons.Default.LocationOn,
            route = "examples"
        ),
        SidebarItem(
            id = "settings",
            title = "Settings",
            icon = Icons.Default.Settings,
            route = "settings"
        )
    )

    val mapViewState = appViewModel.mapViewState.collectAsState().value
    val camera = mapViewState?.mapCameraPosition?.collectAsState()?.value

    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Column(modifier = Modifier.fillMaxSize()) {
                if (currentPage == "map") {
                    TopAppBar(
                        title = {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open menu",
                                    modifier = Modifier
                                        .clickable(
                                            onClick = navigationViewModel::toggleSidebar,
                                        )
                                        .size(32.dp)
                                        .padding(end = 10.dp)
                                        .align(Alignment.CenterVertically)
                                )
//                                Button(
//                                    onClick = navigationViewModel::toggleSidebar,
//                                    modifier = Modifier
//                                        .size(56.dp)
//                                ) {
//                                }
                                IconSelectMenu(
                                    modifier = Modifier.weight(0.7f),
                                    itemList = menuItems,
                                    selectedIndex = selectedIndex,
                                    onSelect = { index, _ ->
                                        selectedIndex = index
                                    },
                                )
//                                Button(
//                                    modifier = Modifier.weight(0.3f),
//                                    onClick = appViewModel::cameraReset,
//                                ) {
//                                    Text("Reset")
//                                }
                            }
                        },
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentPage) {
                        "map" -> {
                            StoreMapPage(storeMapPageViewModel)
                        }
                        "examples" -> MapExamplePage(appViewModel)
                        "settings" -> {
                            // Placeholder for settings page
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Settings page coming soon...")
                            }
                        }
                    }
                }
            }

            // Floating menu button (only show when sidebar is closed)
//            if (!isSidebarExpanded) {
//                FloatingActionButton(
//                    onClick = navigationViewModel::toggleSidebar,
//                    modifier = Modifier
//                        .align(Alignment.TopStart)
//                        .padding(16.dp)
//                        .size(56.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Menu,
//                        contentDescription = "Open menu"
//                    )
//                }
//            }

            // Overlay sidebar
            Sidebar(
                items = sidebarItems,
                selectedItemId = currentPage,
                onItemClick = { item ->
                    navigationViewModel.navigateTo(item.id)
                },
                isExpanded = isSidebarExpanded,
                onToggleSidebar = navigationViewModel::toggleSidebar
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

//        Canvas(
//            modifier = Modifier.size(34.dp)
//                .padding(100.dp)
//        ) {
//            drawRect(
//                color = Color.Red,
//                size = Size(34.dp.toPx(), 34.dp.toPx()),
//                style = DrawStyle.Stroke,
//            )
//        }
    }
}
