package com.mapconductor.arcgis

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
import com.arcgismaps.mapping.ArcGISScene
import com.arcgismaps.mapping.view.Camera
import com.arcgismaps.mapping.view.SceneView
import com.mapconductor.core.GeoPointInterface
import com.mapconductor.core.MapCameraPositionImpl
import com.mapconductor.core.MapPaddings
import com.mapconductor.core.MapViewHolder
import com.mapconductor.core.MapViewState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArcGisMapViewState(
    val context: Context,
    private val id: String,
    private val owner: LifecycleOwner,
): MapViewState {
    private var mapViewHolder: MapViewHolder<WrapSceneView, ArcGISScene>? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val coroutineScope = MainScope() // = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Map padding
    private val _padding = MutableStateFlow(MapPaddings.Zeros)
    val padding: StateFlow<MapPaddings> = _padding.asStateFlow()

    // Camera position
    private val _cameraPosition = MutableStateFlow<Camera?>(null)
    private val cameraPosition: StateFlow<Camera?> = _cameraPosition.asStateFlow()
    override val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraPosition.map { it?.toMapCameraPosition() }.stateIn(
            scope = this.coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    init {
        coroutineScope.launch {
            val existed = MapViewHolderStore.has(id)
            val holder = MapViewHolderStore.getOrCreate(
                context = context,
                id = id,
                owner = owner,
            )
            this@ArcGisMapViewState.mapViewHolder = holder
            mapViewHolder?.mapView?.sceneView?.invalidate()

            if (existed) {
                _isInitialized.value = true

//                mapViewHolder?.mapView?.viewpointChanged?.collect {
//                    this@ArcGisMapViewState._cameraPosition.value = holder.mapView?.getCurrentViewpointCamera()
//                }
                // this@ArcGisMapViewState._cameraPosition.value = holder.map.cameraPosition
                return@launch
            }

            _isInitialized.value = true
            mapViewHolder?.mapView?.sceneView?.viewpointChanged?.collect {
                this@ArcGisMapViewState._cameraPosition.value = holder.mapView.sceneView.getCurrentViewpointCamera()
            }

        }
    }

    override fun moveCameraTo(geoPoint: GeoPointInterface, durationMs: Long): Boolean {
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
            .toCamera()

        coroutineScope.launch {
            mapViewHolder?.mapView?.sceneView?.setViewpointCameraAnimated(
                camera = dstCameraPosition,
                duration = durationMs.toFloat() / 1000.0f,
            )
        }
        return true
    }

    @Keep
    fun setPadding(padding: MapPaddings) {
        this._padding.value = padding
//        mapViewHolder?.map?.setPadding(
//            padding.left.toInt(),
//            padding.top.toInt(),
//            padding.right.toInt(),
//            padding.bottom.toInt(),
//        )
    }


    override fun onResume(owner: LifecycleOwner?) {
        this.mapViewHolder?.mapView?.sceneView?.also { it ->
            it.onResume(owner!!)
            it.renderFrame()
        }
    }
    override fun onPause(owner: LifecycleOwner?) {
        this.mapViewHolder?.mapView?.sceneView?.also { it ->
//            it.onPause(owner!!)
            detach(owner)
        }
    }

    // Destroy the mapView by hand
    override fun destroy(owner: LifecycleOwner?) {
        this.cancelCoroutine()
        MapViewHolderStore.clear(id, owner ?: this.owner)
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

    override fun detach(owner: LifecycleOwner?) {
        this.mapViewHolder?.detach()
    }

    fun renderFrame() {
        this.mapViewHolder?.mapView?.sceneView?.renderFrame()
    }
}

@Composable
fun rememberArcGisMapViewState(
    id: String = "map",
    context: Context = LocalContext.current,
): ArcGisMapViewState {
    // Synchronize the lifecycle with the target compose.
    val owner = LocalLifecycleOwner.current
    val lifecycle = owner.lifecycle

    val state = remember(id) {
        ArcGisMapViewState(context.applicationContext, id, owner)
    }

//    DisposableEffect(lifecycle) {
//        val observer = object : DefaultLifecycleObserver {
//            override fun onResume(owner: LifecycleOwner) {
////                state.onResume(owner)
//            }
//            override fun onPause(owner: LifecycleOwner) {
////                state.onPause(owner)
//            }
//            @SuppressLint("RestrictedApi")
//            override fun onDestroy(owner: LifecycleOwner) {
//                state.cancelCoroutine()
//
//                // ここでActivityが本当に終了するか確認
//                val activity = context.findActivity()
//                if (activity != null &&
//                    activity.isFinishing &&
//                    !activity.isChangingConfigurations
//                ) {
//                    MapViewHolderStore.clear(id, owner)  // Execute mapView.destroy internally
//                }
//            }
//        }
//        lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycle.removeObserver(observer)
//        }
//    }

    return state
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }