package com.mapconductor.core.map

import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPointImpl
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
    val id: String
    val cameraPosition: MapCameraPositionImpl
    var mapDesignType: ActualMapDesignType

    fun moveCameraTo(
        cameraPosition: MapCameraPositionImpl,
        durationMills: Long? = 0,
    )

    fun moveCameraTo(
        position: GeoPointImpl,
        durationMills: Long? = 0,
    )

    fun getMapViewHolder(): MapViewHolder<*, *>?
}

abstract class MapViewStateImpl<ActualMapDesignType> : MapViewState<ActualMapDesignType> {
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
