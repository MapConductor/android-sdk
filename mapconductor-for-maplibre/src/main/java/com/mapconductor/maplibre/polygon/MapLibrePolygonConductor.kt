package com.mapconductor.maplibre.polygon

import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonEntityImpl
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polyline.PolylineEntityImpl
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.maplibre.polyline.MapLibrePolylineOverlayRenderer

class MapLibrePolygonConductor(
    val polygonOverlay: MapLibrePolygonOverlayRenderer,
    val polylineOverlay: MapLibrePolylineOverlayRenderer,
) : OverlayController<
        PolygonState,
        PolygonEntity<PolygonState>,
        PolygonEvent,
    > {
    override val zIndex: Int = 2

    override suspend fun add(data: List<PolygonState>) {
        data.forEach { polygonState ->

            polygonOverlay.createPolygon(polygonState)?.let { polygon ->
                val polygonEntity =
                    PolygonEntityImpl(
                        polygon = polygon,
                        state = polygonState,
                    )
                polygonOverlay.polygonManager.registerEntity(polygonEntity)
            }

            val polylineState = polygonState.toPolylineState()
            polylineOverlay.createPolyline(polylineState)?.let { polyline ->
                val polylineEntity =
                    PolylineEntityImpl(
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
                PolygonEntityImpl(
                    polygon = polygon,
                    state = state,
                )
            polygonOverlay.polygonManager.registerEntity(polygonEntity)
        }

        val polylineState = state.toPolylineState()
        polylineOverlay.createPolyline(polylineState)?.let { polyline ->
            val polylineEntity =
                PolylineEntityImpl(
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

    override fun find(position: GeoPoint): PolygonEntity<PolygonState>? =
        polygonOverlay.polygonManager.find(position) as? PolygonEntity<PolygonState>

    override suspend fun clear() {}

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPositionImpl) {}

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
        extra = this.zIndex,
    )
}
