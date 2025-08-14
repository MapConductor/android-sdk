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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.example.toast.ToastHost
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun GroundImageMapPage(
    groundImageResources: GroundImageResources,
    onToggleSidebar: () -> Unit = {},
) {
    val viewModel: GroundImageMapPageViewModel =
        viewModel<GroundImageMapPageViewModelImpl>(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(GroundImageMapPageViewModelImpl::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return GroundImageMapPageViewModelImpl(groundImageResources) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                },
        )

    DemoMapPageScaffold(
        initCameraPosition = viewModel.initCameraPosition,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        GroundImageMapComponent(
            mapViewState = mapViewState.value,
            viewModel = viewModel,
            onGroundImageClick = viewModel::onGroundImageClick,
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
            title = "Messages",
        ) {
            Column {
                Text("opacity: ${"%.2f".format(viewModel.opacity)}", color = Color.Black)
                Slider(
                    value = viewModel.opacity,
                    onValueChange = { newValue ->
                        viewModel.opacity = newValue
                    },
                    valueRange = 0.0f..1.0f, // スライダー範囲
                    steps = 0, // 中間ステップ数（範囲内を1000分割）
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Black,               // つまみの色
                        activeTrackColor = Color.DarkGray,      // 値までのトラック
                        inactiveTrackColor = Color.LightGray,   // 残りのトラック
                        activeTickColor = Color.Black,          // 有効ステップの目盛り
                        inactiveTickColor = Color.White,        // 無効ステップの目盛り
                    )
                )
            }
        }

        ToastHost(
            messages = viewModel.messages.collectAsState().value,
            onDismiss = { viewModel.removeToast(it) },
        )
    }
}
