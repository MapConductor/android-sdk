package com.mapconductor.arcgis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler

@Composable
fun ArcGISMapView(
    state: ArcGISMapViewStateImpl,
    modifier: Modifier = Modifier,
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
    content: (@Composable ArcGISMapViewScope.() -> Unit)? = null,
) {
    val holderRef = remember { Ref<ArcGISMapViewHolder>() }
    val controllerRef = remember { Ref<ArcGISMapViewControllerImpl>() }
    val scope = remember { ArcGISMapViewScope() } // Use specific scope
    val context = LocalContext.current // Context will be available from MapViewBase too if needed
    val registry = remember { scope.buildRegistry() }
    val owner = LocalLifecycleOwner.current
    owner.lifecycle
    val basemapStyle = remember { ArcGISDesign.toBasemapStyle(state.mapDesignType) }

    MapViewBase(
        state = state,
        modifier = modifier,
        holderRef = holderRef,
        controllerRef = controllerRef,
        viewProvider = { this.mapView },
        scope = scope,
        registry = registry,
        onInitialize = {
            val options =
                ArcGISMapViewInitOptions(
                    basemapStyle = basemapStyle,
                    elevationSources = state.mapDesignType.elevationSources,
                )

            val controller =
                ArcGISViewControllerStore.getOrCreate(
                    context = context,
                    id = state.id,
                    options = options,
                )
            state.controller = controller
            controller.holder.mapView.onCreate(owner)
            controller.holder.mapView.onResume(owner)
            controller.setCameraMoveListener(state::onCameraChange)
            controller.setMapClickListener(onMapClick)
            controller.setCircleClickListener(onCircleClick)
            controller.setOnPolylineClickListener(onPolylineClick)
            controller.setOnPolygonClickListener(onPolygonClick)
            controller.setOnMarkerClickListener(onMarkerClick)
            controller.setOnMarkerDragStart(onMarkerDragStart)
            controller.setOnMarkerDrag(onMarkerDrag)
            controller.setOnMarkerDragEnd(onMarkerDragEnd)
            controller.setOnMarkerAnimateStart(onMarkerAnimateStart)
            controller.setOnMarkerAnimateEnd(onMarkerAnimateEnd)

            state.controller = controller

            val restoreCameraPosition =
                state.cameraPosition.value
                    ?: MapCameraPosition.from(state.initCameraPosition)
            controller.moveCamera(restoreCameraPosition)

            controllerRef.value = controller
            holderRef.value = controller.holder
            true
        },
        customDisposableEffect = { _state, _holderRef ->

            // ArcGIS specific DisposableEffect logic
//            DisposableEffect(lifecycle) {
//                val stateId = _stateId // from BaseMapViewState
//                val observer =
//                    object : DefaultLifecycleObserver {
//                        override fun onResume(owner: LifecycleOwner) {
//                            _holderRef.value?.mapView?.onResume(owner)
//                        }
//
//                        override fun onPause(owner: LifecycleOwner) {
//                            _holderRef.value?.mapView?.onPause(owner)
//                        }
//
//                        override fun onDestroy(owner: LifecycleOwner) {
//                            val currentHolder = _holderRef.value
//                            if (currentHolder != null) {
//                                val activity = context.findActivity()
//                                if (activity?.isChangingConfigurations == true) {
//                                    (currentHolder.mapView.parent as? ViewGroup)?.removeView(currentHolder.mapView)
//                                } else {
//                                    // Ensure these calls are safe if mapView might be null or already destroyed
//                                    currentHolder.mapView.onPause(owner)
//                                    currentHolder.mapView.onDestroy(owner)
//                                    _state.controller = null
//                                    ArcGISMapViewHolderStore.remove(stateId) // Clean up from your store
//                                }
//                            }
//                        }
//                    }
//                lifecycle.addObserver(observer)
//                onDispose {
//                    _state.resetInitState()
//                    lifecycle.removeObserver(observer)
//                }
//            }
        },
        content = content,
    )
}
