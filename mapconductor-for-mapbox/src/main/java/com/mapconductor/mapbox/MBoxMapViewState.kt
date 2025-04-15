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
import com.mapbox.maps.CameraState
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MBoxMapViewState(
    val context: Context,
    private val id: String,
) {
    private var mapViewHolder: MapViewHolder? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val coroutineScope = MainScope() // = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Camera center position
    private val _cameraState = MutableStateFlow<CameraState?>(null)
    private val cameraState: StateFlow<CameraState?> = _cameraState.asStateFlow()
    val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraState.map { it?.toMapCameraPosition() }.stateIn(
            scope = this.coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    init {
        coroutineScope.launch {
            val existed = MapViewHolderStore.has(id)
            val holder = MapViewHolderStore.getOrCreate(id, context)
            this@MBoxMapViewState.mapViewHolder = holder
            if (existed) {
                _isInitialized.value = true
                return@launch
            }

            _isInitialized.value = true
            holder.map.subscribeCameraChanged { changed ->
                _cameraState.value = changed.cameraState
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

        val dstCameraOptions = dstPosition.toCameraOptions()
        val map = this.mapViewHolder?.map
        if (map == null) return false
        map.setCamera(dstCameraOptions)
        return true
    }

    fun onResume() = { Unit }
    fun onPause() = { Unit }

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
}

@Composable
fun rememberMBoxMapViewState(
    id: String = "map",
    context: Context = LocalContext.current,
): MBoxMapViewState {
    val state = remember(id) {
        MBoxMapViewState(context, id)
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