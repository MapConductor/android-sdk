package com.mapconductor.core.map

import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPointImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow

enum class InitState {
    NotStarted,
    Initializing,
    SdkInitialized,
    MapViewCreated,
    MapCreated,
    Failed,
}

interface MapViewState<ActualMapDesignType> {
    interface MoveCameraCallback {
        fun onComplete()
    }

    val id: String
    val initCameraPosition: MapCameraPositionImpl
    val cameraPosition: StateFlow<MapCameraPositionImpl?>
    var mapDesignType: ActualMapDesignType

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
