package com.mapconductor.core.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.Ref
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapconductor.core.CollectAndRenderOverlays
import com.mapconductor.core.LocalCircleCollector
import com.mapconductor.core.LocalMarkerCollector
import com.mapconductor.core.LocalPolylineCollector
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.icons.Default
import com.mapconductor.core.info.InfoWindowCompose
import com.mapconductor.core.info.LocalInfoBubbleCollector
import com.mapconductor.core.marker.MarkerIcon
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

typealias OnMapEventHandler = (GeoPoint) -> Unit
typealias OnCameraMoveHandler<CameraPosition> = (CameraPosition) -> Unit

@OptIn(FlowPreview::class)
@Composable
fun <
    SpecificState : MapViewState<*>,
    // Replace Any with a base MapViewController if you have one
    // Generic type for the actual Android Map View (e.g., com.google.android.gms.maps.MapView)
    SpecificController : MapViewController<*>,
    ActualMapView : View,
    // Generic type for the actual Map SDK object (e.g., GoogleMap, HereMapSDK.MapController)
    ActualMap : Any,
    // SpecificViewHolder is now constrained by your MapViewHolder interface
    // and uses the ActualMapView and ActualMap generic types.
    SpecificViewHolder : MapViewHolder<ActualMapView, ActualMap>,
    SpecificScope : MapViewScope,
> MapViewBase(
    state: SpecificState,
    modifier: Modifier = Modifier,
    holderRef: Ref<SpecificViewHolder>,
    controllerRef: Ref<SpecificController>,
    viewProvider: SpecificViewHolder.() -> ActualMapView, // Function to get the Android View from ViewHolder
    scope: SpecificScope,
    registry: MapOverlayRegistry, // Replace with your actual registry type from scope.buildRegistry()
    onInitialize: suspend () -> Boolean,
    customDisposableEffect: (@Composable (SpecificState, Ref<SpecificViewHolder>) -> Unit)? = null,
    content: (@Composable SpecificScope.() -> Unit)? = null,
) {
    val isResourceProviderReady by ResourceProvider.initialized.collectAsState()
    val initState by state.isInitialized.collectAsState()
    val cameraPosition by state.mapCameraPosition.collectAsState()
    val bubbles by scope.bubbleFlow.collectAsState()
    val controller = controllerRef.value

    if (initState == InitState.Initialized) {
        // 収集した子コンポーネントを描画する
        controllerRef.value?.also { controller ->
            CollectAndRenderOverlays(
                registry = registry, // This should come from the specific scope or be passed
                controller = controller,
            )
            // 子コンポーネントを収集する
            CompositionLocalProvider(
                LocalMarkerCollector provides scope.markerFlow,
                LocalInfoBubbleCollector provides scope.bubbleFlow,
                LocalCircleCollector provides scope.circleFlow,
                LocalPolylineCollector provides scope.polylineFlow,
            ) {
                with(scope) {
                    content?.invoke(this)
                }
            }
            val markers = scope.markerFlow.collectAsState()
            markers.value.forEach { markerState ->
                LaunchedEffect(markerState.id) {
                    markerState.asFlow().debounce(100).collectLatest {
                        controller.updateMarker(markerState)
                    }
                }
            }
            val circles = scope.circleFlow.collectAsState()
            circles.value.forEach { circleState ->
                LaunchedEffect(circleState.id) {
                    circleState.asFlow().debounce(100).collectLatest {
                        controller.updateCircle(circleState)
                    }
                }
            }
        }
    }

    Box(
        modifier =
            modifier
                .background(color = Color.LightGray)
                .fillMaxSize()
                .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        when (initState) {
            InitState.NotStarted -> {
                BasicText(
                    text = "Not initialized yet",
                    modifier = Modifier.fillMaxWidth(),
                    style =
                        TextStyle.Default.merge(
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        ),
                )
            }

            InitState.Failed -> {
                BasicText(
                    text = "Failed to initialize",
                    modifier = Modifier.wrapContentSize(align = Alignment.Center),
                    style = TextStyle.Default.merge(fontSize = 13.sp),
                )
            }

            InitState.Initializing -> {
                BasicText(
                    text = "Initializing",
                    modifier = Modifier.wrapContentSize(align = Alignment.Center),
                    style = TextStyle.Default.merge(fontSize = 13.sp),
                )
            }

            InitState.Initialized -> {
                if (holderRef.value == null) {
                    state.resetInitState() // Or handle error appropriately
                } else {
                    AndroidView(factory = { _ ->
                        val view = viewProvider(holderRef.value!!)
                        (view as ViewGroup).layoutParams =
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                        view
                    })
                }
            }
        }
    }

    if (controller != null && cameraPosition != null && bubbles.isNotEmpty()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clipToBounds(),
        ) {
            bubbles.forEach { entry ->
                val marker = entry.state.marker ?: return@forEach
                val position = marker.position
                val icon = marker.icon ?: MarkerIcon.Default()
                val iconScale = icon.scale ?: 2f
                val positionOffset = holderRef.value?.toScreenOffset(position) ?: return@forEach

                InfoWindowCompose(
                    positionOffset = positionOffset,
                    tailOffset = entry.state.tailOffset,
                    content = entry.content,
                    iconSize =
                        Size(
                            icon.size.width * iconScale,
                            icon.size.height * iconScale,
                        ),
                    iconOffset = icon.anchor,
                    infoAnchorOffset = icon.infoAnchor,
                )
            }
        }
    }

    LaunchedEffect(isResourceProviderReady, initState) {
        if (!isResourceProviderReady) return@LaunchedEffect
        if (initState != InitState.NotStarted) return@LaunchedEffect
        state.initAsync(onInitialize)
    }

    customDisposableEffect?.invoke(state, holderRef)
}
