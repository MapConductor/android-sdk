package com.mapconductor.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.example.navigation.NavigationViewModel
import com.mapconductor.example.pages.circle.CircleMapPage
import com.mapconductor.example.pages.groundimage.GroundImageMapPage
import com.mapconductor.example.pages.groundimage.GroundImageResources
import com.mapconductor.example.pages.map.flyto.FlyToMapIcons
import com.mapconductor.example.pages.map.flyto.FlyToMapPage
import com.mapconductor.example.pages.polyline.PolylineMapPage
import com.mapconductor.example.pages.stores.StoreMapPage
import com.mapconductor.example.ui.sidebar.Sidebar
import com.mapconductor.example.ui.sidebar.SidebarItem
import com.mapconductor.example.ui.theme.AppTheme

@Composable
fun DemoAppScreen() {
    val navigationViewModel: NavigationViewModel = viewModel()
    val currentPage by navigationViewModel.currentPage
    val isSidebarExpanded by navigationViewModel.isSidebarExpanded
    val context = LocalContext.current
    val flyToMapPageIcons =
        remember {
            FlyToMapIcons(
                honolulu = ContextCompat.getDrawable(context, R.drawable.honolulu)!!,
                tokyo = ContextCompat.getDrawable(context, R.drawable.tokyo)!!,
                london = ContextCompat.getDrawable(context, R.drawable.london)!!,
                newYork = ContextCompat.getDrawable(context, R.drawable.newyork)!!,
                sydney = ContextCompat.getDrawable(context, R.drawable.sydney)!!,
            )
        }

    val groundImageResources =
        remember {
            GroundImageResources(
                image = ContextCompat.getDrawable(context, R.drawable.newark_nj_1922_0)!!,
                clickedImage = ContextCompat.getDrawable(context, R.drawable.newark_nj_1922_1)!!,
            )
        }

    val sidebarItems =
        listOf(
            SidebarItem(
                id = "map",
                title = "Map Demo",
                icon = Icons.Default.Home,
                route = "map",
            ),
            SidebarItem(
                id = "flyTo",
                title = "Move camera",
                icon = Icons.Default.PlayArrow,
                route = "flyTo",
            ),
            SidebarItem(
                id = "circle",
                title = "Circle ",
                icon = Icons.Default.CheckCircle,
                route = "circle",
            ),
            SidebarItem(
                id = "groundImage",
                title = "GroundImage",
                icon = Icons.Default.Favorite,
                route = "groundImage",
            ),
            SidebarItem(
                id = "polyline",
                title = "Polyline ",
                icon = Icons.Default.PlayArrow,
                route = "polyline",
            ),
//            SidebarItem(
//                id = "examples",
//                title = "Map Examples",
//                icon = Icons.Default.LocationOn,
//                route = "examples",
//            ),
//            SidebarItem(
//                id = "settings",
//                title = "Settings",
//                icon = Icons.Default.Settings,
//                route = "settings",
//            ),
        )

    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentPage) {
                    "map" -> {
                        StoreMapPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "circle" -> {
                        CircleMapPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "polyline" -> {
                        PolylineMapPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "flyTo" -> {
                        FlyToMapPage(
                            icons = flyToMapPageIcons,
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "groundImage" -> {
                        GroundImageMapPage(
                            groundImageResources = groundImageResources,
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "settings" -> {
                        // Placeholder for settings page
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
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
                onToggleSidebar = navigationViewModel::toggleSidebar,
            )
        }
    }
}
