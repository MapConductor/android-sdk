package com.mapconductor.googlemaps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.googlemaps.circle.GoogleMapCircleController
import com.mapconductor.googlemaps.circle.GoogleMapCircleOverlayRenderer
import com.mapconductor.googlemaps.groundimage.GoogleMapGroundImageController
import com.mapconductor.googlemaps.groundimage.GoogleMapGroundImageOverlayRenderer
import com.mapconductor.googlemaps.marker.GoogleMapMarkerController
import com.mapconductor.googlemaps.polygon.GoogleMapPolygonController
import com.mapconductor.googlemaps.polygon.GoogleMapPolygonOverlayRenderer
import com.mapconductor.googlemaps.polyline.GoogleMapPolylineController
import com.mapconductor.googlemaps.polyline.GoogleMapPolylineOverlayRenderer
import com.mapconductor.googlemaps.raster.GoogleMapRasterLayerController
import com.mapconductor.googlemaps.raster.GoogleMapRasterLayerOverlayRenderer
import android.view.ViewGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
fun GoogleMapView(
    state: GoogleMapViewState,
    modifier: Modifier = Modifier,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    onGroundImageClick: OnGroundImageEventHandler? = null,
    content: (@Composable GoogleMapViewScope.() -> Unit)? = null,
) {
    @Suppress("DEPRECATION")
    GoogleMapView(
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
        onGroundImageClick = onGroundImageClick,
        content = content,
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("DEPRECATION")
@Deprecated("Use GroundImageState onClick instead.")
@Composable
fun GoogleMapView(
    state: GoogleMapViewState,
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
    onGroundImageClick: OnGroundImageEventHandler? = null,
    content: (@Composable GoogleMapViewScope.() -> Unit)? = null,
) {
    val scope = remember { GoogleMapViewScope() } // Use specific scope
    val context = LocalContext.current // Context will be available from MapViewBase too if needed
    val registry = remember { scope.buildRegistry() }
    val cameraState = remember { mutableStateOf<MapCameraPositionInterface?>(state.cameraPosition) }

    MapViewBase(
        state = state,
        cameraState = cameraState,
        modifier = modifier,
        viewProvider = {
            val cameraPosition =
                state.cameraPosition.let { camera ->
                    CameraPosition
                        .Builder()
                        .apply {
                            target(GeoPoint.from(camera.position).toLatLng())
                            zoom(camera.zoom.toFloat())
                            bearing(camera.bearing.toFloat())
                            tilt(camera.tilt.toFloat())
                        }.build()
                }

            val mapInitOptions =
                GoogleMapOptions()
                    .mapType(state.mapDesignType.getValue())
                    .camera(cameraPosition)

            MapView(context, mapInitOptions).apply {
                onCreate(null)
            }
        },
        holderProvider = { mapView ->

            suspendCancellableCoroutine<GoogleMapViewHolder> { cont ->
                mapView.getMapAsync { map ->
                    val holder = GoogleMapViewHolder(mapView, map)
                    cont.resume(holder, onCancellation = {})
                }
            }
        },
        controllerProvider = { holder ->
            val markerController =
                getMarkerController(
                    holder = holder,
                )
            val groundImageController = getGroundImageController(holder)
            val polylineController = getPolylineController(holder)
            val polygonController = getPolygonController(holder)
            val circleController = getCircleController(holder)
            val rasterLayerController = getRasterLayerController(holder)

            // Defer initial camera update until controller is created and view is laid out

            GoogleMapViewController(
                markerController = markerController,
                groundImageController = groundImageController,
                polylineController = polylineController,
                polygonController = polygonController,
                circleController = circleController,
                rasterLayerController = rasterLayerController,
                holder = holder,
            ).also { controller ->
                state.setController(controller)
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
                @Suppress("DEPRECATION")
                run {
                    controller.setOnMarkerClickListener(onMarkerClick)
                    controller.setOnMarkerDragStart(onMarkerDragStart)
                    controller.setOnMarkerDrag(onMarkerDrag)
                    controller.setOnMarkerDragEnd(onMarkerDragEnd)
                    controller.setOnCircleClickListener(onCircleClick)
                    controller.setOnPolylineClickListener(onPolylineClick)
                    controller.setOnPolygonClickListener(onPolygonClick)
                    controller.setOnMarkerAnimateStart(onMarkerAnimateStart)
                    controller.setOnMarkerAnimateEnd(onMarkerAnimateEnd)
                    controller.setOnGroundImageClickListener(onGroundImageClick)
                }
                controller.setMapDesignTypeChangeListener(state::onMapDesignTypeChange)
                // Post an initial camera update once the MapView is laid out
                holder.mapView.post { controller.sendInitialCameraUpdate() }
            }
        },
        scope = scope,
        registry = registry,
        onMapLoaded = onMapLoaded,
        customDisposableEffect = { initState, holderRef ->
            // Specific Google Maps DisposableEffect logic
            val lifecycle = LocalLifecycleOwner.current.lifecycle // Get lifecycle here
            DisposableEffect(lifecycle) {
                val stateId = state.id
                val observer =
                    object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) {
                            holderRef.value?.mapView?.onResume()
                        }

                        override fun onPause(owner: LifecycleOwner) {
                            holderRef.value?.mapView?.onPause()
                        }

                        override fun onDestroy(owner: LifecycleOwner) {
                            val activity = context.findActivity()
                            if (activity?.isChangingConfigurations == true) {
                                holderRef.value?.mapView?.let {
                                    (it.parent as? ViewGroup)?.removeView(it)
                                    it.onDestroy()
                                }
                            } else {
                                GoogleMapViewControllerStore.remove(stateId)
                            }
                        }
                    }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }
        },
        sdkInitialize = {
            sdkInitialize?.invoke(context) ?: true
        },
        // Pass content if it needs to be rendered within the overlay providers in MapViewBase,
        // or handle it here if it's specific to GoogleMapView structure before calling MapViewBase.
        // For now, assuming content relates to overlay definitions.
        content = content, // This might need adjustment based on how overlays are handled
    )
}

private fun getPolygonController(holder: GoogleMapViewHolder): GoogleMapPolygonController {
    val renderer =
        GoogleMapPolygonOverlayRenderer(
            holder = holder,
        )

    val controller =
        GoogleMapPolygonController(
            renderer = renderer,
        )
    return controller
}

private fun getGroundImageController(holder: GoogleMapViewHolder): GoogleMapGroundImageController {
    val renderer =
        GoogleMapGroundImageOverlayRenderer(
            holder = holder,
        )

    val controller =
        GoogleMapGroundImageController(
            renderer = renderer,
        )
    return controller
}

private fun getCircleController(holder: GoogleMapViewHolder): GoogleMapCircleController {
    val renderer =
        GoogleMapCircleOverlayRenderer(
            holder = holder,
        )

    val controller =
        GoogleMapCircleController(
            renderer = renderer,
        )
    return controller
}

private fun getPolylineController(holder: GoogleMapViewHolder): GoogleMapPolylineController {
    val renderer =
        GoogleMapPolylineOverlayRenderer(
            holder = holder,
        )

    val controller =
        GoogleMapPolylineController(
            renderer = renderer,
        )
    return controller
}

private fun getMarkerController(holder: GoogleMapViewHolder) =
    GoogleMapMarkerController.create(
        holder = holder,
    )

private fun getRasterLayerController(holder: GoogleMapViewHolder): GoogleMapRasterLayerController {
    val renderer =
        GoogleMapRasterLayerOverlayRenderer(
            holder = holder,
        )
    return GoogleMapRasterLayerController(
        renderer = renderer,
    )
}
