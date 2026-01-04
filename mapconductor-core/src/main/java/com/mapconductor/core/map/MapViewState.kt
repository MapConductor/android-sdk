package com.mapconductor.core.map

import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.features.GeoPoint
import kotlinx.coroutines.flow.StateFlow

enum class InitState {
    NotStarted,
    Initializing,
    SdkInitialized,
    MapViewCreated,
    MapCreated,
    Failed,
}

interface MapViewStateInterface<ActualMapDesignType> {
    val id: String
    val cameraPosition: MapCameraPosition
    var mapDesignType: ActualMapDesignType

    fun moveCameraTo(
        cameraPosition: MapCameraPosition,
        durationMillis: Long? = 0,
    )

    fun moveCameraTo(
        position: GeoPoint,
        durationMillis: Long? = 0,
    )

    fun getMapViewHolder(): MapViewHolderInterface<*, *>?
}

abstract class MapViewState<ActualMapDesignType> : MapViewStateInterface<ActualMapDesignType> {
    private val tag = this.javaClass.name
}

interface MapOverlayInterface<DataType> {
    val flow: StateFlow<MutableMap<String, DataType>>

    suspend fun render(
        data: MutableMap<String, DataType>,
        controller: MapViewControllerInterface,
    )
}

class MapOverlayRegistry {
    private val overlays = mutableListOf<MapOverlayInterface<*>>()

    fun register(overlay: MapOverlayInterface<*>) {
        if (overlays.toSet().contains(overlay)) return
        overlays.add(overlay)
    }

    fun getAll(): List<MapOverlayInterface<*>> = overlays.toList()
}
