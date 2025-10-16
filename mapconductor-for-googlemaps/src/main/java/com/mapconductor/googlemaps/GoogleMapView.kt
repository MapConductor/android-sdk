package com.mapconductor.googlemaps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.groundimage.OnGroundImageEventHandler
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.map.OnMapViewInitializedHandler
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler
import android.view.ViewGroup

@Composable
fun GoogleMapsView(
    state: GoogleMapViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<GoogleMapActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    onGroundImageClick: OnGroundImageEventHandler? = null,
    shouldInitialize: Boolean = true, // Allow deferring initialization
    content: (@Composable GoogleMapViewScope.() -> Unit)? = null,
) {
    val holderRef = remember { Ref<GoogleMapViewHolder>() }
    val controllerRef = remember { Ref<GoogleMapViewControllerImpl>() }
    val scope = remember { GoogleMapViewScope() } // Use specific scope
    val context = LocalContext.current // Context will be available from MapViewBase too if needed
    val registry = remember { scope.buildRegistry() }

    MapViewBase(
        state = state,
        modifier = modifier,
        holderRef = holderRef,
        controllerRef = controllerRef,
        viewProvider = { this.mapView }, // Assuming GoogleMapViewHolder has a 'mapView' property
        scope = scope,
        registry = registry,
        onInitialize = {
            // Specific Google Maps initialization logic
            // This lambda will be executed within state.initAsync by MapViewBase
            val cameraPosition =
                state.cameraPosition.value?.let { camera ->
                    CameraPosition
                        .Builder()
                        .apply {
                            target(GeoPointImpl.from(camera.position).toLatLng())
                            zoom(camera.zoom.toFloat())
                            bearing(camera.bearing.toFloat())
                            tilt(camera.tilt.toFloat())
                        }.build()
                }

            val mapInitOptions =
                GoogleMapOptions()
                    .mapType(state.mapDesignType?.getValue() ?: GoogleMapDesign.None.getValue())
                    .camera(cameraPosition)

            val controller =
                GoogleMapViewControllerStore.getOrCreate(
                    context = context, // Use context from the outer scope
                    id = state.id,
                    options = mapInitOptions,
                    markerRenderingStrategy = markerRenderingStrategy,
                )
            state.setController(controller)
            controller.setCameraMoveListener(state::onCameraChange)
            controller.setMapClickListener(onMapClick)
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
            controller.setMapDesignTypeChangeListener(state::onMapDesignTypeChange)
            controller.setMapLoadedListener {
                onMapLoaded?.invoke(state)
            }

            holderRef.value = controller.holder
            controllerRef.value = controller
            true // Return success/failure of initialization
        },
        onMapViewInitialized = onMapViewInitialized,
        shouldInitialize = shouldInitialize, // Pass through the deferred initialization parameter
        customDisposableEffect = { _state, _holderRef ->
            // Specific Google Maps DisposableEffect logic
            val lifecycle = LocalLifecycleOwner.current.lifecycle // Get lifecycle here
            DisposableEffect(lifecycle) {
                val stateId = _state.id
                val observer =
                    object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) {
                            _holderRef.value?.mapView?.onResume()
                        }

                        override fun onPause(owner: LifecycleOwner) {
                            _holderRef.value?.mapView?.onPause()
                        }

                        override fun onDestroy(owner: LifecycleOwner) {
                            val activity = context.findActivity()
                            if (activity?.isChangingConfigurations == true) {
                                _holderRef.value?.mapView?.let {
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
                    _state.resetInitState()
                    lifecycle.removeObserver(observer)
                }
            }
        },
        // Pass content if it needs to be rendered within the overlay providers in MapViewBase,
        // or handle it here if it's specific to GoogleMapsView structure before calling MapViewBase.
        // For now, assuming content relates to overlay definitions.
        content = content, // This might need adjustment based on how overlays are handled
    )
}
