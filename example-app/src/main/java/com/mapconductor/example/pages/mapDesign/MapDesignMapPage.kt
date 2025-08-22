package com.mapconductor.example.pages.mapDesign

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import androidx.compose.material3.Button
import com.mapbox.maps.extension.style.expressions.dsl.generated.mod
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.core.map.MapDesignType
import com.mapconductor.core.map.MapViewState
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.GoogleMapDesignType
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapDesign
import com.mapconductor.here.HereMapDesignType
import com.mapconductor.here.HereMapViewState
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.MapboxMapViewState

data class DesignList<T>(
    val text: String,
    val design: MapDesignType<T>
)

@Composable
fun MapDesignMapPage(
    viewModel: MapDesignPageViewModel = MapDesignPageViewModelImpl(),
    onToggleSidebar: () -> Unit = {},
) {
    val buttons = viewModel.buttons.collectAsState()

    DemoMapPageScaffold(
        initCameraPosition = viewModel.initCameraPosition,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        fun callChangeMapDesignType(state: MapViewState<*>,designType: MapDesignType<*>) {
            when (state) {
                is GoogleMapViewState -> {
                    (designType as? GoogleMapDesign)?.let {
                        state.changeMapDesignType(it)
                    }
                }
//                is HereMapViewState -> {
//                    (designType as? MapViewState<HereMapDesign>)?.let {
//                        state.changeMapDesignType(it)
//                    }
//                }
//                is MapboxMapViewState -> {
//                    (designType as? MapViewState<MapboxMapDesign>)?.let {
//                        state.changeMapDesignType(it)
//                    }
//                }
//                is ArcGISMapViewState -> {
//                    (designType as? MapViewState<ArcGISDesign>)?.let {
//                        state.changeMapDesignType(it)
//                    }
//                }
            }
        }

        MapDesignMapComponent(
            mapViewState = mapViewState.value,
            onMapClick = viewModel::onMapClick,
        )

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
            title = "Select Map Design...",
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row {
                    buttons.value.forEach {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                callChangeMapDesignType(mapViewState.value!!, it.designType)
                            }
                        ){
                            Text(it.label)
                        }
                    }
                }
            }
//            Column(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(8.dp),
//            ) {
//                when (mapViewState) {
//                    is GoogleMapViewState -> {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        ) {
//                            Button(
//                                modifier = Modifier.weight(1f),
//                                onClick = {
//                                    (viewModel.mapViewState.value as? MapViewState<GoogleMapDesign>)
//                                        ?.changeMapDesignType(GoogleMapDesign.Normal)
//                                },
//                            ) {
//                                Text("Normal")
//                            }
//                            Button(
//                                modifier = Modifier.weight(1f),
//                                onClick = {
//                                    (viewModel.mapViewState.value as? MapViewState<GoogleMapDesign>)
//                                        ?.changeMapDesignType(GoogleMapDesign.Satellite)
//                                },
//                            ) {
//                                Text("Satellite")
//                            }
//                            Button(
//                                modifier = Modifier.weight(1f),
//                                onClick = {
//                                    (viewModel.mapViewState.value as? MapViewState<GoogleMapDesign>)
//                                        ?.changeMapDesignType(GoogleMapDesign.Hybrid)
//                                },
//                            ) {
//                                Text("Hybrid")
//                            }
//                        }
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        ) {
//                            Button(
//                                modifier = Modifier.weight(1f),
//                                onClick = {
//                                    (viewModel.mapViewState.value as? MapViewState<GoogleMapDesign>)
//                                        ?.changeMapDesignType(GoogleMapDesign.Terrain)
//                                },
//                            ) {
//                                Text("Terrain")
//                            }
//                            Button(
//                                modifier = Modifier.weight(1f),
//                                onClick = {
//                                    (viewModel.mapViewState.value as? MapViewState<GoogleMapDesign>)
//                                        ?.changeMapDesignType(GoogleMapDesign.None)
//                                },
//                            ) {
//                                Text("None")
//                            }
//                        }
//                    }
//
//                    is HereMapViewState -> {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        ) {
//                            Button(
//                                modifier = Modifier.weight(1f),
//                                onClick = {
//                                    (viewModel.mapViewState.value as? MapViewState<HereMapDesign>)
//                                        ?.changeMapDesignType(HereMapDesign.NormalDay)
//                                },
//                            ) {
//                                Text("NormalDay")
//                            }
//                            Button(
//                                modifier = Modifier.weight(1f),
//                                onClick = {
//                                    (viewModel.mapViewState.value as? MapViewState<HereMapDesign>)
//                                        ?.changeMapDesignType(HereMapDesign.NormalNigh)
//                                },
//                            ) {
//                                Text("NormalNigh")
//                            }
//                            Button(
//                                modifier = Modifier.weight(1f),
//                                onClick = {
//                                    (viewModel.mapViewState.value as? MapViewState<HereMapDesign>)
//                                        ?.changeMapDesignType(HereMapDesign.Satellite)
//                                },
//                            ) {
//                                Text("Satellite")
//                            }
//                        }
//                    }
//                }
//            }
        }
    }
}
