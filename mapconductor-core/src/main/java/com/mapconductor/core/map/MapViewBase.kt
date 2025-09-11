package com.mapconductor.core.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.node.Ref
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapconductor.core.CollectAndRenderOverlays
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.circle.CircleCapable
import com.mapconductor.core.circle.LocalCircleCollector
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.groundimage.GroundImageCapable
import com.mapconductor.core.groundimage.LocalGroundImageCollector
import com.mapconductor.core.info.InfoBubbleOverlay
import com.mapconductor.core.info.LocalInfoBubbleCollector
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.LocalMarkerCollector
import com.mapconductor.core.marker.MarkerCapable
import com.mapconductor.core.polygon.LocalPolygonCollector
import com.mapconductor.core.polygon.PolygonCapable
import com.mapconductor.core.polyline.LocalPolylineCollector
import com.mapconductor.core.polyline.PolylineCapable
import com.mapconductor.settings.Settings
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

typealias OnMapEventHandler = (GeoPoint) -> Unit
typealias OnCameraMoveHandler = (MapCameraPosition) -> Unit

@OptIn(FlowPreview::class)
@Composable
fun <
    SpecificState : MapViewState<*>,
    // Replace Any with a base MapViewController if you have one
    // Generic type for the actual Android Map View (e.g., com.google.android.gms.maps.MapView)
    SpecificController : MapViewController,
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
    val cameraPosition by state.cameraPosition.collectAsState()
    val bubbles by scope.bubbleFlow.collectAsState()
    val controller = controllerRef.value
    val coroutine = rememberCoroutineScope { Dispatchers.Default }
    val cameraTick = remember { mutableIntStateOf(0) }

    if (initState == InitState.Initialized && controller != null) {
        // 収集した子コンポーネントを描画する
        CollectAndRenderOverlays(
            registry = registry, // This should come from the specific scope or be passed
            controller = controller,
        )


        val groundImage = scope.groundImageFlow.collectAsState()
        LaunchedEffect(groundImage) {
            coroutine.launch {
                (controller as? GroundImageCapable)?.let { groundImageCapable ->
                    groundImage.value.forEach { groundImageState ->
                            groundImageState.asFlow().debounce(Settings.Default.composeEventDebounce).collectLatest {
                                if (groundImageCapable.hasGroundImage(groundImageState)) {
                                    groundImageCapable.updateGroundImage(groundImageState)
                                }
                        }
                    }
                }
            }
        }
        val polygons = scope.polygonFlow.collectAsState()
        polygons.value.forEach { polygonState ->
            LaunchedEffect(polygonState.id) {
                polygonState.asFlow().debounce(Settings.Default.composeEventDebounce).collectLatest {
                    (controller as? PolygonCapable)?.let { polygonCapable ->
                        if (polygonCapable.hasPolygon(polygonState)) {
                            polygonCapable.updatePolygon(polygonState)
                        }
                    }
                }
            }
        }
        val polylines = scope.polylineFlow.collectAsState()
        polylines.value.forEach { polylineState ->
            LaunchedEffect(polylineState.id) {
                polylineState.asFlow().debounce(Settings.Default.composeEventDebounce).collectLatest {
                    (controller as? PolylineCapable)?.let { polylineCapable ->
                        if (polylineCapable.hasPolyline(polylineState)) {
                            polylineCapable.updatePolyline(polylineState)
                        }
                    }
                }
            }
        }
        val circles = scope.circleFlow.collectAsState()
        circles.value.forEach { circleState ->
            LaunchedEffect(circleState.id) {
                circleState.asFlow().debounce(Settings.Default.composeEventDebounce).collectLatest {
                    (controller as? CircleCapable)?.let { circleCapable ->
                        if (circleCapable.hasCircle(circleState)) {
                            circleCapable.updateCircle(circleState)
                        }
                    }
                }
            }
        }
        val markers = scope.markerFlow.collectAsState()
        LaunchedEffect(markers) {
            coroutine.launch {
                markers.value.forEach { markerState ->
                    markerState.asFlow().debounce(Settings.Default.composeEventDebounce).collectLatest {
                        (controller as? MarkerCapable)?.let { markerCapable ->
                            if (markerCapable.hasMarker(markerState)) {
                                markerCapable.updateMarker(markerState)
                            }
                        }
                    }
                }
            }
        }
        // 子コンポーネントを収集する
//        CompositionLocalProvider(
//            LocalMarkerCollector provides scope.markerFlow,
//            LocalInfoBubbleCollector provides scope.bubbleFlow,
//            LocalCircleCollector provides scope.circleFlow,
//            LocalPolylineCollector provides scope.polylineFlow,
//            LocalPolygonCollector provides scope.polygonFlow,
//            LocalGroundImageCollector provides scope.groundImageFlow,
//        ) {
//            with(scope) {
//                content?.invoke(this)
//            }
//        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { cameraPosition }
            .map { camera ->
                // 丸めて比較キーに
                cameraInvalidationKey(camera)
            }
            .distinctUntilChanged()
            .collect { cameraTick.intValue = (cameraTick.intValue + 1) % 2 } // 変化時のみ
    }

    SubcomposeLayout(modifier = modifier.clipToBounds()) { constraints ->
        // 1) Map フェーズ：先に Map の AndroidView をレイアウト
        val mapPlaceables = subcompose("map") {
            when (initState) {
                InitState.NotStarted -> BasicMessage("Not initialized yet")
                InitState.Failed      -> BasicMessage("Failed to initialize")
                InitState.Initializing -> BasicMessage("Initializing")
                InitState.Initialized -> {
                    // holder が null ならリセット（元コード準拠）
                    if (holderRef.value == null) {
                        LaunchedEffect(Unit) { state.resetInitState() }
                        BasicMessage("Initializing")
                    } else {
                        AndroidView(factory = { _ ->
                            val v = viewProvider(holderRef.value!!)
                            (v as ViewGroup).layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                            v
                        })
                    }
                }
            }
        }.map { it.measure(constraints) }

        val width = mapPlaceables.maxOfOrNull { it.width } ?: constraints.minWidth
        val height = mapPlaceables.maxOfOrNull { it.height } ?: constraints.minHeight
        val mapSize = IntSize(width, height)

        // 2) Overlay フェーズ：Map のサイズが確定し、かつ controller などが揃っているときだけ合成
        val canOverlay =
            initState == InitState.Initialized &&
                controller != null &&
                mapSize.width > 0 && mapSize.height > 0 &&
                holderRef.value != null

        val overlayPlaceables =
            if (canOverlay) {
                subcompose("slotid") {
                    val _tick = cameraTick.intValue

                    // **ここで初めて CompositionLocalProvider を差し込む**
                    CompositionLocalProvider(
                        LocalMarkerCollector provides scope.markerFlow,
                        LocalInfoBubbleCollector provides scope.bubbleFlow,
                        LocalCircleCollector provides scope.circleFlow,
                        LocalPolylineCollector provides scope.polylineFlow,
                        LocalPolygonCollector provides scope.polygonFlow,
                        LocalGroundImageCollector provides scope.groundImageFlow,
                    ) {
                        // 子（Marker など）の収集＆描画
                        with(scope) { content?.invoke(this) }
                    }

                    // InfoBubble など、Map の座標→スクリーン座標変換が必要なもの
                    // を mapSize 確定後に描画
                    if (bubbles.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                        ) {
                            bubbles.forEach { entry ->
                                val marker = entry.marker
                                val position = marker.position
                                val posOffset = holderRef.value?.toScreenOffset(position)
                                if (posOffset != null) {
                                    val icon = marker.icon ?: DefaultIcon()
                                    val iconScale = icon.scale
                                    val iconSize = ResourceProvider.dpToPx(icon.iconSize.value) * iconScale
                                    InfoBubbleOverlay(
                                        positionOffset = posOffset,
                                        tailOffset = entry.tailOffset,
                                        content = entry.content,
                                        iconSize = Size(iconSize.toFloat(), iconSize.toFloat()),
                                        iconOffset = icon.anchor,
                                        infoAnchorOffset = icon.infoAnchor,
                                    )
                                }
                            }
                        }
                    }
                }.map {
                    // Map と同サイズで測定（全面オーバーレイ）
                    it.measure(Constraints.fixed(mapSize.width, mapSize.height))
                }
            } else emptyList()

        layout(mapSize.width, mapSize.height) {
            mapPlaceables.forEach { it.place(0, 0) }
            overlayPlaceables.forEach { it.place(0, 0) }
        }
    }

    LaunchedEffect(isResourceProviderReady, initState) {
        if (!isResourceProviderReady) return@LaunchedEffect
        if (initState != InitState.NotStarted) return@LaunchedEffect
        state.initAsync(onInitialize)
    }

    customDisposableEffect?.invoke(state, holderRef)
}
//
//    Box(
//        modifier =
//            modifier
//                .background(color = Color.LightGray)
//                .fillMaxSize()
//                .clipToBounds(),
//        contentAlignment = Alignment.Center,
//    ) {
//        when (initState) {
//            InitState.NotStarted -> {
//                BasicText(
//                    text = "Not initialized yet",
//                    modifier = Modifier.fillMaxWidth(),
//                    style =
//                        TextStyle.Default.merge(
//                            fontSize = 13.sp,
//                            textAlign = TextAlign.Center,
//                        ),
//                )
//            }
//
//            InitState.Failed -> {
//                BasicText(
//                    text = "Failed to initialize",
//                    modifier = Modifier.wrapContentSize(align = Alignment.Center),
//                    style = TextStyle.Default.merge(fontSize = 13.sp),
//                )
//            }
//
//            InitState.Initializing -> {
//                BasicText(
//                    text = "Initializing",
//                    modifier = Modifier.wrapContentSize(align = Alignment.Center),
//                    style = TextStyle.Default.merge(fontSize = 13.sp),
//                )
//            }
//
//            InitState.Initialized -> {
//                if (holderRef.value == null) {
//                    state.resetInitState() // Or handle error appropriately
//                } else {
//                    AndroidView(factory = { _ ->
//                        val view = viewProvider(holderRef.value!!)
//                        (view as ViewGroup).layoutParams =
//                            ViewGroup.LayoutParams(
//                                ViewGroup.LayoutParams.MATCH_PARENT,
//                                ViewGroup.LayoutParams.MATCH_PARENT,
//                            )
//                        view
//                    })
//                }
//            }
//        }
//    }
//
//    cameraPosition?.let {
//        if (controller != null && bubbles.isNotEmpty()) {
//            Box(
//                modifier =
//                    Modifier
//                        .fillMaxSize()
//                        .clipToBounds(),
//            ) {
//                bubbles.forEach { entry ->
//                    val marker = entry.marker
//                    val position = marker.position
//                    val positionOffset = holderRef.value?.toScreenOffset(position) ?: return@forEach
//                    val icon = marker.icon ?: DefaultIcon()
//                    val iconScale = icon.scale
//                    val iconSize = ResourceProvider.dpToPx(icon.iconSize.value) * iconScale
//
//                    InfoBubbleOverlay(
//                        positionOffset = positionOffset,
//                        tailOffset = entry.tailOffset,
//                        content = entry.content,
//                        iconSize = Size(iconSize.toFloat(), iconSize.toFloat()),
//                        iconOffset = icon.anchor,
//                        infoAnchorOffset = icon.infoAnchor,
//                    )
//                }
//            }
//        }
//    }
//
//    LaunchedEffect(isResourceProviderReady, initState) {
//        if (!isResourceProviderReady) return@LaunchedEffect
//        if (initState != InitState.NotStarted) return@LaunchedEffect
//        state.initAsync(onInitialize)
//    }
//
//    customDisposableEffect?.invoke(state, holderRef)
//}
@Composable
private fun BasicMessage(text: String) {
    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle.Default.merge(fontSize = 13.sp, textAlign = TextAlign.Center),
        )
    }
}
private fun cameraInvalidationKey(camera: MapCameraPosition?): Long {
    if (camera == null) return 0L
    val latE5 = (camera.position.latitude  * 1e5).toInt()
    val lonE5 = (camera.position.longitude * 1e5).toInt()
    val zoom100 = (camera.zoom * 100).toInt()       // 小数2桁まで
    val bearing10 = (camera.bearing * 10).toInt()   // 小数1桁まで
    // 適当なハッシュ化
    return (((latE5 * 31 + lonE5) * 31 + zoom100) * 31 + bearing10).toLong()
}
