package com.mapconductor.example.pages.groundimage

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mapconductor.core.groundimage.GroundImageClickEvent
import com.mapconductor.example.R
import com.mapconductor.example.pages.circle.CircleMapComponent
import com.mapconductor.example.pages.circle.CirclePageViewModel
import com.mapconductor.example.pages.circle.CirclePageViewModelImpl
import com.mapconductor.example.toast.ToastHost
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MapViewStatePanel
import com.mapconductor.example.ui.MessageCard
import android.content.Context
import android.graphics.drawable.Drawable

@Composable
fun GroundImageMapPage(
    context: Context = LocalContext.current,
    image: Drawable = ContextCompat.getDrawable(context, R.drawable.newark_nj_1922_0)!!,
    viewModel: GroundImageMapPageViewModel = GroundImageMapPageViewModelImpl(image),
    onToggleSidebar: () -> Unit = {},
) {
    DemoMapPageScaffold(
        initCameraPosition = viewModel.initCameraPosition,
        onToggleSidebar = onToggleSidebar,
        onMapViewStateChanged = viewModel::onMapViewChanged,
    ) { paddingValues ->
        val mapViewState = viewModel.mapViewState.collectAsState()

        GroundImageMapComponent(
            mapViewState = mapViewState.value,
            viewModel = viewModel,
            onMapClick = viewModel::onMapClick,
            onGroundImageClick = viewModel::onGroundImageClick,
            onGroundImageChange = viewModel::onGroundImageChange,
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
            MapViewStatePanel(
                mapViewState.value
                    ?.mapCameraPosition
                    ?.collectAsState()
                    ?.value,
            )
        }

        ToastHost(
            messages = viewModel.messages.collectAsState().value,
            onDismiss = { viewModel.removeToast(it) },
        )
    }
}
