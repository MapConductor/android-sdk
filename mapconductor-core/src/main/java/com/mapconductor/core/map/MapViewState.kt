package com.mapconductor.core.map

import android.util.Log
import com.mapconductor.core.IMapCameraPosition
import com.mapconductor.core.MapDesignType
import com.mapconductor.core.features.IGeoPoint
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

    val stateId: String
    val initCameraPosition: IMapCameraPosition
    val isInitialized: StateFlow<InitState>
    val mapCameraPosition: StateFlow<IMapCameraPosition?>
    val mapDesignType: MapDesignType<T>

    fun initAsync(init: suspend () -> Boolean)
    fun resetInitState()
    fun moveCameraTo(position: IMapCameraPosition, durationMs: Long = 0, listener: MoveCameraCallback? = null)
    fun moveCameraTo(position: IGeoPoint, durationMs: Long = 0, listener: MoveCameraCallback? = null)
//    fun addMarkers(markerDataList: List<MarkerEntry>, listener: AddMarkersCallback? = null)
}

abstract class MapViewStateImpl<T>(
    protected val mainCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
): MapViewState<T> {
    private val TAG = this.javaClass.name

    private val _isInitialized = MutableStateFlow(InitState.NotStarted)
    override val isInitialized: StateFlow<InitState> = _isInitialized.asStateFlow()

    override fun resetInitState() {
        this._isInitialized.value = InitState.NotStarted
    }

    protected fun warningLog(message: String) {
        Log.w(TAG, message)
    }

    protected fun debugLog(message: String) {
        Log.d(TAG, message)
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
