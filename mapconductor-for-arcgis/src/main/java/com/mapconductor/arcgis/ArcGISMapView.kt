package com.mapconductor.arcgis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnMapClickHandler
import android.view.ViewGroup

@Composable
fun ArcGISMapView(
    state: ArcGISMapViewState,
    modifier: Modifier = Modifier,
    onMapClick: OnMapClickHandler = {},
    content: (@Composable ArcGISMapViewScope.() -> Unit)? = null,
) {
    val holderRef = remember { Ref<ArcGISMapViewHolder>() }
    val controllerRef = remember { Ref<ArcGISMapViewController>() }
    val scope = remember { ArcGISMapViewScope() } // Use specific scope
    val context = LocalContext.current // Context will be available from MapViewBase too if needed
    val registry = remember { scope.buildRegistry() }
    val owner = LocalLifecycleOwner.current
    val lifecycle = owner.lifecycle
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

            val options = ArcGISMapViewInitOptions(
                basemapStyle = basemapStyle,
                elevationSources = state.mapDesignType.elevationSources,
            )

            val holder = ArcGISMapViewHolderStore.getOrCreate(
                context = context,
                id = state.stateId,
                options = options,
            )
            holder.mapView.onCreate(owner)
            holder.mapView.onResume(owner)

            val controller =
                ArcGISViewControllerStore.getOrCreate(
                    id = state.stateId,
                    holder = holder,
                )
            controller.setOnCameraMove(state::OnCameraChange)
            controller.setOnMapClick(onMapClick)

            state.controller = controller

            val restoreCameraPosition =
                state.mapCameraPosition.value
                    ?: MapCameraPosition.from(state.initCameraPosition)
            controller.moveCamera(restoreCameraPosition)

            controllerRef.value = controller
            holderRef.value = holder
            true
        },
        customDisposableEffect = { _state, _holderRef ->

            // ArcGIS specific DisposableEffect logic
//            DisposableEffect(lifecycle) {
//                val stateId = _state.stateId // from BaseMapViewState
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
