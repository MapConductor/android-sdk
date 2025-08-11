package com.mapconductor.example.pages.map.flyto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun FlyToMapPage(
    icons: FlyToMapIcons,
    viewModel: FlyToPageViewModel = FlyToPageViewModelImpl(icons),
    onToggleSidebar: () -> Unit = {},
) {
    DemoMapPageScaffold(
        initCameraPosition = viewModel.initCameraPosition,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        FlyToMapComponent(
            mapViewState = mapViewState.value,
            polylines = viewModel.polylines,
            markers = viewModel.markers,
        )

        // Control Panel
        MessageCard(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    ),
            title = "Fly To Controls",
            maxHeight = 200.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
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
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.flyToSydney() },
                    ) {
                        Text("Sydney")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { viewModel.flyToHonolulu() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Honolulu")
                    }

                    Button(
                        onClick = { viewModel.flyToTokyo() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Tokyo")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { viewModel.flyToLondon() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("London")
                    }

                    Button(
                        onClick = { viewModel.flyToNewYork() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("New York")
                    }
                }
            }
        }
    }
}
