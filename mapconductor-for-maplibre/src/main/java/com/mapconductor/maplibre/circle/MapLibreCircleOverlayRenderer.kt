package com.mapconductor.maplibre.circle

import com.google.gson.JsonObject
import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.circle.AbstractCircleOverlayRenderer
import com.mapconductor.core.circle.CircleEntityInterface
import com.mapconductor.core.circle.CircleManagerInterface
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.maplibre.MapLibreActualCircle
import com.mapconductor.maplibre.MapLibreMapViewHolderInterface
import com.mapconductor.maplibre.toMapLibreColorString
import com.mapconductor.maplibre.toPoint
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import kotlin.math.cos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapLibreCircleOverlayRenderer(
    val layer: MapLibreCircleLayer,
    val circleManager: CircleManagerInterface<MapLibreActualCircle>,
    override val holder: MapLibreMapViewHolderInterface,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<MapLibreActualCircle>() {
    override suspend fun createCircle(state: CircleState): MapLibreActualCircle? {
        val center = GeoPoint.from(state.center).toPoint()
        val latCorr = if (state.geodesic) cos(Math.toRadians(center.latitude())) else 1.0
        return Feature.fromGeometry(
            Point.fromLngLat(center.longitude(), center.latitude()),
            JsonObject().apply {
                addProperty(MapLibreCircleLayer.Prop.LATITUDE_CORRECTION, latCorr)
                addProperty(MapLibreCircleLayer.Prop.RADIUS, state.radiusMeters)
                addProperty(MapLibreCircleLayer.Prop.FILL_COLOR, state.fillColor.toMapLibreColorString())
                addProperty(MapLibreCircleLayer.Prop.STROKE_COLOR, state.strokeColor.toMapLibreColorString())
                addProperty(MapLibreCircleLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
                addProperty(MapLibreCircleLayer.Prop.Z_INDEX, state.zIndex ?: calculateZIndex(state.center))
            },
            "circle-${state.id}",
        )
    }

    override suspend fun updateCircleProperties(
        circle: MapLibreActualCircle,
        current: CircleEntityInterface<MapLibreActualCircle>,
        prev: CircleEntityInterface<MapLibreActualCircle>,
    ): MapLibreActualCircle? {
        val state = current.state
        val center = GeoPoint.from(state.center).toPoint()
        val latCorr = if (state.geodesic) cos(Math.toRadians(center.latitude())) else 1.0
        return Feature.fromGeometry(
            Point.fromLngLat(center.longitude(), center.latitude()),
            JsonObject().apply {
                addProperty(MapLibreCircleLayer.Prop.LATITUDE_CORRECTION, latCorr)
                addProperty(MapLibreCircleLayer.Prop.RADIUS, state.radiusMeters)
                addProperty(MapLibreCircleLayer.Prop.FILL_COLOR, state.fillColor.toMapLibreColorString())
                addProperty(MapLibreCircleLayer.Prop.STROKE_COLOR, state.strokeColor.toMapLibreColorString())
                addProperty(MapLibreCircleLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
                addProperty(MapLibreCircleLayer.Prop.Z_INDEX, state.zIndex ?: calculateZIndex(state.center))
            },
            "circle-${state.id}",
        )
    }

    override suspend fun removeCircle(entity: CircleEntityInterface<MapLibreActualCircle>) {
        // Remove by redrawing remaining; nothing to do here
    }

    override suspend fun onPostProcess() {
        val circles = circleManager.allEntities()
        val style = holder.getController()?.getStyleInstance() ?: holder.map.style
        style?.let { s ->
            coroutine.launch { layer.draw(circles, s) }
        }
    }
}
