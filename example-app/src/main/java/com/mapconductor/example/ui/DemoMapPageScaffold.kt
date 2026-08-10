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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.arcgis.rememberArcGISMapViewState
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.R
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.googlemaps.rememberGoogleMapViewState
import com.mapconductor.here.HereMapDesign
import com.mapconductor.here.HereViewState
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.longdo.LongdoDesign
import com.mapconductor.longdo.LongdoViewState
import com.mapconductor.longdo.rememberLongdoMapViewState
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.MapboxViewState
import com.mapconductor.mapbox.rememberMapboxMapViewState
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreViewState
import com.mapconductor.maplibre.rememberMapLibreMapViewState
import com.mapconductor.maptiler.MapTilerDesign
import com.mapconductor.maptiler.MapTilerViewState
import com.mapconductor.maptiler.rememberMapTilerMapViewState
import com.mapconductor.openmobilemaps.OpenMobileMapsDesign
import com.mapconductor.openmobilemaps.OpenMobileMapsViewState
import com.mapconductor.openmobilemaps.rememberOpenMobileMapsMapViewState
import com.mapconductor.tomtom.TomTomMapDesign
import com.mapconductor.tomtom.TomTomMapViewState
import com.mapconductor.tomtom.rememberTomTomMapViewState

@Composable
fun googleMapViewItem(initCameraPosition: MapCameraPositionInterface): IconItem<GoogleMapViewState> {
    val googleMapState =
        rememberGoogleMapViewState(
            mapDesign = GoogleMapDesign.Normal,
            cameraPosition = initCameraPosition,
        )
    return IconItem(
        key = "googlemap",
        label = "Google Map",
        lightIconResId = R.drawable.google_maps_logo,
        darkIconResId = R.drawable.google_maps_logo,
        value = googleMapState,
    )
}

@Composable
fun mapboxViewItem(initCameraPosition: MapCameraPositionInterface): IconItem<MapboxViewState> {
    val mapboxMapState =
        rememberMapboxMapViewState(
            mapDesign = MapboxMapDesign.Standard,
            cameraPosition = initCameraPosition,
        )
    return IconItem(
        key = "mapbox",
        label = "Mapbox",
        lightIconResId = R.drawable.mapbox_logo_black,
        darkIconResId = R.drawable.mapbox_logo_white,
        value = mapboxMapState,
    )
}

@Composable
fun hereViewItem(initCameraPosition: MapCameraPositionInterface): IconItem<HereViewState> {
    val hereMapState =
        rememberHereMapViewState(
            mapDesign = HereMapDesign.NormalDay,
            cameraPosition = initCameraPosition,
        )
    return IconItem(
        key = "heremap",
        label = "Here",
        lightIconResId = R.drawable.here_logo_black,
        darkIconResId = R.drawable.here_logo_white,
        value = hereMapState,
    )
}

@Composable
fun arcGISViewItem(initCameraPosition: MapCameraPositionInterface): IconItem<ArcGISMapViewState> {
    val elevationSources =
        listOf(
            "https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer",
        )
    val arcGISMapState =
        rememberArcGISMapViewState(
            mapDesign = ArcGISDesign.Streets.withElevationSources(elevationSources),
            cameraPosition = initCameraPosition,
        )
    return IconItem(
        key = "arcgis",
        label = "ArcGIS",
        lightIconResId = R.drawable.arcgis_logo_black,
        darkIconResId = R.drawable.arcgis_logo_white,
        value = arcGISMapState,
    )
}

@Composable
fun mapLibreViewItem(initCameraPosition: MapCameraPositionInterface): IconItem<MapLibreViewState> {
    val mapLibreMapState =
        rememberMapLibreMapViewState(
            mapDesign = MapLibreDesign.OsmBright,
            cameraPosition = initCameraPosition,
        )
    return IconItem(
        key = "maplibre",
        label = "MapLibre",
        lightIconResId = R.drawable.maplibre_logo,
        darkIconResId = R.drawable.maplibre_logo,
        value = mapLibreMapState,
    )
}

@Composable
fun tomtomViewItem(initCameraPosition: MapCameraPositionInterface): IconItem<TomTomMapViewState> {
    val tomtomMapState =
        rememberTomTomMapViewState(
            mapDesign = TomTomMapDesign.Standard,
            cameraPosition = initCameraPosition,
        )
    return IconItem(
        key = "tomtom",
        label = "TomTom",
        lightIconResId = R.drawable.tomtom_logo,
        darkIconResId = R.drawable.tomtom_logo,
        value = tomtomMapState,
    )
}

/**
 * ArcGIS の 2D（MapView）表示。3D（SceneView）と同じ [ArcGISMapViewState] を使うため、
 * どちらを描画するかは [LocalSelectedProviderKey] を見て [com.mapconductor.example.MapViewContainer]
 * が決める。状態は 2D/3D で別インスタンスにして、切り替え時に相互干渉しないようにする。
 */
@Composable
fun arcGIS2DViewItem(initCameraPosition: MapCameraPositionInterface): IconItem<ArcGISMapViewState> {
    val arcGIS2DMapState =
        rememberArcGISMapViewState(
            mapDesign = ArcGISDesign.Streets,
            cameraPosition = initCameraPosition,
        )
    return IconItem(
        key = "arcgis2d",
        label = "ArcGIS 2D",
        lightIconResId = R.drawable.arcgis_logo_black,
        darkIconResId = R.drawable.arcgis_logo_white,
        value = arcGIS2DMapState,
    )
}

@Composable
fun maptilerViewItem(initCameraPosition: MapCameraPositionInterface): IconItem<MapTilerViewState> {
    val maptilerMapState =
        rememberMapTilerMapViewState(
            mapDesign = MapTilerDesign.Streets,
            cameraPosition = initCameraPosition,
        )
    return IconItem(
        key = "maptiler",
        label = "MapTiler",
        lightIconResId = R.drawable.maptiler_logo,
        darkIconResId = R.drawable.maptiler_logo,
        value = maptilerMapState,
    )
}

@Composable
fun longdoViewItem(initCameraPosition: MapCameraPositionInterface): IconItem<LongdoViewState> {
    val longdoMapState =
        rememberLongdoMapViewState(
            mapDesign = LongdoDesign.Normal,
            cameraPosition = initCameraPosition,
        )
    return IconItem(
        key = "longdo",
        label = "Longdo",
        lightIconResId = R.drawable.longdo_logo,
        darkIconResId = R.drawable.longdo_logo,
        value = longdoMapState,
    )
}

@Composable
fun openMobileMapsViewItem(initCameraPosition: MapCameraPositionInterface): IconItem<OpenMobileMapsViewState> {
    val openMobileMapsState =
        rememberOpenMobileMapsMapViewState(
            mapDesign = OpenMobileMapsDesign.OpenStreetMap,
            cameraPosition = initCameraPosition,
        )
    return IconItem(
        key = "openmobilemaps",
        label = "Open Mobile Maps",
        lightIconResId = R.drawable.openmobilemaps_logo,
        darkIconResId = R.drawable.openmobilemaps_logo,
        value = openMobileMapsState,
    )
}

@Composable
fun DefaultMapViewItems(initCameraPosition: MapCameraPositionInterface): List<IconItem<out MapViewState<out Any>>> =
    listOf(
        mapLibreViewItem(initCameraPosition),
        arcGISViewItem(initCameraPosition),
        arcGIS2DViewItem(initCameraPosition),
        mapboxViewItem(initCameraPosition),
        hereViewItem(initCameraPosition),
        googleMapViewItem(initCameraPosition),
        tomtomViewItem(initCameraPosition),
        maptilerViewItem(initCameraPosition),
        longdoViewItem(initCameraPosition),
        openMobileMapsViewItem(initCameraPosition),
    )

@Composable
fun DemoMapPageScaffold(
    menuItems: List<IconItem<out MapViewState<out Any>>>,
    initSelect: Int = 0,
    onToggleSidebar: () -> Unit,
    onMapViewStateChanged: (MapViewStateInterface<*>) -> Unit = {},
    content: @Composable (BoxScope.(PaddingValues) -> Unit) = {},
) {
    // `--es provider <key>` が指定されていれば、そのプロバイダで開始する（UI テスト用）。
    // 一致しなければ [initSelect] のまま。
    val requestedIndex =
        remember(menuItems) {
            com.mapconductor.example.MainActivity.providerExtra
                ?.let { key -> menuItems.indexOfFirst { it.key.equals(key, ignoreCase = true) } }
                ?.takeIf { it >= 0 }
                ?: initSelect
        }
    var selectedIndex by rememberSaveable { mutableIntStateOf(requestedIndex) }
    LaunchedEffect(selectedIndex) {
        onMapViewStateChanged(menuItems.elementAt(selectedIndex).value)
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalSelectedProviderKey provides menuItems.elementAt(selectedIndex).key,
    ) {
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
                            .widthIn(max = 400.dp)
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
                            modifier = Modifier.wrapContentSize(),
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
}
