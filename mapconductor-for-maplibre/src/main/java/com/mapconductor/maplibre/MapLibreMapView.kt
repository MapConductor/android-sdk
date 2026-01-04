package com.mapconductor.maplibre

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.maplibre.circle.MapLibreCircleController
import com.mapconductor.maplibre.circle.MapLibreCircleLayer
import com.mapconductor.maplibre.circle.MapLibreCircleOverlayRenderer
import com.mapconductor.maplibre.marker.MapLibreMarkerController
import com.mapconductor.maplibre.marker.MapLibreMarkerOverlayRenderer
import com.mapconductor.maplibre.marker.MarkerDragLayer
import com.mapconductor.maplibre.marker.MarkerLayer
import com.mapconductor.maplibre.polygon.MapLibrePolygonConductor
import com.mapconductor.maplibre.polygon.MapLibrePolygonLayer
import com.mapconductor.maplibre.polygon.MapLibrePolygonOverlayRenderer
import com.mapconductor.maplibre.polyline.MapLibrePolylineController
import com.mapconductor.maplibre.polyline.MapLibrePolylineLayer
import com.mapconductor.maplibre.polyline.MapLibrePolylineOverlayRenderer
import com.mapconductor.maplibre.raster.MapLibreRasterLayerController
import com.mapconductor.maplibre.raster.MapLibreRasterLayerOverlayRenderer
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun MapLibreMapView(
    state: MapLibreViewState,
    modifier: Modifier = Modifier,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    content: (@Composable MapLibreMapViewScope.() -> Unit)? = null,
) {
    @Suppress("DEPRECATION")
    MapLibreMapView(
        state = state,
        modifier = modifier,
        sdkInitialize = sdkInitialize,
        onMapLoaded = onMapLoaded,
        onMapClick = onMapClick,
        onCameraMoveStart = onCameraMoveStart,
        onCameraMove = onCameraMove,
        onCameraMoveEnd = onCameraMoveEnd,
        onMarkerClick = null,
        onMarkerDragStart = null,
        onMarkerDrag = null,
        onMarkerDragEnd = null,
        onMarkerAnimateStart = null,
        onMarkerAnimateEnd = null,
        onPolylineClick = null,
        onCircleClick = null,
        onPolygonClick = null,
        content = content,
    )
}

@Deprecated("Use CircleState/PolylineState/PolygonState onClick instead.")
@Composable
fun MapLibreMapView(
    state: MapLibreViewState,
    modifier: Modifier = Modifier,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    onMarkerClick: OnMarkerEventHandler?,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapLibreMapViewScope.() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = remember { MapLibreMapViewScope() }
    val registry = remember { scope.buildRegistry() }
    val cameraState = remember { mutableStateOf<MapCameraPositionInterface?>(state.cameraPosition) }

    MapViewBase(
        state = state,
        cameraState = cameraState,
        modifier = modifier,
        viewProvider = {
            val cameraPosition =
                state.cameraPosition.toCameraPosition()
            val mapInitOptions =
                MapLibreMapOptions
                    .createFromAttributes(context)
                    .camera(cameraPosition)
                    .textureMode(true)
            // Don't set style here - it will be set in holderProvider

            MapView(context, mapInitOptions)
        },
        scope = scope,
        registry = registry,
        onMapLoaded = onMapLoaded,
        holderProvider = { mapView ->
            suspendCancellableCoroutine { continuation ->
                mapView.getMapAsync { map ->
                    // Set style and wait for it to load completely
                    map.setStyle(state.mapDesignType.styleJsonURL) {
                        // Resume only after style is fully loaded
                        continuation.resume(MapLibreMapViewHolder(mapView, map)) {}
                    }
                }
            }
        },
        controllerProvider = { holder ->
            val markerController =
                getMarkerController(
                    holder = holder,
                )
            val polylineController =
                getPolylineController(
                    holder = holder,
                )
            val polygonController =
                getPolygonController(
                    holder = holder,
                )
            val circleController = getCircleController(holder)
            val rasterLayerController = getRasterLayerController(holder)

            // Defer initial camera update until controller is created and view is laid out

            MapLibreViewController(
                holder = holder,
                markerController = markerController,
                polylineController = polylineController,
                polygonController = polygonController,
                circleController = circleController,
                rasterLayerController = rasterLayerController,
            ).also { controller ->
                // Store controller reference in holder
                holder.setController(controller)
                controller.setCameraMoveStartListener {
                    cameraState.value = it
                    state.updateCameraPosition(it)
                    onCameraMoveStart?.invoke(it)
                }
                controller.setCameraMoveListener {
                    cameraState.value = it
                    state.updateCameraPosition(it)
                    onCameraMove?.invoke(it)
                }
                controller.setCameraMoveEndListener {
                    cameraState.value = it
                    state.updateCameraPosition(it)
                    onCameraMoveEnd?.invoke(it)
                }
                controller.setMapClickListener(onMapClick)
                controller.setMapDesignTypeChangeListener(state::onMapDesignTypeChange)
                controller.setOnMarkerDragStart(onMarkerDragStart)
                controller.setOnMarkerDrag(onMarkerDrag)
                controller.setOnMarkerDragEnd(onMarkerDragEnd)
                controller.setOnMarkerAnimateEnd(onMarkerAnimateEnd)
                controller.setOnMarkerAnimateStart(onMarkerAnimateStart)
                controller.setOnMarkerClickListener(onMarkerClick)
                controller.setOnPolylineClickListener(onPolylineClick)
                controller.setOnCircleClickListener(onCircleClick)
                controller.setOnPolygonClickListener(onPolygonClick)
                state.setController(controller)
                // Post an initial camera update after layout to compute visibleRegion correctly
                holder.mapView.post { controller.sendInitialCameraUpdate() }
            }
        },
        sdkInitialize = {
            if (sdkInitialize != null) {
                sdkInitialize(context)
            } else {
                MapLibre.getInstance(context)
                true
            }
        },
        // Pass content if it needs to be rendered within the overlay providers in MapViewBase,
        // or handle it here if it's specific to MapLibreMapView structure before calling MapViewBase.
        // For now, assuming content relates to overlay definitions.
        content = content, // This might need adjustment based on how overlays are handled
    )
}

internal fun getMarkerController(holder: MapLibreMapViewHolderInterface): MapLibreMarkerController {
    val manager = MarkerManager.defaultManager<MapLibreActualMarker>()
    val markerLayer: MarkerLayer =
        MarkerLayer(
            sourceId = "markers-source",
            layerId = "markers-layer",
        )
    val dragLayer: MarkerDragLayer =
        MarkerDragLayer(
            sourceId = "marker-drag-source",
            layerId = "marker-drag-layer",
        )
    val renderer =
        MapLibreMarkerOverlayRenderer(
            holder = holder,
            markerLayer = markerLayer,
            dragLayer = dragLayer,
            markerManager = manager,
        )

    val controller =
        MapLibreMarkerController(
            renderer = renderer,
        )
    return controller
}

internal fun getPolylineController(holder: MapLibreMapViewHolderInterface): MapLibrePolylineController {
    val polylineLayer: MapLibrePolylineLayer =
        MapLibrePolylineLayer(
            sourceId = "polyline-source",
            layerId = "polyline-layer",
        )
    val polylineManager = PolylineManager<MapLibreActualPolyline>()

    val renderer =
        MapLibrePolylineOverlayRenderer(
            layer = polylineLayer,
            polylineManager = polylineManager,
            holder = holder,
        )

    val controller =
        MapLibrePolylineController(
            renderer = renderer,
        )
    return controller
}

internal fun getPolygonController(holder: MapLibreMapViewHolderInterface): MapLibrePolygonConductor {
    val polylineLayer =
        MapLibrePolylineLayer(
            sourceId = "polygon-outline-source",
            layerId = "polygon-outline-layer",
        )
    val polylineManager = PolylineManager<MapLibreActualPolyline>()
    val polylineOverlayRenderer =
        MapLibrePolylineOverlayRenderer(
            layer = polylineLayer,
            polylineManager = polylineManager,
            holder = holder,
        )

    val polygonManager = PolygonManager<MapLibreActualPolygon>()
    val polygonLayer =
        MapLibrePolygonLayer(
            sourceId = "polygon-fill-source",
            layerId = "polygon-fill-layer",
        )
    val polygonOverlayRenderer =
        MapLibrePolygonOverlayRenderer(
            layer = polygonLayer,
            polygonManager = polygonManager,
            holder = holder,
        )

    return MapLibrePolygonConductor(
        polygonOverlay = polygonOverlayRenderer,
        polylineOverlay = polylineOverlayRenderer,
    )
}

internal fun getCircleController(holder: MapLibreMapViewHolderInterface): MapLibreCircleController {
    val circleLayer =
        MapLibreCircleLayer(
            sourceId = "circle-source",
            layerId = "circle-layer",
        )
    val circleManager = CircleManager<MapLibreActualCircle>()
    val renderer =
        MapLibreCircleOverlayRenderer(
            layer = circleLayer,
            circleManager = circleManager,
            holder = holder,
        )
    return MapLibreCircleController(
        renderer = renderer,
        circleManager = circleManager,
    )
}

internal fun getRasterLayerController(holder: MapLibreMapViewHolderInterface): MapLibreRasterLayerController {
    val renderer =
        MapLibreRasterLayerOverlayRenderer(
            holder = holder,
        )
    return MapLibreRasterLayerController(
        renderer = renderer,
    )
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
