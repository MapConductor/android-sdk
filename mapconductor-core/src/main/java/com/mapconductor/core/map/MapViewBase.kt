package com.mapconductor.core.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapconductor.core.CollectAndRenderOverlays
import com.mapconductor.core.MapViewScope
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.circle.CircleCapableInterface
import com.mapconductor.core.circle.LocalCircleCollector
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.groundimage.GroundImageCapableInterface
import com.mapconductor.core.groundimage.LocalGroundImageCollector
import com.mapconductor.core.info.InfoBubbleOverlay
import com.mapconductor.core.info.LocalInfoBubbleCollector
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.LocalMarkerCollector
import com.mapconductor.core.marker.MarkerCapableInterface
import com.mapconductor.core.polygon.LocalPolygonCollector
import com.mapconductor.core.polygon.PolygonCapableInterface
import com.mapconductor.core.polyline.LocalPolylineCollector
import com.mapconductor.core.polyline.PolylineCapableInterface
import com.mapconductor.core.raster.LocalRasterLayerCollector
import com.mapconductor.core.raster.RasterLayerCapableInterface
import android.util.Log
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

typealias OnMapLoadedHandler = (MapViewStateInterface<*>) -> Unit
internal typealias InternalOnMapLoadedHandler = () -> Unit
typealias OnMapEventHandler = (GeoPoint) -> Unit
typealias OnCameraMoveHandler = (MapCameraPosition) -> Unit

@Composable
fun <
    SpecificState : MapViewStateInterface<*>,
    // Replace Any with a base MapViewControllerInterface if you have one
    // Generic type for the actual Android Map View (e.g., com.google.android.gms.maps.MapView)
    SpecificController : MapViewControllerInterface,
    ActualMapView : View,
    // Generic type for the actual Map SDK object (e.g., GoogleMap, HereMapSDK.MapController)
    ActualMap : Any,
    // SpecificViewHolder is now constrained by your MapViewHolderInterface interface
    // and uses the ActualMapView and ActualMap generic types.
    SpecificScope : MapViewScope,
    SpecificHolder : MapViewHolderInterface<ActualMapView, ActualMap>,
> MapViewBase(
    state: SpecificState,
    cameraState: MutableState<MapCameraPositionInterface?>,
    modifier: Modifier = Modifier,
    viewProvider: () -> ActualMapView, // Function to get the Android View from ViewHolder
    scope: SpecificScope,
    registry: MapOverlayRegistry, // Replace with your actual registry type from scope.buildRegistry()
    serviceRegistry: MapServiceRegistry = EmptyMapServiceRegistry,
    sdkInitialize: suspend () -> Boolean = { true },
    holderProvider: suspend (mapView: ActualMapView) -> SpecificHolder,
    controllerProvider: suspend (holder: SpecificHolder) -> SpecificController,
    onMapLoaded: OnMapLoadedHandler? = null,
    customDisposableEffect: (@Composable (InitState, Ref<SpecificHolder>) -> Unit)? = null,
    content: (@Composable SpecificScope.() -> Unit)? = null,
) {
    ResourceProvider.init(LocalContext.current)
    val mapViewRef = remember { Ref<ActualMapView>() }
    val controllerRef = remember { Ref<SpecificController>() }
    val holderRef = remember { Ref<SpecificHolder>() }
    var initState by remember { mutableStateOf<InitState>(InitState.NotStarted) }
    val bubbles by scope.bubbleFlow.collectAsState()
    val cameraTick = remember { mutableIntStateOf(0) }
    val controller = controllerRef.value

    if (initState == InitState.MapCreated && controller != null) {
        // 5. 収集した子コンポーネントを描画する
        DisposableEffect(controller) {
            scope.groundImageCollector.setUpdateHandler { groundImageState ->
                (controller as? GroundImageCapableInterface)?.let { groundImageCapable ->
                    if (groundImageCapable.hasGroundImage(groundImageState)) {
                        groundImageCapable.updateGroundImage(groundImageState)
                    }
                }
            }
            scope.rasterLayerCollector.setUpdateHandler { rasterLayerState ->
                (controller as? RasterLayerCapableInterface)?.let { rasterLayerCapable ->
                    if (rasterLayerCapable.hasRasterLayer(rasterLayerState)) {
                        rasterLayerCapable.updateRasterLayer(rasterLayerState)
                    }
                }
            }
            scope.polygonCollector.setUpdateHandler { polygonState ->
                (controller as? PolygonCapableInterface)?.let { polygonCapable ->
                    if (polygonCapable.hasPolygon(polygonState)) {
                        polygonCapable.updatePolygon(polygonState)
                    }
                }
            }
            scope.polylineCollector.setUpdateHandler { polylineState ->
                (controller as? PolylineCapableInterface)?.let { polylineCapable ->
                    if (polylineCapable.hasPolyline(polylineState)) {
                        polylineCapable.updatePolyline(polylineState)
                    }
                }
            }
            scope.circleCollector.setUpdateHandler { circleState ->
                (controller as? CircleCapableInterface)?.let { circleCapable ->
                    if (circleCapable.hasCircle(circleState)) {
                        circleCapable.updateCircle(circleState)
                    }
                }
            }
            scope.markerCollector.setUpdateHandler { markerState ->
                (controller as? MarkerCapableInterface)?.let { markerCapable ->
                    if (markerCapable.hasMarker(markerState)) {
                        markerCapable.updateMarker(markerState)
                    }
                }
            }

            onDispose {
                scope.groundImageCollector.setUpdateHandler(null)
                scope.rasterLayerCollector.setUpdateHandler(null)
                scope.polygonCollector.setUpdateHandler(null)
                scope.polylineCollector.setUpdateHandler(null)
                scope.circleCollector.setUpdateHandler(null)
                scope.markerCollector.setUpdateHandler(null)
            }
        }

        CollectAndRenderOverlays(
            registry = registry, // This should come from the specific scope or be passed
            controller = controller,
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { state.cameraPosition }
            .map { camera ->
                // 丸めて比較キーに
                cameraInvalidationKey(camera)
            }.distinctUntilChanged()
            .collect { cameraTick.intValue = (cameraTick.intValue + 1) % 2 } // 変化時のみ
    }

    SubcomposeLayout(modifier = modifier.fillMaxSize().clipToBounds().background(Color.LightGray)) { constraints ->
        // 2. Map フェーズ：先に Map の AndroidView をレイアウト
        val mapPlaceables =
            subcompose("map") {
                when (initState) {
                    InitState.NotStarted -> BasicMessage("Not initialized yet")
                    InitState.Failed -> BasicMessage("Failed to initialize")
                    InitState.Initializing -> BasicMessage("SDK Initializing")
                    InitState.SdkInitialized -> {
                        // 3. Create a map view
                        viewProvider().also { mapView ->
                            (mapView as ViewGroup).layoutParams =
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                            mapViewRef.value = mapView
                            initState = InitState.MapViewCreated
                        }
                        BasicMessage("Loading.")
                    }
                    else -> {
                        AndroidView(factory = { context ->
                            mapViewRef.value!!
                        })
                    }
                }
            }.map { it.measure(constraints) }

        val width = mapPlaceables.maxOfOrNull { it.width } ?: constraints.minWidth
        val height = mapPlaceables.maxOfOrNull { it.height } ?: constraints.minHeight
        val mapSize = IntSize(width, height)

        // 2) Overlay フェーズ：Map のサイズが確定し、かつ controller などが揃っているときだけ合成
        val canOverlay =
            initState >= InitState.MapViewCreated // &&
        controller != null &&
            mapSize.width > 0 &&
            mapSize.height > 0 &&
            holderRef.value != null

        val overlayPlaceables =
            if (canOverlay) {
                subcompose("slotid") {
                    @Suppress("UNUSED_VARIABLE") // KtLint: backing property rule workaround
                    val tick = cameraTick.intValue
                    val localController = controllerRef.value ?: return@subcompose

                    // 子コンポーネントを収集する
                    // **ここで初めて CompositionLocalProvider を差し込む**
                    CompositionLocalProvider(
                        LocalMapOverlayRegistry provides registry,
                        LocalMapServiceRegistry provides serviceRegistry,
                        LocalMapViewController provides localController,
                        LocalMarkerCollector provides scope.markerCollector,
                        LocalInfoBubbleCollector provides scope.bubbleFlow,
                        LocalCircleCollector provides scope.circleCollector,
                        LocalPolylineCollector provides scope.polylineCollector,
                        LocalPolygonCollector provides scope.polygonCollector,
                        LocalGroundImageCollector provides scope.groundImageCollector,
                        LocalRasterLayerCollector provides scope.rasterLayerCollector,
                    ) {
                        // 子（Marker など）の収集＆描画
                        with(scope) { content?.invoke(this) }
                    }

                    // InfoBubble など、Map の座標→スクリーン座標変換が必要なもの
                    // を mapSize 確定後に描画
                    if (bubbles.isNotEmpty() && cameraState.value != null) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clipToBounds(),
                        ) {
                            bubbles.forEach { mapEntry ->
                                val entry = mapEntry.value
                                val marker = entry.marker
                                val position = marker.position
                                val posOffset = holderRef.value?.toScreenOffset(position)
                                if (posOffset != null) {
                                    // Keep a stable key per marker id; avoid using Flow as a key.
                                    key(marker.id) {
                                        val icon = marker.icon ?: DefaultMarkerIcon()
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
                    }
                }.map {
                    // Map と同サイズで測定（全面オーバーレイ）
                    it.measure(Constraints.fixed(mapSize.width, mapSize.height))
                }
            } else {
                emptyList()
            }

        layout(mapSize.width, mapSize.height) {
            mapPlaceables.forEach { it.place(0, 0) }
            overlayPlaceables.forEach { it.place(0, 0) }
        }
    }

    // 1. Start initialization
    // Use a stable key so changing initState inside doesn't cancel this effect
    LaunchedEffect(Unit) {
        if (initState != InitState.NotStarted) return@LaunchedEffect
        initState = InitState.Initializing
        try {
            val success = sdkInitialize()
            initState = if (success) InitState.SdkInitialized else InitState.Failed
        } catch (ce: CancellationException) {
            // Composition left; don't mark as failure or log error
            return@LaunchedEffect
        } catch (e: Exception) {
            initState = InitState.Failed
            Log.e("MapConductor", "Failed to initialize the Map view", e)
        }
    }

    // 4. Create a map instance, then returns as a holder
    LaunchedEffect(initState) {
        if (initState != InitState.MapViewCreated) return@LaunchedEffect
        mapViewRef.value?.let { mapView ->
            val holder = holderProvider(mapView)
            holderRef.value = holder
            controllerRef.value = controllerProvider(holder)
            initState = InitState.MapCreated
            Log.d("DEBUG", "------------->onMapLoaded")
            onMapLoaded?.invoke(state)
        }
    }

    customDisposableEffect?.invoke(initState, holderRef)
}

@Composable
private fun BasicMessage(text: String) {
    Box(
        modifier =
            Modifier
                .background(Color.LightGray)
                .fillMaxSize(),
        contentAlignment = Alignment.Center,
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
    val latE5 = (camera.position.latitude * 1e5).toInt()
    val lonE5 = (camera.position.longitude * 1e5).toInt()
    val zoom100 = (camera.zoom * 100).toInt() // 小数2桁まで
    val bearing10 = (camera.bearing * 10).toInt() // 小数1桁まで
    // 適当なハッシュ化
    return (((latE5 * 31 + lonE5) * 31 + zoom100) * 31 + bearing10).toLong()
}
