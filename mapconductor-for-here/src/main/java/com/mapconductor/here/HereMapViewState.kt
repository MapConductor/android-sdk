package com.mapconductor.here

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.here.sdk.animation.AnimationListener
import com.here.sdk.animation.AnimationState
import com.here.sdk.core.Anchor2D
import com.here.sdk.core.GeoOrientation
import com.here.sdk.core.Metadata
import com.here.sdk.core.Point2D
import com.here.sdk.core.Rectangle2D
import com.here.sdk.core.Size2D
import com.here.sdk.gestures.TapListener
import com.here.sdk.mapview.HereMap
import com.here.sdk.mapview.ImageFormat
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraAnimationFactory
import com.here.sdk.mapview.MapCameraListener
import com.here.sdk.mapview.MapImage
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapScene
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import com.here.sdk.mapview.MapViewBase
import com.here.time.Duration
import com.mapconductor.core.GeoPointInterface
import com.mapconductor.core.MapCameraPositionImpl
import com.mapconductor.core.MapViewHolder
import com.mapconductor.core.MapViewState
import com.mapconductor.core.MarkerDataWithHandler
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.calculateZIndex
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.WeakHashMap

class HereMapViewState(
    private val context: Context,
    private val id: String,
): MapViewState,
    MapCameraListener, AnimationListener,
    TapListener {
    private var mapViewHolder: MapViewHolder<MapView, HereMap>? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val coroutineScope = MainScope() // = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _markerDataWithHandler: HashMap<Int, MarkerDataWithHandler> = HashMap()

    // Camera center position
    private val _cameraState = MutableStateFlow<MapCamera.State?>(null)
    private val cameraState: StateFlow<MapCamera.State?> = _cameraState.asStateFlow()
    override val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraState.map { it?.toMapCameraPosition() }.stateIn(
            scope = this.coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )


    init {
        coroutineScope.launch {
            val existed = MapViewHolderStore.has(id)
            val holder = MapViewHolderStore.getOrCreate(
                context,
                id,
            )
            this@HereMapViewState.mapViewHolder = holder
            if (existed) {
                _isInitialized.value = true
                holder.mapView.camera.removeListener(this@HereMapViewState)
                holder.mapView.camera.addListener(this@HereMapViewState)
                holder.mapView.gestures.tapListener = this@HereMapViewState
                return@launch
            }
            _isInitialized.value = true

            holder.mapView.mapScene.loadScene(MapScheme.NORMAL_DAY) { mapError ->
                if (mapError != null) {
                    Log.e("HereMapViewState", "Loading map failed: mapError: " + mapError.name)
                    return@loadScene
                }
                holder.mapView.camera.addListener(this@HereMapViewState)
                holder.mapView.gestures.tapListener = this@HereMapViewState
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
            target = GeoPoint.fromImpl(geoPoint),
        )
        return this.moveCameraTo(newPosition, durationMs)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    override fun addMarkers(markerDataList: List<MarkerDataWithHandler>) {
        val defaultIcon = ResourceProvider.getIconResourceWithBitmap(
            ResourceProvider.DEFAULT_MARKER.name,
        )
        val defaultIconBitmap = defaultIcon!!.bitmap
        val defaultIconBytes = bitmapToByteArray(defaultIconBitmap)
        val defaultIconMapImage = MapImage(
            defaultIconBytes,
            ImageFormat.PNG,
            defaultIcon.width.toLong(),
            defaultIcon.height.toLong(),
        )
        val defaultIconAnchor = Anchor2D(
            defaultIcon.anchorX.toDouble() / defaultIcon.width.toDouble(),
            defaultIcon.anchorY.toDouble() / defaultIcon.height.toDouble(),
        )

        val markers = mutableListOf<MapMarker>()
        markerDataList.forEach { markerDataWithHandler ->
            val data = markerDataWithHandler.first
            val handler = markerDataWithHandler.second

            val key = data.hashCode() xor handler.hashCode()
            if (this._markerDataWithHandler.containsKey(key)) return@forEach

            val metadata = Metadata().apply {
                setInteger("dataKey", key)
            }
            val marker = MapMarker(
                GeoPoint.fromImpl(data.pointBase).toGeoCoordinates(),
                defaultIconMapImage,
                defaultIconAnchor,
            ).apply {
                drawOrder = calculateZIndex(data.pointBase).toInt()
            }
            marker.metadata = metadata
            markers.add(marker)


            // marker hashCode -> data key
            this._markerDataWithHandler.set(key, markerDataWithHandler)
        }
        this.mapViewHolder?.mapView?.mapScene?.addMapMarkers(markers)
    }

    override fun moveCameraTo(dstPosition: MapCameraPositionImpl, durationMs: Long): Boolean {
        if (!this.isInitialized.value) {
            Log.w("GMapViewState", "moveCameraTo() called before map is initialized.")
            return false
        }
        val camera = this.mapViewHolder?.mapView?.camera ?: return false

        val dst = MapCameraPosition.fromImpl(dstPosition)
        if (durationMs == 0L) {
            camera.applyUpdate(
                dst.toMapCameraUpdate(),
            )
        } else {
//            bowFactor > 0: 最初にズームアウト → 到達時にズームイン
//            bowFactor < 0: 最初にズームイン → 到達時にズームアウト（ややレア）
//            bowFactor = 0: 常に同じズーム（直線的）
            val bowFactor = 1.0
            val animation = MapCameraAnimationFactory.flyTo(
                dst.target.toGeoCoordinates().toUpdate(),
                GeoOrientation(dst.bearing, dst.tilt).toUpdate(),
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, dst.zoom),
                bowFactor,
                Duration.ofMillis(durationMs),
            )
            camera.startAnimation(animation, this)
        }

        return true
    }

    override fun onResume(owner: LifecycleOwner?) {
        this.mapViewHolder?.mapView?.onResume()
    }
    override fun onPause(owner: LifecycleOwner?) {
        this.mapViewHolder?.detach()
        this.mapViewHolder?.mapView?.onPause()
    }

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

    override fun onMapCameraUpdated(cameraState: MapCamera.State) {
        this._cameraState.value = cameraState
    }

    override fun onAnimationStateChanged(p0: AnimationState) {
        val cameraState = this.mapViewHolder?.mapView?.camera?.state ?: return
        this._cameraState.value = cameraState
    }

    override fun onTap(touchPoint: Point2D) {
        val originInPixels = Point2D(touchPoint.x, touchPoint.y)
        val density = Resources.getSystem().displayMetrics.density
        val sizeInPixels = Size2D(32.0 * density, 32.0 * density)
        val rectangle = Rectangle2D(originInPixels, sizeInPixels)

        this.mapViewHolder?.mapView?.pick(null, rectangle,
            MapViewBase.MapPickCallback { mapPickResult ->
                if (mapPickResult == null) return@MapPickCallback
                val tappedMarker = mapPickResult.mapItems?.markers?.
                    filter { it: MapMarker ->
                        val dataKey = it.metadata?.getInteger("dataKey") ?: 0
                        this._markerDataWithHandler.containsKey(dataKey)
                    }?.maxByOrNull { it: MapMarker -> it.drawOrder }

                if (tappedMarker != null) {
                    val dataKey = tappedMarker.metadata?.getInteger("dataKey") ?: return@MapPickCallback
                    val dataWithHandler = this._markerDataWithHandler.get(dataKey) ?: return@MapPickCallback
                    val data = dataWithHandler.first
                    val onClick = dataWithHandler.second
                    coroutineScope.launch {
                        onClick(data)
                    }

                    return@MapPickCallback
                }
                // TODO: find tapped overlay (do not remove this comment)
            })
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