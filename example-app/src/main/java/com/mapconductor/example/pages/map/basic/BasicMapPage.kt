package com.mapconductor.example.pages.map.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.polyline.Polyline
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.example.ui.MessageCard
import com.mapconductor.openmobilemaps.OpenMobileMapDesign
import com.mapconductor.openmobilemaps.OpenMobileMapView
import com.mapconductor.openmobilemaps.OpenMobileMapViewState

@Composable
fun BasicMapPage(viewModel: BasicMapPageViewModel = viewModel<BasicMapPageViewModelImpl>()) {
    val initCameraPosition = MapCameraPosition(
        position = GeoPoint.fromLatLong(0.0, 0.0),
    )
    val state = remember {
        OpenMobileMapViewState(
            id = "test",
            initCameraPosition = initCameraPosition,
            mapDesignType = OpenMobileMapDesign.OpenStreetMap,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OpenMobileMapView(state) {

            viewModel.polylines.forEach { polyline ->
                key(polyline.id) {
                    Polyline(polyline)
                }
            }
        }

        MessageCard(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 56.dp,
                    ),
            title = "Test",
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = viewModel.geodesic,
                    onCheckedChange = {
                        viewModel.geodesic = !viewModel.geodesic
                    },
                    thumbContent =
                        if (viewModel.geodesic) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        },
                )
                Text(
                    text = "geodesic",
                    modifier =
                        Modifier
                            .align(Alignment.CenterVertically)
                            .padding(16.dp),
                )
            }
        }
    }
}
