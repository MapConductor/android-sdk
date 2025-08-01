package com.mapconductor.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.toFixed
import com.mapconductor.example.navigation.NavigationViewModel
import com.mapconductor.example.pages.MapExamplePage
import com.mapconductor.example.pages.circle.CircleMapPage
import com.mapconductor.example.pages.circle.CirclePageViewModelImpl
import com.mapconductor.example.pages.stores.StoreMapPage
import com.mapconductor.example.pages.stores.StoreMapPageViewModel
import com.mapconductor.example.ui.sidebar.Sidebar
import com.mapconductor.example.ui.sidebar.SidebarItem
import com.mapconductor.example.ui.theme.AppTheme

@Composable
fun DemoAppScreen(
    appViewModel: AppViewModel,
    storeMapPageViewModel: StoreMapPageViewModel,
    circlePageViewModel: CirclePageViewModelImpl = CirclePageViewModelImpl(),
) {
    val navigationViewModel: NavigationViewModel = viewModel()
    val currentPage by navigationViewModel.currentPage
    val isSidebarExpanded by navigationViewModel.isSidebarExpanded

    val sidebarItems = listOf(
        SidebarItem(
            id = "map",
            title = "Map Demo",
            icon = Icons.Default.Home,
            route = "map"
        ),
        SidebarItem(
            id = "circle",
            title = "Circle Demo",
            icon = Icons.Default.CheckCircle,
            route = "circle"
        ),
        SidebarItem(
            id = "examples",
            title = "Map Examples",
            icon = Icons.Default.LocationOn,
            route = "examples"
        ),
        SidebarItem(
            id = "settings",
            title = "Settings",
            icon = Icons.Default.Settings,
            route = "settings"
        )
    )

    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentPage) {
                    "map" -> {
                        StoreMapPage(
                            viewModel = storeMapPageViewModel,
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "circle" -> {
                        CircleMapPage(
                            viewModel = circlePageViewModel,
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "examples" -> MapExamplePage(appViewModel)
                    "settings" -> {
                        // Placeholder for settings page
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Settings page coming soon...")
                        }
                    }
                }
            }

            // Overlay sidebar
            Sidebar(
                items = sidebarItems,
                selectedItemId = currentPage,
                onItemClick = { item ->
                    navigationViewModel.navigateTo(item.id)
                },
                isExpanded = isSidebarExpanded,
                onToggleSidebar = navigationViewModel::toggleSidebar
            )
        }
    }
}

@Composable
fun BoxScope.DebugPanel(camera: MapCameraPosition?) {
    Column(
        modifier =
            Modifier
                .align(Alignment.TopEnd)
                .background(
                    Color(
                        red = 0.9f,
                        green = 0.9f,
                        blue = 0.9f,
                        alpha = 0.75f,
                    ),
                ).wrapContentHeight()
                .fillMaxWidth(),
    ) {
        Text("LatLng: (${camera?.position?.toUrlValue()})", color = Color.Black)
        Text("Zoom: ${camera?.zoom?.toFixed(2)}", color = Color.Black)
        Text("bearing: ${camera?.bearing?.toInt()}", color = Color.Black)
        Text("tilt: ${camera?.tilt?.toInt()}", color = Color.Black)

//        Canvas(
//            modifier = Modifier.size(34.dp)
//                .padding(100.dp)
//        ) {
//            drawRect(
//                color = Color.Red,
//                size = Size(34.dp.toPx(), 34.dp.toPx()),
//                style = DrawStyle.Stroke,
//            )
//        }
    }
}
