package com.mapconductor.example.pages.map.arcgis2d

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapconductor.arcgis.map.ArcGISDesign
import com.mapconductor.arcgis.map.ArcGISMapView2D
import com.mapconductor.arcgis.map.ArcGISMapViewState
import com.mapconductor.arcgis.map.rememberArcGISMapViewState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.example.R
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.IconItem
import com.mapconductor.example.ui.MessageCard

@Composable
fun ArcGISMapView2DPage(onToggleSidebar: () -> Unit = {}) {
    val initCameraPosition = remember {
        MapCameraPosition(
            position = GeoPoint(35.68162987878426, 139.76703394012318),
            zoom = 0.0,
            bearing = 127.0,
        )
    }

    val arcGISState = rememberArcGISMapViewState(
        mapDesign = ArcGISDesign.Streets,
        cameraPosition = initCameraPosition,
    )

    var mapState by remember { mutableStateOf<ArcGISMapViewState?>(null) }

    val menuItems = remember(arcGISState) {
        listOf(
            IconItem(
                key = "arcgis2d",
                label = "ArcGIS 2D",
                lightIconResId = R.drawable.arcgis_logo_black,
                darkIconResId = R.drawable.arcgis_logo_white,
                value = arcGISState,
            ),
        )
    }

    DemoMapPageScaffold(
        menuItems = menuItems,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = { state ->
            mapState = state as? ArcGISMapViewState
        },
    ) { paddingValues ->
        mapState?.let { state ->
            ArcGISMapView2D(
                state = state,
                modifier = Modifier.fillMaxSize(),
            )
        }

        MessageCard(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                    )
                    .sizeIn(maxWidth = 600.dp),
            title = "ArcGIS MapView 2D",
        ) {
            Text("Flat 2D map view using ArcGIS MapView (no 3D tilt or elevation).")
        }
    }
}
