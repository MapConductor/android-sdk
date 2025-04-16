package com.mapconductor.googlemaps

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.mapconductor.core.GeoPointImpl
import com.mapconductor.core.MapCameraPositionImpl
import com.mapconductor.core.MapPaddings
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

class GoogleMapViewState(
    val context: Context,
    private val id: String,
): MapViewStateImpl,
    OnCameraMoveListener, OnCameraIdleListener, CancelableCallback {
    private var mapViewHolder: MapViewHolderImpl<MapView, GoogleMap>? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val coroutineScope = MainScope() // = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Map padding
    private val _padding = MutableStateFlow(MapPaddings.Zeros)
    val padding: StateFlow<MapPaddings> = _padding.asStateFlow()

    // Camera position
    private val _cameraPosition = MutableStateFlow<CameraPosition?>(null)
    private val cameraPosition: StateFlow<CameraPosition?> = _cameraPosition.asStateFlow()
    override val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraPosition.map { it?.toMapCameraPosition(padding.value) }.stateIn(
            scope = this.coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    init {
        coroutineScope.launch {
            val existed = MapViewHolderStore.has(id)
            val holder = MapViewHolderStore.getOrCreate(context, id)
            this@GoogleMapViewState.mapViewHolder = holder
            if (existed) {
                _isInitialized.value = true
                holder.map.setOnCameraMoveListener(this@GoogleMapViewState)
                holder.map.setOnCameraIdleListener(this@GoogleMapViewState)
                this@GoogleMapViewState._cameraPosition.value = holder.map.cameraPosition
                return@launch
            }

            val map = holder.map
            _isInitialized.value = true
            map.setOnCameraMoveListener(this@GoogleMapViewState)
            map.setOnCameraIdleListener(this@GoogleMapViewState)
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
            target = geoPoint,
        )
        return this.moveCameraTo(newPosition, durationMs)
    }
    override fun moveCameraTo(dstPosition: MapCameraPositionImpl, durationMs: Long): Boolean {
        if (!this.isInitialized.value) {
            Log.w("GMapViewState", "moveCameraTo() called before map is initialized.")
            return false
        }

        val dstCameraPosition = MapCameraPosition
            .fromImpl(dstPosition)
            .toCameraPosition()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(dstCameraPosition)
        val map = this.mapViewHolder?.map
        if (map ==  null) return false
        if (durationMs == 0L) {
            map.moveCamera(cameraUpdate)
        } else {
            map.animateCamera(cameraUpdate, durationMs.toInt(), this)
        }
        return true
    }

    @Keep
    fun setPadding(padding: MapPaddings) {
        this._padding.value = padding
        mapViewHolder?.map?.setPadding(
            padding.left.toInt(),
            padding.top.toInt(),
            padding.right.toInt(),
            padding.bottom.toInt(),
        )
    }


    override fun onResume() {
        this.mapViewHolder?.mapView?.onResume()
    }
    override fun onPause() {
        this.mapViewHolder?.mapView?.onPause()
    }

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

    override fun onCameraMove() {
        _cameraPosition.value = this.mapViewHolder?.map?.cameraPosition
    }

    override fun onCameraIdle() {
        _cameraPosition.value = this.mapViewHolder?.map?.cameraPosition
    }

    override fun onCancel() {
        _cameraPosition.value = this.mapViewHolder?.map?.cameraPosition
    }

    override fun onFinish() {
        _cameraPosition.value = this.mapViewHolder?.map?.cameraPosition
    }
}

@Composable
fun rememberGMapViewState(
    id: String = "map",
    context: Context = LocalContext.current,
): GoogleMapViewState {
    val state = remember(id) {
        GoogleMapViewState(context, id)
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