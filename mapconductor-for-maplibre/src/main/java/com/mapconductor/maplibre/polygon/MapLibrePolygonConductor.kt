package com.mapconductor.maplibre.polygon

import com.mapconductor.core.controller.OverlayControllerInterface
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonEntityInterface
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.maplibre.polyline.MapLibrePolylineOverlayRenderer

class MapLibrePolygonConductor(
    val polygonOverlay: MapLibrePolygonOverlayRenderer,
    val polylineOverlay: MapLibrePolylineOverlayRenderer,
) : OverlayControllerInterface<
        PolygonState,
        PolygonEntityInterface<PolygonState>,
        PolygonEvent,
    > {
    override val zIndex: Int = 2

    override suspend fun add(data: List<PolygonState>) {
        val nextIds = data.asSequence().map { it.id }.toSet()
        val prevIds =
            polygonOverlay.polygonManager
                .allEntities()
                .asSequence()
                .map { it.state.id }
                .toSet()
        val removeIds = prevIds - nextIds

        removeIds.forEach { id ->
            polygonOverlay.polygonManager.removeEntity(id)
            polylineOverlay.polylineManager.removeEntity("outline-$id")
        }

        data.forEach { polygonState ->

            polygonOverlay.createPolygon(polygonState)?.let { polygon ->
                val polygonEntity =
                    PolygonEntity(
                        polygon = polygon,
                        state = polygonState,
                    )
                polygonOverlay.polygonManager.registerEntity(polygonEntity)
            }

            val polylineState = polygonState.toPolylineState()
            polylineOverlay.createPolyline(polylineState)?.let { polyline ->
                val polylineEntity =
                    PolylineEntity(
                        polyline = polyline,
                        state = polylineState,
                    )
                polylineOverlay.polylineManager.registerEntity(polylineEntity)
            }
        }
        polygonOverlay.onPostProcess()
        polylineOverlay.onPostProcess()
    }

    override suspend fun update(state: PolygonState) {
        polygonOverlay.createPolygon(state)?.let { polygon ->
            val polygonEntity =
                PolygonEntity(
                    polygon = polygon,
                    state = state,
                )
            polygonOverlay.polygonManager.registerEntity(polygonEntity)
        }

        val polylineState = state.toPolylineState()
        polylineOverlay.createPolyline(polylineState)?.let { polyline ->
            val polylineEntity =
                PolylineEntity(
                    polyline = polyline,
                    state = polylineState,
                )
            polylineOverlay.polylineManager.registerEntity(polylineEntity)
        }
        polygonOverlay.onPostProcess()
        polylineOverlay.onPostProcess()
    }

    fun dispatchClick(event: PolygonEvent) {
        event.state.onClick?.invoke(event)
        clickListener?.invoke(event)
    }

    override var clickListener: ((PolygonEvent) -> Unit)? = null

    @Suppress("UNCHECKED_CAST")
    override fun find(position: GeoPointInterface): PolygonEntityInterface<PolygonState>? =
        polygonOverlay.polygonManager.find(position) as? PolygonEntityInterface<PolygonState>

    override suspend fun clear() {
        polygonOverlay.polygonManager.clear()
        polylineOverlay.polylineManager.clear()
        polygonOverlay.onPostProcess()
        polylineOverlay.onPostProcess()
    }

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {}

    override fun destroy() {
        // No native resources to clean up for polygons
    }
}

private fun PolygonState.toPolylineState(): PolylineState {
    val closedPoints =
        if (points.first() != points.last()) {
            points + points.first()
        } else {
            points
        }
    return PolylineState(
        points = closedPoints,
        id = "outline-${this.id}",
        strokeColor = this.strokeColor,
        strokeWidth = this.strokeWidth,
        geodesic = this.geodesic,
        zIndex = this.zIndex,
        extra = this.extra,
    )
}
