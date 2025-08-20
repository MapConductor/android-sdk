package com.mapconductor.example.pages.mapDesign

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.mapconductor.example.ui.DemoMapPageScaffold

@Composable
fun MapDesignMapPage(
    viewModel: MapDesignPageViewModel = MapDesignPageViewModelImpl(),
    onToggleSidebar: () -> Unit = {},
) {
    DemoMapPageScaffold(
        initCameraPosition = viewModel.initCameraPosition,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        MapDesignMapComponent(
            mapViewState = mapViewState.value,
            onMapClick = viewModel::onMapClick,
        )

//        // Control Panel
//        MessageCard(
//            modifier =
//                Modifier
//                    .align(Alignment.BottomStart)
//                    .padding(
//                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
//                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
//                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
//                    ),
//            title = "Select Map Design...",
//            maxHeight = 400.dp,
//        ) {
//            Column(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(8.dp),
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                ) {
//                    Button(
//                        modifier = Modifier.weight(1f),
//                        onClick = { viewModel.flyToSydney() },
//                    ) {
//                        Text("Normal")
//                    }
//
//                    Button(
//                        modifier = Modifier.weight(1f),
//                        onClick = { viewModel.flyToSydney() },
//                    ) {
//                        Text("Satellite")
//                    }
//                }
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                ) {
//                    Button(
//                        modifier = Modifier.weight(1f),
//                        onClick = { viewModel.flyToHonolulu() },
//                    ) {
//                        Text("Hybrid")
//                    }
//
//                    Button(
//                        modifier = Modifier.weight(1f),
//                        onClick = { viewModel.flyToTokyo() },
//                    ) {
//                        Text("Terrain")
//                    }
//                }
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                ) {
//                    Button(
//                        modifier = Modifier.weight(1f),
//                        onClick = { viewModel.flyToLondon() },
//                    ) {
//                        Text("None")
//                    }
//                }
//            }
//        }
    }
}
