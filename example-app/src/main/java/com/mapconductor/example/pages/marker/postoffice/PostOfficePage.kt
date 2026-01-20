package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.IconSelectMenu
import com.mapconductor.postoffice.PostOffice
import com.mapconductor.postoffice.PostOfficeDataLoader
import com.mapconductor.utils.LoadingDialog

@Composable
fun PostOfficePage(
    postOfficeIcon: ImageIcon,
    onToggleSidebar: () -> Unit = {},
) {
    val context = LocalContext.current
    val dataLoader = remember { PostOfficeDataLoader(context) }

    val viewModel: PostOfficeViewModel =
        viewModel<PostOfficeViewModel>(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(PostOfficeViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return PostOfficeViewModel(
                                postOfficeIcon = postOfficeIcon,
                                dataLoader = dataLoader,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                },
        )

    val leftMenuItems = DefaultMapViewItems(viewModel.initCameraPosition)
    val rightMenuItems = DefaultMapViewItems(viewModel.initCameraPosition)

    var leftSelectedIndex by rememberSaveable { mutableIntStateOf(0) } // Mapbox
    var rightSelectedIndex by rememberSaveable { mutableIntStateOf(3) } // MapLibre

    @Suppress("UNCHECKED_CAST")
    val leftState = leftMenuItems[leftSelectedIndex].value as MapViewStateInterface<*>
    @Suppress("UNCHECKED_CAST")
    val rightState = rightMenuItems[rightSelectedIndex].value as MapViewStateInterface<*>

    var leftCameraPosition by remember { mutableStateOf(viewModel.initCameraPosition) }

    val selectedMarker = viewModel.selectedMarker.collectAsState().value
    val markers = viewModel.markerList.collectAsState().value
    val isMapLoaded = viewModel.isMapLoaded.collectAsState().value
    val isDataLoading = viewModel.isDataLoading.collectAsState().value

    var leftMapLoaded by remember { mutableStateOf(false) }
    var rightMapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPostOfficeData()
    }

    // Notify viewModel when left state changes
    LaunchedEffect(leftState) {
        viewModel.onMapViewChanged(leftState)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with SDK selectors
            Card(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Toggle sidebar",
                        modifier =
                            Modifier
                                .clickable(onClick = onToggleSidebar)
                                .size(32.dp)
                                .padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Left (Source)",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconSelectMenu(
                            itemList = leftMenuItems,
                            selectedIndex = leftSelectedIndex,
                            onSelect = { index, _ ->
                                leftSelectedIndex = index
                                leftMapLoaded = false
                            },
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Right (Synced)",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconSelectMenu(
                            itemList = rightMenuItems,
                            selectedIndex = rightSelectedIndex,
                            onSelect = { index, _ ->
                                rightSelectedIndex = index
                                rightMapLoaded = false
                            },
                        )
                    }
                }
            }

            // Two maps side by side
            Row(modifier = Modifier.fillMaxSize()) {
                // Left map (Source)
                PostOfficeMapComponent(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    mapViewState = leftState,
                    selectedMarker = selectedMarker,
                    markers = markers,
                    onMapLoaded = {
                        leftMapLoaded = true
                        viewModel.onMapLoaded(it)
                    },
                    onMapClick = viewModel::onMapClick,
                    onInfoWndClick = viewModel::onInfoClick,
                    onCameraMoveEnd = { position ->
                        leftCameraPosition = position
                        rightState.moveCameraTo(position, durationMillis = 0)
                    },
                )

                // Divider
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline),
                )

                // Right map (Synced)
                PostOfficeMapComponent(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    mapViewState = rightState,
                    selectedMarker = selectedMarker,
                    markers = markers,
                    onMapLoaded = { rightMapLoaded = true },
                    onMapClick = viewModel::onMapClick,
                    onInfoWndClick = viewModel::onInfoClick,
                    onCameraMoveEnd = null,
                )
            }
        }

        if (!leftMapLoaded || !rightMapLoaded || isDataLoading) {
            LoadingDialog(
                title = "Loading Post Offices",
                message =
                    when {
                        !leftMapLoaded || !rightMapLoaded -> "Preparing maps..."
                        else -> "Generating markers..."
                    },
            )
        }
    }
}
