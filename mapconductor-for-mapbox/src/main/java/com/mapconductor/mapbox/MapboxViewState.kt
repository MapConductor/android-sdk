package com.mapconductor.mapbox

import android.annotation.SuppressLint
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
import com.mapbox.maps.CameraChanged
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.CameraState
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapconductor.core.GeoPointImpl
import com.mapconductor.core.MapCameraPositionImpl
import com.mapconductor.core.MapViewHolderImpl
import com.mapconductor.core.MapViewStateImpl
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapboxViewState(
    val context: Context,
    private val id: String,
): MapViewStateImpl, CameraChangedCallback {
    private var mapViewHolder: MapViewHolderImpl<MapView, MapboxMap>? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val coroutineScope = MainScope() // = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Camera center position
    private val _cameraState = MutableStateFlow<CameraState?>(null)
    private val cameraState: StateFlow<CameraState?> = _cameraState.asStateFlow()
    override val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraState.map { it?.toMapCameraPosition() }.stateIn(
            scope = this.coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    init {
        coroutineScope.launch {
            val existed = MapViewHolderStore.has(id)
            val holder = MapViewHolderStore.getOrCreate(context, id)
            this@MapboxViewState.mapViewHolder = holder
            if (existed) {
                _isInitialized.value = true
                holder.map.subscribeCameraChanged(this@MapboxViewState)
                this@MapboxViewState._cameraState.value = holder.map.cameraState
                return@launch
            }

            _isInitialized.value = true
            holder.map.subscribeCameraChanged(this@MapboxViewState)
        }
    }

    override fun moveCameraTo(geoPoint: GeoPointImpl, durationMs: Long): Boolean {
        if (!this.isInitialized.value) {
            Log.w("GMapViewState", "moveCameraTo() called before map is initialized.")
            return false
        }
        val currCameraPosition = this.mapCameraPosition.value
        if (currCameraPosition == null) return false
        val newPosition = currCameraPosition.copy(
            target = GeoPoint.fromImpl(geoPoint),
        )
        return this.moveCameraTo(newPosition, durationMs)
    }
    override fun moveCameraTo(dstPosition: MapCameraPositionImpl, durationMs: Long): Boolean {
        if (!this.isInitialized.value) {
            Log.w("GMapViewState", "moveCameraTo() called before map is initialized.")
            return false
        }

        val dstCameraOptions = MapCameraPosition.fromImpl(dstPosition).toCameraOptions()
        val map = this.mapViewHolder?.map ?: return false
        if (durationMs == 0L) {
            map.setCamera(dstCameraOptions)
        } else {
            map.flyTo(
                dstCameraOptions,
                MapAnimationOptions.mapAnimationOptions {
                    duration(durationMs)
                },
            )
        }
        return true
    }

    override fun onResume() = Unit
    override fun onPause() = Unit

    // Destroy the mapView by hand
    override fun destroy() {
        this.cancelCoroutine()
        MapViewHolderStore.clear(id)
    }

    internal fun cancelCoroutine() {
        coroutineScope.cancel() // This coroutine scope keeps alive until cancelling.
        // Don't destroy the mapViewHolder here,
        // because the activity will re-create soon when rotating the device.
        // this.mapViewHolder?.destroy()
    }

    override fun attachTo(container: ViewGroup) {
        this.mapViewHolder?.attachTo(container)
    }

    override fun detach() {
        this.mapViewHolder?.detach()
    }

    override fun run(cameraChanged: CameraChanged) {
        this._cameraState.value = cameraChanged.cameraState
    }
}

@Composable
fun rememberMBoxMapViewState(
    id: String = "map",
    context: Context = LocalContext.current,
): MapboxViewState {
    val state = remember(id) {
        MapboxViewState(context, id)
    }

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
            @SuppressLint("RestrictedApi")
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