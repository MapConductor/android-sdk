package com.mapconductor.example.pages.mapDesign

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

data class DesignList<T>(
    val text: String,
    val design: MapDesignType<T>
)

@OptIn(ExperimentalMaterial3Api::class)
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
                is GoogleMapViewState -> (designType as? GoogleMapDesign)?.let { state.changeMapDesignType(it) }
                is HereMapViewState   -> (designType as? HereMapDesign  )?.let { state.changeMapDesignType(it) }
                is MapboxMapViewState -> (designType as? MapboxMapDesign)?.let { state.changeMapDesignType(it) }
                is ArcGISMapViewState -> (designType as? ArcGISDesign   )?.let { state.changeMapDesignType(it) }
//                is GoogleMapViewState -> {
//                    (designType as? GoogleMapDesign)?.let {
//                        state.changeMapDesignType(it)
//                    }
//                }
//                is HereMapViewState -> {
//                    (designType as? HereMapDesign)?.let {
//                        state.changeMapDesignType(it)
//                    }
//                }
//                is MapboxMapViewState -> {
//                    (designType as? MapboxMapDesign)?.let {
//                        state.changeMapDesignType(it)
//                    }
//                }
//                is ArcGISMapViewState -> {
//                    (designType as? ArcGISDesign)?.let {
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
            // --- ここからドロップダウン ---
            var expanded by remember { mutableStateOf(false) }
            val items = buttons.value
            var selectedLabel by rememberSaveable { mutableStateOf(items.firstOrNull()?.label ?: "") }

            // mapViewState が null の間は選択させない
            val enabled = mapViewState.value != null && items.isNotEmpty()

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (enabled) expanded = !expanded }
            ) {
                TextField(
                    modifier = Modifier.menuAnchor(), // ExposedDropdownMenuBoxScope
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
                                    callChangeMapDesignType(state, item.designType)
                                }
                            }
                        )
                    }
                }
            }
            // --- ここまでドロップダウン ---
//            Column(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(8.dp),
//            ) {
//                Row {
//                    buttons.value.forEach {
//                        Button(
//                            modifier = Modifier.weight(1f),
//                            onClick = {
//                                callChangeMapDesignType(mapViewState.value!!, it.designType)
//                            }
//                        ){
//                            Text(it.label)
//                        }
//                    }
//                }
//            }
        }
    }
}
