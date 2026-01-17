package com.mapconductor.core.marker

import com.mapconductor.core.controller.OverlayControllerInterface
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition

/**
 * A camera controller that notifies [MarkerTileRenderer] of zoom changes.
 *
 * This controller is registered with the map view controller to receive
 * camera position updates. When the zoom level changes, it notifies the
 * renderer so that tiles can be updated with the correct marker scale.
 *
 * Usage:
 * ```kotlin
 * val renderer = MarkerTileRenderer(...)
 * val cameraController = MarkerTileCameraController(renderer)
 * mapViewController.registerOverlayController(cameraController)
 * ```
 */
class MarkerTileCameraController(
    private val renderer: MarkerTileRenderer,
) : OverlayControllerInterface<Unit, Unit, Unit> {
    override val zIndex: Int = 0
    override var clickListener: ((Unit) -> Unit)? = null

    override suspend fun add(data: List<Unit>) {
        // No-op: markers are managed separately
    }

    override suspend fun update(state: Unit) {
        // No-op: markers are managed separately
    }

    override suspend fun clear() {
        // No-op: markers are managed separately
    }

    override fun find(position: GeoPointInterface): Unit? = null

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        renderer.updateCameraZoom(mapCameraPosition.zoom)
    }

    override fun destroy() {
        // No native resources to clean up.
    }
}
