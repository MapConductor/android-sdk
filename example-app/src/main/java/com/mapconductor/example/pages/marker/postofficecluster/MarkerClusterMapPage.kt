package com.mapconductor.example.pages.marker.postofficecluster

import androidx.collection.LruCache
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.MarkerIconInterface
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import com.mapconductor.example.ui.MessageCard
import com.mapconductor.marker.clustering.MarkerClusterGroupState
import com.mapconductor.postoffice.PostOfficeDataLoader
import com.mapconductor.utils.LoadingDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log

@Composable
fun MarkerClusterMapPage(
    postOfficeIcon: ImageIcon,
    onToggleSidebar: () -> Unit = {},
) {
    val context = LocalContext.current
    val dataLoader = remember { PostOfficeDataLoader(context) }
    val clusterIconProvider: (Int) -> MarkerIconInterface =
        remember(context) {
            val bitmap =
                runCatching {
                    context.assets.open("cluster_red.png").use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
            val cache = ClusterIconLruCache(maxSize = 128)
            val provider: (Int) -> MarkerIconInterface = { count ->
                bitmap?.let { image ->
                    cache.getOrCreate(clusterCountLabel(count)) { label ->
                        ImageIcon(
                            image = drawClusterIcon(background = image, label = label).toDrawable(context.resources),
                            anchor = Offset(0.5f, 0.5f),
                        )
                    }
                } ?: ColorDefaultIcon(label = clusterCountLabel(count))
            }
            provider
        }

    val viewModel: MarkerClusterMapPageViewModelInterface =
        viewModel<MarkerClusterMapPageViewModel>(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(MarkerClusterMapPageViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return MarkerClusterMapPageViewModel(
                                postOfficeIcon = postOfficeIcon,
                                dataLoader = dataLoader,
                            ) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                },
        )

    // Read in composition (not only inside remember/SideEffect) so this scope stays
    // subscribed to the toggle and the SideEffect below re-runs on every change.
    val debugHullPolygons = viewModel.debugHullPolygons
    val clusterState =
        remember(clusterIconProvider) {
            MarkerClusterGroupState(
                clusterIconProvider = clusterIconProvider,
                enableZoomAnimation = true,
                enablePanAnimation = true,
                debugHullPolygons = debugHullPolygons,
                onClusterClick = { cluster ->
                    Log.d("cluster", "---->${cluster.count}")
                }
            )
        }
    SideEffect {
        clusterState.debugHullPolygons = debugHullPolygons
    }

    // Show loading dialog while map or data is loading; start data load once
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val selectedMarker = viewModel.selectedMarker.collectAsState().value
        val markers = viewModel.markerList.collectAsState().value
        val mapViewState = viewModel.mapViewState.collectAsState().value
        val isMapLoaded = viewModel.isMapLoaded.collectAsState().value
        val isDataLoading = viewModel.isDataLoading.collectAsState().value

        DemoMapPageScaffold(
            menuItems = DefaultMapViewItems(viewModel.initCameraPosition),
            onToggleSidebar = onToggleSidebar,
            onMapViewStateChanged = viewModel::onMapViewChanged,
        ) { paddingValues ->
            mapViewState?.let { mapViewState ->
                MarkerClusterMapComponent(
                    mapViewState = mapViewState,
                    selectedMarker = selectedMarker,
                    markers = markers,
                    onMapLoaded = viewModel::onMapLoaded,
                    onMapClick = viewModel::onMapClick,
                    onInfoWndClick = viewModel::onInfoClick,
                    clusterGroupState = clusterState,
                )
            }

            MessageCard(
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            bottom = paddingValues.calculateBottomPadding() + 16.dp,
                            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                        ),
                title = "Controls",
                maxHeight = 200.dp,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(
                        checked = viewModel.debugHullPolygons,
                        onCheckedChange = { checked ->
                            viewModel.debugHullPolygons = checked
                        },
                        thumbContent =
                            if (viewModel.debugHullPolygons) {
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
                        text = "debug",
                        modifier =
                            Modifier
                                .align(Alignment.CenterVertically)
                                .padding(16.dp),
                    )
                }
            }
        }

        if (!isMapLoaded || isDataLoading) {
            LoadingDialog(
                title = "Loading Post Offices",
                message = if (!isMapLoaded) "Preparing map..." else "Generating markers...",
            )
        }
    }
}

private const val CLUSTER_LABEL_TEXT_SIZE_PX = 170f
private val CLUSTER_LABEL_RECT = Rect(15, 20, 497, 184)

private fun clusterCountLabel(count: Int): String =
    when {
        count > 1_000 -> "1k+"
        count > 200 -> "200+"
        count > 100 -> "100+"
        else -> count.toString()
    }

private fun drawClusterIcon(
    background: Bitmap,
    label: String,
): Bitmap {
    val bitmap = background.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(bitmap)
    val textPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.toArgb()
            textSize = CLUSTER_LABEL_TEXT_SIZE_PX
            textAlign = Paint.Align.CENTER
            isSubpixelText = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

    val fontMetrics = textPaint.fontMetrics
    val baseline = CLUSTER_LABEL_RECT.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f
    canvas.drawText(label, CLUSTER_LABEL_RECT.centerX().toFloat(), baseline, textPaint)
    return bitmap
}

private class ClusterIconLruCache(
    maxSize: Int,
) {
    private val cache = LruCache<String, MarkerIconInterface>(maxSize)

    @Synchronized
    fun getOrCreate(
        key: String,
        create: (String) -> MarkerIconInterface,
    ): MarkerIconInterface {
        cache.get(key)?.let { return it }
        return create(key).also { cache.put(key, it) }
    }
}
