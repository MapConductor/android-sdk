package com.mapconductor.heatmap

import com.mapconductor.core.controller.OverlayControllerInterface
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition

class HeatmapCameraController(
    private val renderer: HeatmapTileRenderer,
) : OverlayControllerInterface<Unit, Unit, Unit> {
    override val zIndex: Int = 0
    override var clickListener: ((Unit) -> Unit)? = null

    override suspend fun add(data: List<Unit>) {}

    override suspend fun update(state: Unit) {}

    override suspend fun clear() {}

    override fun find(position: GeoPointInterface): Unit? = null

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        renderer.updateCameraZoom(mapCameraPosition.zoom)
    }

    override fun destroy() {
        // No native resources to clean up.
    }
}
