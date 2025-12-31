package com.mapconductor.mapbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapconductor.core.circle.CircleManagerImpl
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.PolygonManagerImpl
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineManagerImpl
import com.mapconductor.mapbox.circle.MapboxCircleController
import com.mapconductor.mapbox.circle.MapboxCircleLayer
import com.mapconductor.mapbox.circle.MapboxCircleOverlayRenderer
import com.mapconductor.mapbox.marker.MapboxMarkerController
import com.mapconductor.mapbox.marker.MapboxMarkerOverlayRenderer
import com.mapconductor.mapbox.marker.MarkerDragLayer
import com.mapconductor.mapbox.marker.MarkerLayer
import com.mapconductor.mapbox.polygon.MapboxPolygonConductor
import com.mapconductor.mapbox.polygon.MapboxPolygonLayer
import com.mapconductor.mapbox.polygon.MapboxPolygonOverlayRenderer
import com.mapconductor.mapbox.polyline.MapboxPolylineController
import com.mapconductor.mapbox.polyline.MapboxPolylineLayer
import com.mapconductor.mapbox.polyline.MapboxPolylineOverlayRenderer
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup

@Composable
fun MapboxMapView(
    state: MapboxViewStateImpl,
    modifier: Modifier = Modifier,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    content: (@Composable MapboxMapViewScope.() -> Unit)? = null,
) {
    @Suppress("DEPRECATION")
    MapboxMapView(
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
        onCircleClick = null,
        onPolylineClick = null,
        onPolygonClick = null,
        content = content,
    )
}

@Deprecated("Use CircleState/PolylineState/PolygonState onClick instead.")
@Composable
fun MapboxMapView(
    state: MapboxViewStateImpl,
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
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapboxMapViewScope.() -> Unit)? = null,
) {
    val holderRef = remember { Ref<MapboxMapViewHolder>() }
    val context = LocalContext.current
    val controllerRef = remember { Ref<MapboxMapViewControllerImpl>() }
    val scope = remember { MapboxMapViewScope() }
    val registry = remember { scope.buildRegistry() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val cameraState = remember { mutableStateOf<MapCameraPosition?>(state.cameraPosition) }

    MapViewBase(
        state = state,
        cameraState = cameraState,
        modifier = modifier,
        viewProvider = {
            val cameraOptions =
                state.cameraPosition.toCameraOptions()

            val styleUri = state.mapDesignType.getValue()

            val mapOptions =
                MapInitOptions(
                    context = context,
                    textureView = true,
                    styleUri = styleUri,
                    cameraOptions = cameraOptions,
                )

            MapView(context, mapOptions).also {
                it.onStart()
            }
        },
        holderProvider = { mapView -> MapboxMapViewHolderImpl(mapView, mapView.mapboxMap) },
        controllerProvider = { holder ->

            val markerController =
                getMarkerController(
                    holder = holder,
                )
            val polylineController = getPolylineController(holder)
            val polygonController = getPolygonController(holder)
            val circleController = getCircleController(holder)

            // Defer initial camera update until after controller is created and view is laid out

            MapboxMapViewControllerImpl(
                holder = holder,
                markerController = markerController,
                polylineController = polylineController,
                polygonController = polygonController,
                circleController = circleController,
            ).also { controller ->
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
                controller.setOnCircleClickListener(onCircleClick)
                controller.setOnPolylineClickListener(onPolylineClick)
                controller.setOnPolygonClickListener(onPolygonClick)
                controller.setOnMarkerClickListener(onMarkerClick)
                controller.setOnMarkerDragStart(onMarkerDragStart)
                controller.setOnMarkerDrag(onMarkerDrag)
                controller.setOnMarkerDragEnd(onMarkerDragEnd)
                controller.setOnMarkerAnimateStart(onMarkerAnimateStart)
                controller.setOnMarkerAnimateEnd(onMarkerAnimateEnd)
                controller.setMapDesignTypeChangeListener(state::onMapDesignTypeChange)
                state.setController(controller)

                holderRef.value = holder
                controllerRef.value = controller

                // Post an initial camera update once the MapView is laid out and style is ready
                holder.mapView.post { controller.sendInitialCameraUpdate() }
            }
        },
        scope = scope,
        registry = registry,
        sdkInitialize = {
            if (sdkInitialize != null) {
                sdkInitialize(context)
            } else {
                MapboxInitSDK(context)
                true
            }
        },
        onMapLoaded = onMapLoaded,
        // Pass content if it needs to be rendered within the overlay providers in MapViewBase,
        // or handle it here if it's specific to MapboxMapView structure before calling MapViewBase.
        // For now, assuming content relates to overlay definitions.
        content = content, // This might need adjustment based on how overlays are handled
        customDisposableEffect = { initState, holderRef ->

            // HERE specific DisposableEffect logic
            DisposableEffect(lifecycle) {
                val stateId = state.id // from BaseMapViewState
                val observer =
                    object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) {
                            holderRef.value?.mapView?.onResume()
                        }

                        override fun onPause(owner: LifecycleOwner) {
                            // Do not call here to keep the MapView instance
                            // holderRef.value?.mapView?.onPause()
                        }

                        override fun onDestroy(owner: LifecycleOwner) {
                            val currentHolder = holderRef.value
                            if (currentHolder != null) {
                                val activity = context.findActivity()
                                if (activity?.isChangingConfigurations == true) {
                                    (currentHolder.mapView.parent as? ViewGroup)?.removeView(currentHolder.mapView)
                                } else {
                                    // Ensure these calls are safe if mapView might be null or already destroyed
                                    currentHolder.mapView.onDestroy()
                                }
                            }
                        }
                    }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }
        },
    )
}

internal fun getPolygonController(holder: MapboxMapViewHolder): MapboxPolygonConductor {
    val polylineLayer: MapboxPolylineLayer =
        MapboxPolylineLayer(
            sourceId = "polygon-outline-source",
            layerId = "polygon-outline-layer",
        )
    val polylineManager = PolylineManagerImpl<MapboxActualPolyline>()
    val polylineOverlayRenderer =
        MapboxPolylineOverlayRenderer(
            layer = polylineLayer,
            polylineManager = polylineManager,
            holder = holder,
        )

    val polygonManager = PolygonManagerImpl<MapboxActualPolygon>()
    val polygonLayer: MapboxPolygonLayer =
        MapboxPolygonLayer(
            sourceId = "polygon-fill-source",
            layerId = "polygon-fill-layer",
        )
    val polygonOverlayRenderer =
        MapboxPolygonOverlayRenderer(
            layer = polygonLayer,
            polygonManager = polygonManager,
            holder = holder,
        )

    val conductor =
        MapboxPolygonConductor(
            polygonOverlay = polygonOverlayRenderer,
            polylineOverlay = polylineOverlayRenderer,
        )
    return conductor
}

internal fun getCircleController(holder: MapboxMapViewHolder): MapboxCircleController {
    val circleLayer: MapboxCircleLayer =
        MapboxCircleLayer(
            sourceId = "circle-source",
            layerId = "circle-layer",
        )
    val circleManager = CircleManagerImpl<MapboxActualCircle>()

    val renderer =
        MapboxCircleOverlayRenderer(
            layer = circleLayer,
            circleManager = circleManager,
            holder = holder,
        )

    val controller =
        MapboxCircleController(
            renderer = renderer,
        )
    return controller
}

internal fun getPolylineController(holder: MapboxMapViewHolder): MapboxPolylineController {
    val polylineLayer: MapboxPolylineLayer =
        MapboxPolylineLayer(
            sourceId = "polyline-source",
            layerId = "polyline-layer",
        )
    val polylineManager = PolylineManagerImpl<MapboxActualPolyline>()

    val renderer =
        MapboxPolylineOverlayRenderer(
            layer = polylineLayer,
            polylineManager = polylineManager,
            holder = holder,
        )

    val controller =
        MapboxPolylineController(
            renderer = renderer,
        )
    return controller
}

internal fun getMarkerController(
    holder: MapboxMapViewHolder,
): MapboxMarkerController {
    val manager = MarkerManager.defaultManager<MapboxActualMarker>()
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
        MapboxMarkerOverlayRenderer(
            holder = holder,
            markerLayer = markerLayer,
            dragLayer = dragLayer,
            markerManager = manager,
        )

    val controller =
        MapboxMarkerController(
            renderer = renderer,
        )
    return controller
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
