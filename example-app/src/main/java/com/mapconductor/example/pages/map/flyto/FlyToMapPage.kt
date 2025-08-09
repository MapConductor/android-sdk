package com.mapconductor.example.pages.map.flyto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import android.graphics.drawable.Drawable

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
            onMapClick = viewModel::onMapClick,
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
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.flyToHonolulu() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Honolulu")
                    }

                    Button(
                        onClick = { viewModel.flyToTokyo() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tokyo")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.flyToLondon() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("London")
                    }

                    Button(
                        onClick = { viewModel.flyToNewYork() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("New York")
                    }
                }
            }
        }
    }
}
