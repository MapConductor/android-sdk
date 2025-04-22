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
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapconductor.core.GeoPointInterface
import com.mapconductor.core.MapCameraPositionImpl
import com.mapconductor.core.MapViewHolder
import com.mapconductor.core.MapViewState
import com.mapconductor.core.MarkerDataWithHandler
import com.mapconductor.core.ResourceProvider
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
): MapViewState, CameraChangedCallback,
    OnPointAnnotationClickListener {
    private var mapViewHolder: MapViewHolder<MapView, MapboxMap>? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val coroutineScope = MainScope() // = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var pointAnnotationManager: PointAnnotationManager

    // Camera center position
    private val _cameraState = MutableStateFlow<CameraState?>(null)
    private val cameraState: StateFlow<CameraState?> = _cameraState.asStateFlow()
    override val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraState.map { it?.toMapCameraPosition() }.stateIn(
            scope = this.coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    private val _markerDataWithHandler: HashMap<Int, MarkerDataWithHandler> = HashMap()
    private val _markerToData: HashMap<Int, Int> = HashMap()

    init {
        coroutineScope.launch {
            val existed = MapViewHolderStore.has(id)
            val holder = MapViewHolderStore.getOrCreate(context, id)
            this@MapboxViewState.mapViewHolder = holder
            if (existed) {
                _isInitialized.value = true
                this@MapboxViewState.setListeners()
                this@MapboxViewState._cameraState.value = holder.map.cameraState
                return@launch
            }

            _isInitialized.value = true
            holder.map.subscribeCameraChanged(this@MapboxViewState)
            this@MapboxViewState.setListeners()

        }
    }

    private fun setListeners() {
        val map = this.mapViewHolder?.map ?: return
        val mapView = this.mapViewHolder?.mapView ?: return

        map.subscribeCameraChanged(this)
        val annotationApi = mapView.annotations
        this@MapboxViewState.pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationManager.addClickListener(this@MapboxViewState)
    }

    override fun moveCameraTo(geoPoint: GeoPointInterface, durationMs: Long): Boolean {
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

    override fun addMarkers(markerDataList: List<MarkerDataWithHandler>) {
        markerDataList.forEach { markerDataWithHandler ->
            val data = markerDataWithHandler.first
            val handler = markerDataWithHandler.second

            val key = data.hashCode() xor handler.hashCode()
            if (this._markerDataWithHandler.containsKey(key)) return@forEach

            var pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(GeoPoint.fromImpl(data.pointBase).toPoint())
            if (data.icon == null) {
                pointAnnotationOptions = this.setDefaultMarker(pointAnnotationOptions)
            }
            val marker = pointAnnotationManager.create(pointAnnotationOptions)

            // marker hashCode -> data key
            this._markerToData.set(marker.hashCode(), key)
            this._markerDataWithHandler.set(key, markerDataWithHandler)
        }
    }


    private fun setDefaultMarker(options: PointAnnotationOptions): PointAnnotationOptions {
        val icon = ResourceProvider.getIconResourceWithBitmap(ResourceProvider.DEFAULT_MARKER.name)
        if (icon == null) return options;

        val iconW = icon.width.toDouble()
        val anchorX = (iconW / 2.0) - ResourceProvider.DEFAULT_MARKER.anchorX.toDouble()
        val iconH = icon.height.toDouble()
        val anchorY = (iconH / 2.0) - ResourceProvider.DEFAULT_MARKER.anchorY.toDouble()

        return options.withIconImage(icon.bitmap)
            .withIconAnchor(IconAnchor.CENTER)
            .withIconOffset(listOf(anchorX, anchorY))
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

    override fun onResume(owner: LifecycleOwner?) = Unit
    override fun onPause(owner: LifecycleOwner?) = Unit

    // Destroy the mapView by hand
    override fun destroy(owner: LifecycleOwner?) {
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

    override fun detach(owner: LifecycleOwner?) {
        this.mapViewHolder?.detach()
    }

    override fun run(cameraChanged: CameraChanged) {
        this._cameraState.value = cameraChanged.cameraState
    }

    override fun onAnnotationClick(annotation: PointAnnotation): Boolean {
        val dataKey = _markerToData.get(annotation.hashCode()) ?: return false
        val dataWithHandler = _markerDataWithHandler.get(dataKey) ?: return false
        val data = dataWithHandler.first
        val onClick = dataWithHandler.second
        coroutineScope.launch {
            onClick(data)
        }
        return true
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