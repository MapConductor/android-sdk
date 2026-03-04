package com.mapconductor.example.pages.groundimage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.example.toast.ToastHost
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GroundImageMapPage(
    groundImageResources: GroundImageResources,
    onToggleSidebar: () -> Unit = {},
) {
    val viewModel = remember { GroundImageMapPageViewModel(groundImageResources) }
    val coroutineScope = rememberCoroutineScope()
    var sliderOpacity by remember { mutableStateOf(viewModel.opacity) }
    var opacityJob by remember { mutableStateOf<Job?>(null) }
    val debounceMs = 80L

    DemoMapPageScaffold(
        menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        GroundImageMapComponent(
            mapViewState = mapViewState.value,
            viewModel = viewModel,
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
            title = "GroundImage Example",
        ) {
            Column {
                Text("opacity: ${"%.2f".format(sliderOpacity)}", color = Color.Black)
                Slider(
                    value = sliderOpacity,
                    onValueChange = { newValue ->
                        sliderOpacity = newValue
                        opacityJob?.cancel()
                        opacityJob =
                            coroutineScope.launch {
                                delay(debounceMs)
                                viewModel.opacity = sliderOpacity
                            }
                    },
                    onValueChangeFinished = {
                        opacityJob?.cancel()
                        opacityJob =
                            coroutineScope.launch {
                                delay(debounceMs)
                                viewModel.opacity = sliderOpacity
                            }
                    },
                    valueRange = 0.0f..1.0f, // スライダー範囲
                    steps = 0,
                    colors =
                        SliderDefaults.colors(
                            thumbColor = Color.Black, // つまみの色
                            activeTrackColor = Color.DarkGray, // 値までのトラック
                            inactiveTrackColor = Color.LightGray, // 残りのトラック
                            activeTickColor = Color.Black, // 有効ステップの目盛り
                            inactiveTickColor = Color.White, // 無効ステップの目盛り
                        ),
                )
            }
        }

        ToastHost(
            messages = viewModel.messages.collectAsState().value,
            onDismiss = { viewModel.removeToast(it) },
        )
    }
}
