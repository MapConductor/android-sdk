package com.mapconductor.core.map

import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
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

interface MapViewState<T> {
    interface MoveCameraCallback {
        fun onComplete(result: Boolean)
    }

    val id: String
    val initCameraPosition: MapCameraPosition
    val isInitialized: StateFlow<InitState>
    val mapCameraPosition: StateFlow<MapCameraPosition?>
    val mapDesignType: MapDesignType<T>

    fun initAsync(init: suspend () -> Boolean)

    fun resetInitState()

    fun moveCameraTo(
        cameraPosition: MapCameraPosition,
        durationMs: Long = 0,
        listener: MoveCameraCallback? = null,
    )

    fun moveCameraTo(
        position: GeoPoint,
        durationMs: Long = 0,
        listener: MoveCameraCallback? = null,
    )
}

abstract class MapViewStateImpl<T>(
    protected val mainCoroutine: CoroutineScope =
        CoroutineScope(Dispatchers.Main),
) : MapViewState<T> {
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
                init()
                _isInitialized.value = InitState.Initialized
            } catch (e: Exception) {
                _isInitialized.value = InitState.Failed
                Log.e("MapConductor", "Failed to initialize the Map view", e)
            }
        }
    }
}

interface MapOverlay<T> {
    val flow: StateFlow<List<T>>

    suspend fun render(
        data: List<T>,
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
