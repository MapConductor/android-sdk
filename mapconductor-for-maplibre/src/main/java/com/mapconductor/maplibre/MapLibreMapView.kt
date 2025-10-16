package com.mapconductor.maplibre

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.map.OnMapViewInitializedHandler
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup


@Composable
fun MapLibreMapView(
    state: MapLibreViewStateImpl,
    modifier: Modifier = Modifier,
//    markerRenderingStrategy: MarkerRenderingStrategy<MapLibreActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
//    onMarkerClick: OnMarkerEventHandler? = null,
//    onMarkerDragStart: OnMarkerEventHandler? = null,
//    onMarkerDrag: OnMarkerEventHandler? = null,
//    onMarkerDragEnd: OnMarkerEventHandler? = null,
//    onMarkerAnimateStart: OnMarkerEventHandler? = null,
//    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
//    onCircleClick: OnCircleEventHandler? = null,
//    onPolylineClick: OnPolylineEventHandler? = null,
//    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapLibreMapViewScope.() -> Unit)? = null,
) {
    val holderRef = remember { Ref<MapLibreViewHolder>() }
    val context = LocalContext.current
    val controllerRef = remember { Ref<MapLibreViewControllerImpl>() }
    val scope = remember { MapLibreMapViewScope() }
    val registry = remember { scope.buildRegistry() }

    MapViewBase(
        state = state,
        modifier = modifier,
        holderRef = holderRef,
        controllerRef = controllerRef,
        viewProvider = { this.mapView },
        scope = scope,
        registry = registry,
        onInitialize = {
            MapLibre.getInstance(context)
            val cameraPosition =
                state.cameraPosition.value.toCameraPosition()
            val mapInitOptions = MapLibreMapOptions.createFromAttributes(context)
                .camera(cameraPosition)
                .textureMode(true)

            val mapView = MapView(context, mapInitOptions).apply {
                onCreate(null)
            }
            val holder =  MapLibreViewHolderImpl(mapView)
            holderRef.value = holder

            mapView.getMapAsync { map ->
                holder.map = map
                val controller =
                    MapLibreViewControllerImpl(
                        holder = holder,
                    )

                holder.map.setStyle(state.mapDesignType.styleJsonURL)

                controller.setCameraMoveListener(state::onCameraChange)
                controller.setMapClickListener(onMapClick)
                controller.setMapDesignTypeChangeListener(state::onMapDesignTypeChange)
                state.setController(controller)
                controller.setMapLoadedListener {
                    onMapLoaded?.invoke(state)
                }
                controllerRef.value = controller
            }

            true
        },
//        customDisposableEffect = { _state, _holderRef ->
//            // Specific Google Maps DisposableEffect logic
//            val lifecycle = LocalLifecycleOwner.current.lifecycle // Get lifecycle here
//            DisposableEffect(lifecycle) {
//                val stateId = _state.id
//                val observer =
//                    object : DefaultLifecycleObserver {
//                        override fun onResume(owner: LifecycleOwner) {
//                            holderRef.value?.mapView?.onResume()
//                        }
//
//                        override fun onPause(owner: LifecycleOwner) {
//                            holderRef.value?.mapView?.onPause()
//                        }
//
//                        override fun onDestroy(owner: LifecycleOwner) {
//                            val activity = context.findActivity()
//                            if (activity?.isChangingConfigurations == true) {
//                                _holderRef.value?.mapView?.let {
//                                    (it.parent as? ViewGroup)?.removeView(it)
//                                    it.onDestroy()
//                                }
//                            } else {
//                                MapLibreViewControllerStore.remove(stateId)
//                            }
//                        }
//                    }
//                lifecycle.addObserver(observer)
//                onDispose {
//                    _state.resetInitState()
//                    lifecycle.removeObserver(observer)
//                }
//            }
//        },

        // Pass content if it needs to be rendered within the overlay providers in MapViewBase,
        // or handle it here if it's specific to GoogleMapsView structure before calling MapViewBase.
        // For now, assuming content relates to overlay definitions.
        content = content, // This might need adjustment based on how overlays are handled
    )
    LaunchedEffect(controllerRef.value, holderRef.value?.map) {
        if (controllerRef.value == null) return@LaunchedEffect

        holderRef.value?.map?.let {
            onMapViewInitialized?.invoke(state)
        }
    }
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
