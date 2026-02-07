package com.mapconductor.example

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.example.navigation.NavigationViewModel
import com.mapconductor.example.pages.circle.CircleMapPage
import com.mapconductor.example.pages.groundimage.GroundImageMapPage
import com.mapconductor.example.pages.groundimage.GroundImageResources
import com.mapconductor.example.pages.heatmaplayer.HeatmapLayerPage
import com.mapconductor.example.pages.infobubble.MultipleBubblesPage
import com.mapconductor.example.pages.infobubble.RichContentBubblePage
import com.mapconductor.example.pages.infobubble.SimpleTextBubblePage
import com.mapconductor.example.pages.infobubble.StyledInfoBubblePage
import com.mapconductor.example.pages.map.basic.StoreMapPage
import com.mapconductor.example.pages.map.camerasync.CameraSyncPage
import com.mapconductor.example.pages.map.design.MapDesignMapPage
import com.mapconductor.example.pages.map.flyto.FlyToMapIcons
import com.mapconductor.example.pages.map.flyto.FlyToMapPage
import com.mapconductor.example.pages.map.visibleregion.VisibleRegionPage
import com.mapconductor.example.pages.marker.animation.AnimationMapPage
import com.mapconductor.example.pages.marker.icons.MarkerBasicPage
import com.mapconductor.example.pages.marker.postoffice.PostOfficePage
import com.mapconductor.example.pages.marker.postofficecluster.MarkerClusterMapPage as PostOfficeClusterMapPage
import com.mapconductor.example.pages.polygon.basic.PolygonMapPage
import com.mapconductor.example.pages.polygon.click.PolygonClickPage
import com.mapconductor.example.pages.polygon.geodesic.PolygonGeodesicPage
import com.mapconductor.example.pages.polyline.PolylineClickMapPage
import com.mapconductor.example.pages.polyline.PolylineMapPage
import com.mapconductor.example.pages.rasterlayer.RasterLayerMapPage
import com.mapconductor.example.pages.startup.StartUpPage
import com.mapconductor.example.ui.sidebar.Sidebar
import com.mapconductor.example.ui.sidebar.SidebarItem
import com.mapconductor.example.ui.theme.AppTheme

@Composable
fun DemoAppScreen(initPage: String = "map") {
    val navigationViewModel: NavigationViewModel = remember { NavigationViewModel(initPage) }

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
    val postOfficeIcon =
        remember {
            val baseicon = AppCompatResources.getDrawable(context, R.drawable.postoffice)!!
            ImageIcon(
                image = baseicon,
                scale = 0.5f,
            )
        }

    val sidebarItems =
        listOf(
            SidebarItem(
                id = "map-basic",
                title = "Map",
            ),
            SidebarItem(
                id = "simple-info-bubble",
                title = "Simple Text Bubble",
            ),
            SidebarItem(
                id = "styled-info-bubble",
                title = "Custom Styled Bubble",
            ),
            SidebarItem(
                id = "rich-content-info-bubble",
                title = "Rich Content Bubble",
            ),
            SidebarItem(
                id = "multiple-info-bubbles",
                title = "Multiple Bubbles",
            ),
            SidebarItem(
                id = "map-flyTo",
                title = "Move camera",
            ),
            SidebarItem(
                id = "map-design",
                title = "Map design",
            ),
            SidebarItem(
                id = "map-visibleregion",
                title = "VisibleRegion",
            ),
            SidebarItem(
                id = "map-camerasync",
                title = "Camera Sync",
            ),
            SidebarItem(
                id = "marker-basic",
                title = "Marker",
            ),
            SidebarItem(
                id = "marker-animation",
                title = "Marker animation ",
            ),
            SidebarItem(
                id = "marker-postoffice",
                title = "Bunch of Markers",
            ),
            SidebarItem(
                id = "marker-postoffice-cluster",
                title = "Marker Clustering",
            ),
            SidebarItem(
                id = "circle",
                title = "Circle ",
            ),
            SidebarItem(
                id = "groundImage",
                title = "GroundImage",
            ),
            SidebarItem(
                id = "polyline",
                title = "Polyline ",
            ),
            SidebarItem(
                id = "polyline-click",
                title = "Polyline Click",
            ),
            SidebarItem(
                id = "polygon",
                title = "Polygon ",
            ),
            SidebarItem(
                id = "polygon-click",
                title = "Polygon Click",
            ),
            SidebarItem(
                id = "polygon-geodesic",
                title = "Geodesic polygons",
            ),
            SidebarItem(
                id = "raster-layer",
                title = "Raster Layer",
            ),
            SidebarItem(
                id = "heatmap-overlay",
                title = "Heatmap overlay",
            ),
        )

    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentPage) {
                    "startup" -> {
                        StartUpPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "simple-info-bubble" -> {
                        SimpleTextBubblePage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "styled-info-bubble" -> {
                        StyledInfoBubblePage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "rich-content-info-bubble" -> {
                        RichContentBubblePage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "multiple-info-bubbles" -> {
                        MultipleBubblesPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "map-basic" -> {
                        StoreMapPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "map-design" -> {
                        MapDesignMapPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "map-visibleregion" -> {
                        VisibleRegionPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
//                        ZoomCalibrationPage(
//                            onToggleSidebar = navigationViewModel::toggleSidebar,
//                        )
                    }
                    "map-camerasync" -> {
                        CameraSyncPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "map-flyTo" -> {
                        FlyToMapPage(
                            icons = flyToMapPageIcons,
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "marker-basic" -> {
                        MarkerBasicPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "marker-animation" -> {
                        AnimationMapPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "marker-postoffice" -> {
                        PostOfficePage(
                            postOfficeIcon = postOfficeIcon,
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "marker-postoffice-cluster" -> {
                        PostOfficeClusterMapPage(
                            postOfficeIcon = postOfficeIcon,
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
                    "polyline-click" -> {
                        PolylineClickMapPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "polygon" -> {
                        PolygonMapPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "polygon-click" -> {
                        PolygonClickPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "polygon-geodesic" -> {
                        PolygonGeodesicPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "groundImage" -> {
                        GroundImageMapPage(
                            groundImageResources = groundImageResources,
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "raster-layer" -> {
                        RasterLayerMapPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
                    }
                    "heatmap-overlay" -> {
                        HeatmapLayerPage(
                            onToggleSidebar = navigationViewModel::toggleSidebar,
                        )
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
