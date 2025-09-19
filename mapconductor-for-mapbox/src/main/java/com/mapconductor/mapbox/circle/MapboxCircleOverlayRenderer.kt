package com.mapconductor.mapbox.circle

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.sources.removeGeoJSONSourceFeatures
import com.mapconductor.core.circle.AbstractCircleOverlayRenderer
import com.mapconductor.core.circle.CircleEntity
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.mapbox.MapboxActualCircle
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.toMapboxColorString
import com.mapconductor.mapbox.toPoint
import kotlin.math.cos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapboxCircleOverlayRenderer(
    val layer: MapboxCircleLayer =
        MapboxCircleLayer(
            sourceId = "circles-source",
            layerId = "circles-layer",
        ),
    val circleManager: CircleManager<MapboxActualCircle>,
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<MapboxActualCircle>() {
    override suspend fun removeCircle(entity: CircleEntity<MapboxActualCircle>) {
        val featureIds = listOf("circle-${entity.state.id}")
        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun createCircle(state: CircleState): MapboxActualCircle? {
        val centerPoint = GeoPointImpl.from(state.center).toPoint()
        return Feature.fromGeometry(
            Point.fromLngLat(centerPoint.longitude(), centerPoint.latitude()),
            JsonObject().apply {
                val latitudeCorrection = cos(Math.toRadians(centerPoint.latitude()))
                addProperty(MapboxCircleLayer.Prop.LATITUDE_CORRECTION, latitudeCorrection)
                addProperty(MapboxCircleLayer.Prop.RADIUS, state.radiusMeters)
                addProperty(MapboxCircleLayer.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
                addProperty(MapboxCircleLayer.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                addProperty(MapboxCircleLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
            },
            "circle-${state.id}",
        )
    }

    override suspend fun updateCircleProperties(
        circle: MapboxActualCircle,
        current: CircleEntity<MapboxActualCircle>,
        prev: CircleEntity<MapboxActualCircle>,
    ): MapboxActualCircle? {
        val state = current.state
        val centerPoint = GeoPointImpl.from(state.center).toPoint()
        return Feature.fromGeometry(
            Point.fromLngLat(centerPoint.longitude(), centerPoint.latitude()),
            JsonObject().apply {
                val latitudeCorrection = cos(Math.toRadians(centerPoint.latitude()))
                addProperty(MapboxCircleLayer.Prop.LATITUDE_CORRECTION, latitudeCorrection)
                addProperty(MapboxCircleLayer.Prop.RADIUS, state.radiusMeters)
                addProperty(MapboxCircleLayer.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
                addProperty(MapboxCircleLayer.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                addProperty(MapboxCircleLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
            },
            "circle-${state.id}",
        )
    }

    override suspend fun onPostProcess() {
        val circles = getAllCircleEntities()
        coroutine.launch {
            layer.draw(circles)
        }
    }

    private fun getAllCircleEntities(): List<CircleEntity<MapboxActualCircle>> {
        // This would need access to the polyline manager
        // For now, we'll implement a simple workaround
        return circleManager.allEntities()
    }
}
