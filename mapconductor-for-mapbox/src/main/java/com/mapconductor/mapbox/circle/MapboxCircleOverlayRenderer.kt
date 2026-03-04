package com.mapconductor.mapbox.circle

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.sources.removeGeoJSONSourceFeatures
import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.circle.AbstractCircleOverlayRenderer
import com.mapconductor.core.circle.CircleEntityInterface
import com.mapconductor.core.circle.CircleManagerInterface
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
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
    val circleManager: CircleManagerInterface<MapboxActualCircle>,
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<MapboxActualCircle>() {
    override suspend fun removeCircle(entity: CircleEntityInterface<MapboxActualCircle>) {
        val featureIds = listOf("circle-${entity.state.id}")
        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun createCircle(state: CircleState): MapboxActualCircle? {
        val centerPoint = GeoPoint.from(state.center).toPoint()
        val latitudeCorrection =
            if (state.geodesic) {
                cos(Math.toRadians(centerPoint.latitude()))
            } else {
                1.0
            }
        return Feature.fromGeometry(
            Point.fromLngLat(centerPoint.longitude(), centerPoint.latitude()),
            JsonObject().apply {
                addProperty(MapboxCircleLayer.Prop.LATITUDE_CORRECTION, latitudeCorrection)
                addProperty(MapboxCircleLayer.Prop.RADIUS, state.radiusMeters)
                addProperty(MapboxCircleLayer.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
                addProperty(MapboxCircleLayer.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                addProperty(MapboxCircleLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
                addProperty(MapboxCircleLayer.Prop.Z_INDEX, state.zIndex ?: calculateZIndex(state.center))
            },
            "circle-${state.id}",
        )
    }

    override suspend fun updateCircleProperties(
        circle: MapboxActualCircle,
        current: CircleEntityInterface<MapboxActualCircle>,
        prev: CircleEntityInterface<MapboxActualCircle>,
    ): MapboxActualCircle? {
        val state = current.state
        val centerPoint = GeoPoint.from(state.center).toPoint()
        val latitudeCorrection =
            if (state.geodesic) {
                cos(Math.toRadians(centerPoint.latitude()))
            } else {
                1.0
            }
        return Feature.fromGeometry(
            Point.fromLngLat(centerPoint.longitude(), centerPoint.latitude()),
            JsonObject().apply {
                addProperty(MapboxCircleLayer.Prop.LATITUDE_CORRECTION, latitudeCorrection)
                addProperty(MapboxCircleLayer.Prop.RADIUS, state.radiusMeters)
                addProperty(MapboxCircleLayer.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
                addProperty(MapboxCircleLayer.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                addProperty(MapboxCircleLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
                addProperty(MapboxCircleLayer.Prop.Z_INDEX, state.zIndex ?: calculateZIndex(state.center))
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

    private fun getAllCircleEntities(): List<CircleEntityInterface<MapboxActualCircle>> {
        // This would need access to the polyline manager
        // For now, we'll implement a simple workaround
        return circleManager.allEntities()
    }
}
