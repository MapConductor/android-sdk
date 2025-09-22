package com.mapconductor.core.map

import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPointImpl
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class InitState {
    NotStarted,
    Initializing,
    Initialized,
    Failed,
}

interface MapViewState<ActualMapDesignType> {
    interface MoveCameraCallback {
        fun onComplete()
    }

    val id: String
    val initCameraPosition: MapCameraPositionImpl
    val isInitialized: StateFlow<InitState>
    val cameraPosition: StateFlow<MapCameraPositionImpl?>
    var mapDesignType: ActualMapDesignType

    fun initAsync(init: suspend () -> Boolean)

    fun resetInitState()

    fun moveCameraTo(
        cameraPosition: MapCameraPositionImpl,
        durationMs: Long? = 0,
        listener: MoveCameraCallback? = null,
    )

    fun moveCameraTo(
        position: GeoPointImpl,
        durationMs: Long? = 0,
        listener: MoveCameraCallback? = null,
    )

    fun getMapViewHolder(): MapViewHolder<*, *>?
}

abstract class MapViewStateImpl<ActualMapDesignType>(
    protected val mainCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : MapViewState<ActualMapDesignType> {
    private val tag = this.javaClass.name

    private val _isInitialized = MutableStateFlow(InitState.NotStarted)
    override val isInitialized: StateFlow<InitState> = _isInitialized.asStateFlow()

    override fun resetInitState() {
        this._isInitialized.value = InitState.NotStarted
    }

    protected fun warningLog(message: String) {
        Log.w(tag, message)
    }

    protected fun debugLog(message: String) {
        Log.d(tag, message)
    }

    override fun initAsync(init: suspend () -> Boolean) {
        if (isInitialized.value != InitState.NotStarted) return
        _isInitialized.value = InitState.Initializing

        mainCoroutine.launch {
            try {
                val success = init()
                _isInitialized.value = if (success) InitState.Initialized else InitState.Failed
            } catch (e: Exception) {
                _isInitialized.value = InitState.Failed
                Log.e("MapConductor", "Failed to initialize the Map view", e)
            }
        }
    }
}

interface MapOverlay<DataType> {
    val flow: StateFlow<MutableMap<String, DataType>>

    suspend fun render(
        data: MutableMap<String, DataType>,
        controller: MapViewController,
    )
}

class MapOverlayRegistry {
    private val overlays = mutableListOf<MapOverlay<*>>()

    fun register(overlay: MapOverlay<*>) {
        if (overlays.toSet().contains(overlay)) return
        overlays.add(overlay)
    }

    fun getAll(): List<MapOverlay<*>> = overlays.toList()
}
