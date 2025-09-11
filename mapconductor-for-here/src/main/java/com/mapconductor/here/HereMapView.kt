package com.mapconductor.here

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler
import android.util.Log
import android.view.ViewGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun HereMapView(
    state: HereViewStateImpl,
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
    content: (@Composable HereViewScope.() -> Unit)? = null,
) {
    val holderRef = remember { Ref<HereViewHolder>() }
    val scope = remember { HereViewScope() }
    val controllerRef = remember { Ref<HereMapViewControllerImpl>() }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
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
            HereMapViewControllerStore.initSDK(context)

            val mapInitOptions =
                HereViewInitOptions(
                    scheme = state.mapDesignType.getValue(),
                )

            val controller =
                HereMapViewControllerStore.getOrCreate(
                    context = context,
                    id = state.id,
                    options = mapInitOptions,
                )

            controller.setCameraMoveListener(state::onCameraChange)
            controller.setMapClickListener(onMapClick)
            controller.setOnMarkerClickListener(onMarkerClick)
            controller.setOnMarkerDragStart(onMarkerDragStart)
            controller.setOnMarkerDrag(onMarkerDrag)
            controller.setOnMarkerDragEnd(onMarkerDragEnd)
            controller.setOnMarkerAnimateStart(onMarkerAnimateStart)
            controller.setOnMarkerAnimateEnd(onMarkerAnimateEnd)
            controller.setOnCircleClickListener(onCircleClick)
            controller.setOnPolylineClickListener(onPolylineClick)
            controller.setOnPolygonClickListener(onPolygonClick)
            state.setController(controller)
            controller.setMapDesignTypeChangeListener(state::onMapDesignTypeChange)

            controller.holder.mapView.mapScene.loadScene(state.mapDesignType.getValue()) { mapError ->
                if (mapError != null) {
                    throw Throwable("Loading map failed: mapError: " + mapError.name)
                }
            }
            try {
                holderRef.value = controller.holder
                controllerRef.value = controller

                return@MapViewBase suspendCancellableCoroutine<Boolean> { cont ->
                    val restoreCameraPosition = state.cameraPosition.value
                    controller.moveCamera(
                        position = restoreCameraPosition,
                        listener =
                            object : MapViewState.MoveCameraCallback {
                                override fun onComplete() {
                                    cont.resume(true) { }
                                }
                            },
                    )
                }
            } catch (e: Exception) {
                Log.e("HereMap", "failed to initialize", e)
                false // Scene loading failed
            }
        },
        customDisposableEffect = { _state, _holderRef ->

            // HERE specific DisposableEffect logic
            DisposableEffect(lifecycle) {
                val stateId = _state.id // from BaseMapViewState
                val observer =
                    object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) {
                            // Do not call here to keep the MapView instance
                            // _holderRef.value?.mapView?.onResume()
                        }

                        override fun onPause(owner: LifecycleOwner) {
                            // Do not call here to keep the MapView instance
                            // _holderRef.value?.mapView?.onPause()
                        }

                        override fun onDestroy(owner: LifecycleOwner) {
                            val currentHolder = _holderRef.value
                            if (currentHolder != null) {
                                val activity = context.findActivity()
                                if (activity?.isChangingConfigurations == true) {
                                    (currentHolder.mapView.parent as? ViewGroup)?.removeView(currentHolder.mapView)
                                } else {
                                    // Ensure these calls are safe if mapView might be null or already destroyed
                                    currentHolder.mapView.onPause()
                                    currentHolder.mapView.onDestroy()
                                    HereMapViewControllerStore.remove(stateId) // Clean up from your store
                                }
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
