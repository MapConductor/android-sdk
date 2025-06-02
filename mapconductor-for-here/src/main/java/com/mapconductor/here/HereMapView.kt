package com.mapconductor.here

import android.util.Log
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnMapClickHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun HereMapView(
    state: IHereMapViewState,
    modifier: Modifier = Modifier,
    onMapClick: OnMapClickHandler = {},
    content: (@Composable HereMapViewScope.() -> Unit)? = null,
) {
    val holderRef = remember { Ref<HereMapViewHolder>() }
    val scope = remember { HereMapViewScope() }
    val controllerRef = remember { Ref<HereMapController>() }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val registry = remember { scope.buildRegistry() }

    MapViewBase(
        state = state,
        modifier = modifier,
        holderRef = holderRef,
        controllerRef = controllerRef,
        mapProvider = { this.map },
        viewProvider = { this.mapView },
        scope = scope,
        registry = registry,
        onInitialize = {
            HereMapViewHolderStore.initSDK(context)

            val mapInitOptions = HereMapViewInitOptions(
                scheme = state.mapDesignType.id,
            )

            val holder = HereMapViewHolderStore.getOrCreate(
                context = context,
                id = state.stateId,
                options = mapInitOptions,
            )

            val onCameraMove = (state as? HereMapViewState)?.let {
                it::OnCameraChange
            }

            val controller = HereMapController(
                holder = holder,
                onCameraMove = onCameraMove,
                onMapClick = onMapClick,
            )
            (state as? HereMapViewState)?.controller = controller

            holder.mapView.mapScene.loadScene(state.mapDesignType.id) { mapError ->
                if (mapError != null) {
                    throw Throwable("Loading map failed: mapError: " + mapError.name)
                }
            }
            try {
                holderRef.value = holder
                controllerRef.value = controller

                return@MapViewBase suspendCancellableCoroutine<Boolean> { cont ->
                    val restoreCameraPosition = state.mapCameraPosition.value ?: state.initCameraPosition
                    controller.moveCamera(
                        dstPosition = MapCameraPosition.from(restoreCameraPosition),
                        listener = object : MapViewState.MoveCameraCallback {
                            override fun onComplete(result: Boolean) {
                                cont.resume(result) {  }
                            }
                        }
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
                val stateId = _state.stateId // from BaseMapViewState
                val observer = object : DefaultLifecycleObserver {
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
                                HereMapViewHolderStore.remove(stateId) // Clean up from your store
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
        content = content // This might need adjustment based on how overlays are handled
    )
}
