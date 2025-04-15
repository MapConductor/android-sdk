package com.mapconductor.here

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.here.sdk.core.GeoOrientation
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraListener
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapScheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HereMapViewState(
    private val context: Context,
    private val id: String,
): MapCameraListener {
    private var mapViewHolder: MapViewHolder? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val coroutineScope = MainScope() // = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Camera center position
    private val _cameraState = MutableStateFlow<MapCamera.State?>(null)
    private val cameraState: StateFlow<MapCamera.State?> = _cameraState.asStateFlow()
    val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraState.map { it?.toMapCameraPosition() }.stateIn(
            scope = this.coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )


    init {
        coroutineScope.launch {
            val existed = MapViewHolderStore.has(id)
            val holder = MapViewHolderStore.getOrCreate(
                id,
                context,
            )
            this@HereMapViewState.mapViewHolder = holder
            if (existed) {
                _isInitialized.value = true
                holder.mapView.camera.removeListener(this@HereMapViewState)
                holder.mapView.camera.addListener(this@HereMapViewState)
                return@launch
            }
            _isInitialized.value = true

            holder.mapView.mapScene.loadScene(MapScheme.NORMAL_DAY) { mapError ->
                if (mapError != null) {
                    Log.e("HereMapViewState", "Loading map failed: mapError: " + mapError.name)
                    return@loadScene
                }
                holder.mapView.camera.addListener(this@HereMapViewState)
            }
        }
    }


    fun moveCameraTo(geoPoint: GeoPoint): Boolean {
        if (!this.isInitialized.value) {
            Log.w("GMapViewState", "moveCameraTo() called before map is initialized.")
            return false
        }
        val currCameraPosition = this.mapCameraPosition.value
        if (currCameraPosition == null) return false
        val newPosition = currCameraPosition.copy(
            target = geoPoint,
        )
        return this.moveCameraTo(newPosition)
    }
    fun moveCameraTo(dstPosition: MapCameraPosition): Boolean {
        if (!this.isInitialized.value) {
            Log.w("GMapViewState", "moveCameraTo() called before map is initialized.")
            return false
        }

        this.mapViewHolder?.mapView?.camera?.lookAt(
            dstPosition.target.toGeoCoordinates(),
            GeoOrientation(dstPosition.bearing, dstPosition.tilt).toUpdate(),
            MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, dstPosition.zoom),
        )

        return true
    }

    fun onResume() = this.mapViewHolder?.mapView?.onResume()
    fun onPause() = {
        this.mapViewHolder?.detach()
        this.mapViewHolder?.mapView?.onPause()
    }

    // Destroy the mapView by hand
    fun destroy() {
        this.cancelCoroutine()
        MapViewHolderStore.clear(id)
    }

    internal fun cancelCoroutine() {
        coroutineScope.cancel() // This coroutine scope keeps alive until cancelling.
        // Don't destroy the mapViewHolder here,
        // because the activity will re-create soon when rotating the device.
        // this.mapViewHolder?.destroy()
    }

    internal fun attachTo(container: ViewGroup) {
        this.mapViewHolder?.attachTo(container)
    }

    override fun onMapCameraUpdated(cameraState: MapCamera.State) {
        this._cameraState.value = cameraState
    }
}

@Composable
fun rememberHereMapViewState(
    id: String = "map",
    context: Context = LocalContext.current,
): HereMapViewState {
    val state = remember(id) { HereMapViewState(context, id) }

    // Synchronize the lifecycle with the target compose.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                state.onResume()
            }
            override fun onPause(owner: LifecycleOwner) {
                state.onPause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                state.cancelCoroutine()

                // ここでActivityが本当に終了するか確認
                val activity = context.findActivity()
                if (activity != null &&
                    activity.isFinishing &&
                    !activity.isChangingConfigurations
                ) {
                    MapViewHolderStore.clear(id)  // Execute mapView.destroy internally
                }
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return state
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }