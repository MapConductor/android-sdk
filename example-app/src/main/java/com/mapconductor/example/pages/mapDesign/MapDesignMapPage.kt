package com.mapconductor.example.pages.mapDesign

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.core.map.MapDesignType
import com.mapconductor.core.map.MapViewState
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapDesign
import com.mapconductor.here.HereMapViewState
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.MapboxMapViewState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.TextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDesignMapPage(
    onToggleSidebar: () -> Unit = {},
) {
    val viewModel: MapDesignPageViewModel =
        viewModel<MapDesignPageViewModelImpl>(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(MapDesignPageViewModelImpl::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return MapDesignPageViewModelImpl() as T
                        }
                        throw IllegalArgumentException("Unknown VieModel class")
                    }
                }
        )

    val optionsState = viewModel.options.collectAsState()

    DemoMapPageScaffold(
        initCameraPosition = viewModel.initCameraPosition,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        fun callChangeMapDesignType(state: MapViewState<*>,designType: MapDesignType<*>) {
            when (state) {
                is GoogleMapViewState -> (designType as? GoogleMapDesign)?.let { state.changeMapDesignType(it) }
                is HereMapViewState   -> (designType as? HereMapDesign  )?.let { state.changeMapDesignType(it) }
                is MapboxMapViewState -> (designType as? MapboxMapDesign)?.let { state.changeMapDesignType(it) }
                is ArcGISMapViewState -> (designType as? ArcGISDesign   )?.let { state.changeMapDesignType(it) }
            }
        }

        MapDesignMapComponent(
            mapViewState = mapViewState.value,
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
            var expanded by remember { mutableStateOf(false) }
            val items = optionsState.value

            // SDKキー（Google/Here/Mapbox/ArcGISで切替）
            val sdkKey = when (mapViewState.value) {
                is GoogleMapViewState -> "google"
                is HereMapViewState   -> "here"
                is MapboxMapViewState -> "mapbox"
                is ArcGISMapViewState -> "arcgis"
                else -> "none"
            }

            var selectedLabel by rememberSaveable(sdkKey) {  // SDKごとに独立して保存
                mutableStateOf(items.firstOrNull()?.label ?: "")
            }

            // ★ options（候補リスト）が変わったら選択と展開状態をリセット
            LaunchedEffect(items) {
                selectedLabel = items.firstOrNull()?.label ?: ""
                expanded = false
            }

            val enabled = mapViewState.value != null && items.isNotEmpty()

            key(sdkKey) { // ★ SDK切替でサブツリーごと作り直し
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { if (enabled) expanded = !expanded }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        value = selectedLabel,
                        onValueChange = {},
                        readOnly = true,
                        enabled = enabled,
                        label = { Text("Map design") },
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        items.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.label) },
                                onClick = {
                                    selectedLabel = item.label
                                    expanded = false
                                    mapViewState.value?.let { state ->
                                        callChangeMapDesignType(state, item.design)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
