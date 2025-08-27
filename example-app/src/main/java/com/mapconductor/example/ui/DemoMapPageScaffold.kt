package com.mapconductor.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.rememberArcGISMapViewState
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.example.R
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.here.HereMapDesign
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.rememberMapboxMapViewState

@Composable
fun DemoMapPageScaffold(
    initCameraPosition: IMapCameraPosition,
    initSelect: Int = 0,
    onToggleSidebar: () -> Unit,
    onMapViewStateChanged: (MapViewState<*>) -> Unit = {},
    content: @Composable BoxScope.(PaddingValues) -> Unit = {},
) {
    // ---------- Map States ---------------
    val googleMapState =
        rememberGoogleMapViewState(
            mapDesign = GoogleMapDesign.Normal,
            cameraPosition = initCameraPosition,
        )
    val mapboxMapState =
        rememberMapboxMapViewState(
            mapDesign = MapboxMapDesign.Standard,
            cameraPosition = initCameraPosition,
        )
    val hereMapState =
        rememberHereMapViewState(
            mapDesign = HereMapDesign.NormalDay,
            cameraPosition = initCameraPosition,
        )
    val elevationSources =
        listOf(
            "https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer",
        )
    val arcGISMapState =
        rememberArcGISMapViewState(
            mapDesign = ArcGISDesign.Streets.withElevationSources(elevationSources),
            cameraPosition = initCameraPosition,
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

    var selectedIndex by rememberSaveable { mutableIntStateOf(initSelect) }
    LaunchedEffect(selectedIndex) {
        onMapViewStateChanged(menuItems.elementAt(selectedIndex).value)
    }

    Scaffold { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize().padding(
                    start = paddingValues.calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
                    end = paddingValues.calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
                    bottom = paddingValues.calculateBottomPadding(),
                ),
        ) {
            content(paddingValues)

            Card(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = paddingValues.calculateTopPadding(),
                            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 10.dp,
                            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 10.dp,
                            bottom = paddingValues.calculateBottomPadding(),
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
        }
    }
}
