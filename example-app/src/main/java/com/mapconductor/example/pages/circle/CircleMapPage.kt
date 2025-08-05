package com.mapconductor.example.pages.circle

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mapconductor.example.R
import com.mapconductor.example.toast.ToastHost
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard

@Composable
fun CircleMapPage(
    viewModel: CirclePageViewModel = CirclePageViewModelImpl(),
    onToggleSidebar: () -> Unit = {},
) {
    DemoMapPageScaffold(
        initCameraPosition = viewModel.initCameraPosition,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        CircleMapComponent(
            mapViewState = mapViewState.value,
            viewModel = viewModel,
            onMapClickHandler = viewModel::onMapClick,
            onMarkerClickHandler = viewModel::onMarkerClick,
            onCircleClickHandler = viewModel::onCircleClick,
            onMarkerDrag = viewModel::onMarkerDrag,
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
            Text(
                text = stringResource(R.string.circle_example_description),
                modifier = Modifier.fillMaxSize(),
            )
        }

        ToastHost(
            messages = viewModel.messages.collectAsState().value,
            onDismiss = { viewModel.removeToast(it) },
        )
    }
}
