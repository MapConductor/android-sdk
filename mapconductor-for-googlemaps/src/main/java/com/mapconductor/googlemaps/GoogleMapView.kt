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
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnMapClickHandler
import android.view.ViewGroup

@Composable
fun GoogleMapsView(
    state: IGoogleMapViewState,
    modifier: Modifier = Modifier,
    onMapClick: OnMapClickHandler = {},
    content: (@Composable GoogleMapViewScope.() -> Unit)? = null,
) {
    val holderRef = remember { Ref<GoogleMapViewHolder>() }
    val controllerRef = remember { Ref<GoogleMapViewController>() }
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
                state.mapCameraPosition.value?.let {
                    CameraPosition
                        .Builder()
                        .apply {
                            target(GeoPoint.from(it.position).toLatLng())
                            zoom(it.zoom.toFloat())
                            bearing(it.bearing.toFloat())
                            tilt(it.tilt.toFloat())
                        }.build()
                }

            val mapInitOptions =
                GoogleMapOptions()
                    .mapType(state.mapDesignType.getValue())
                    .camera(cameraPosition)

            val holder =
                GoogleMapViewHolderStore.getOrCreate(
                    context = context, // Use context from the outer scope
                    id = state.stateId,
                    options = mapInitOptions,
                )
            val onCameraMove =
                (state as? GoogleMapViewState)?.let {
                    it::OnCameraChange
                }
            val controller =
                GoogleMapViewController(
                    holder = holder,
                    onCameraMove = onCameraMove,
                    onMapTap = onMapClick,
                )
            (state as? GoogleMapViewState)?.controller = controller

            holderRef.value = holder
            controllerRef.value = controller
            true // Return success/failure of initialization
        },
        customDisposableEffect = { _state, _holderRef ->
            // Specific Google Maps DisposableEffect logic
            val lifecycle = LocalLifecycleOwner.current.lifecycle // Get lifecycle here
            DisposableEffect(lifecycle) {
                val stateId = _state.stateId
                val observer =
                    object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) {}

                        override fun onPause(owner: LifecycleOwner) {}

                        override fun onDestroy(owner: LifecycleOwner) {
                            val activity = context.findActivity()
                            if (activity?.isChangingConfigurations == true) {
                                (_holderRef.value!!.mapView.parent as? ViewGroup)?.removeView(
                                    _holderRef.value!!.mapView,
                                )
                            } else {
                                GoogleMapViewHolderStore.remove(stateId)
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
