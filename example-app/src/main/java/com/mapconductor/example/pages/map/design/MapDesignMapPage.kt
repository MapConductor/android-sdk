package com.mapconductor.example.pages.map.design

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.arcgis.ArcGISDesignTypeInterface
import com.mapconductor.arcgis.ArcGISMapViewStateInterface
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import com.mapconductor.googlemaps.GoogleMapDesignType
import com.mapconductor.googlemaps.GoogleMapViewStateInterface
import com.mapconductor.here.HereMapDesignType
import com.mapconductor.here.HereViewStateInterface
import com.mapconductor.mapbox.MapboxDesignType
import com.mapconductor.mapbox.MapboxViewStateInterface
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreViewStateInterface
import com.mapconductor.tomtom.TomTomMapDesignType
import com.mapconductor.tomtom.TomTomMapViewStateInterface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDesignMapPage(onToggleSidebar: () -> Unit = {}) {
    val viewModel = remember { MapDesignPageViewModel() }

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()
        val mapDesignOptions = viewModel.mapDesignOptions.collectAsState()

        MapDesignMapComponent(
            mapViewState = mapViewState.value,
        )

        mapViewState.value?.let { state ->
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
                key(state) {
                    MapDesignTypeSelector(
                        state = state,
                        mapDesignOptions = mapDesignOptions.value,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDesignTypeSelector(
    state: MapViewStateInterface<*>,
    mapDesignOptions: List<MapDesignOption>,
) {
    var expanded by remember { mutableStateOf(false) }

    var selectedLabel by rememberSaveable(state.id) {
        mutableStateOf(mapDesignOptions.firstOrNull()?.label ?: "")
    }

    val onClick: (MapDesignOption) -> Unit = fun (mapDesignOption: MapDesignOption) {
        selectedLabel = mapDesignOption.label
        expanded = false
        when (state) {
            is GoogleMapViewStateInterface -> {
                @Suppress("UNCHECKED_CAST")
                state.mapDesignType = mapDesignOption.design as GoogleMapDesignType
            }
            is HereViewStateInterface -> {
                @Suppress("UNCHECKED_CAST")
                state.mapDesignType = mapDesignOption.design as HereMapDesignType
            }
            is ArcGISMapViewStateInterface -> {
                @Suppress("UNCHECKED_CAST")
                state.mapDesignType = mapDesignOption.design as ArcGISDesignTypeInterface
            }
            is MapboxViewStateInterface -> {
                @Suppress("UNCHECKED_CAST")
                state.mapDesignType = mapDesignOption.design as MapboxDesignType
            }
            is MapLibreViewStateInterface -> {
                @Suppress("UNCHECKED_CAST")
                state.mapDesignType = mapDesignOption.design as MapLibreDesign
            }
            is TomTomMapViewStateInterface -> {
                @Suppress("UNCHECKED_CAST")
                state.mapDesignType = mapDesignOption.design as TomTomMapDesignType
            }
            else -> throw IllegalArgumentException("Not implemented yet")
        }
    }

    key(state.id) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Map design") },
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                mapDesignOptions.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.label) },
                        onClick = {
                            onClick(item)
                        },
                    )
                }
            }
        }
    }
}
