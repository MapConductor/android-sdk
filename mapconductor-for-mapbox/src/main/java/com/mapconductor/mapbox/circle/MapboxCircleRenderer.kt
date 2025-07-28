package com.mapconductor.mapbox.circle

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.sources.removeGeoJSONSourceFeatures
import com.mapbox.maps.extension.style.sources.updateGeoJSONSourceFeatures
import com.mapconductor.core.circle.AbstractCircleRenderer
import com.mapconductor.core.circle.CircleEntity
import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleOverlayManagerImpl
import com.mapconductor.core.circle.CircleRenderer.UpdateParams
import com.mapconductor.core.circle.CircleRendererFactory
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.mapbox.MapboxActualCircle
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.toMapboxColorString
import com.mapconductor.mapbox.toPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DefaultMapboxCircleRenderer : CircleRendererFactory<MapboxActualCircle> {
    override fun create(
        onAdd: suspend (List<CircleState>) -> List<MapboxActualCircle?>,
        onChange: suspend (List<UpdateParams<MapboxActualCircle>>) -> List<MapboxActualCircle?>,
        onRemove: suspend (List<CircleEntity<MapboxActualCircle>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): CircleOverlayManager<MapboxActualCircle> =
        CircleOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class MapboxCircleRenderer(
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope,
    private val layer: MapboxCircleLayer,
) : AbstractCircleRenderer<MapboxActualCircle>() {
    override suspend fun addCircles(newCircles: List<CircleState>): List<MapboxActualCircle?> {
        val circles =
            newCircles.map { state ->
                val centerPoint = GeoPoint.from(state.center).toPoint()
                Feature.fromGeometry(
                    Point.fromLngLat(centerPoint.longitude(), centerPoint.latitude()),
                    JsonObject().apply {
                        addProperty(MapboxCircleLayer.Prop.RADIUS, state.radiusMeters)
                        addProperty(MapboxCircleLayer.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
                        addProperty(MapboxCircleLayer.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                        addProperty(MapboxCircleLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
                    },
                    "circle-${state.id}",
                )
            }
        return circles
    }

    override suspend fun removeCircles(removeEntities: List<CircleEntity<MapboxActualCircle>>) {
        val featureIds = removeEntities.map { "circle-${it.state.id}" }
        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun changeCircle(changes: List<UpdateParams<MapboxActualCircle>>): List<MapboxActualCircle> {
        val features =
            changes.map { params ->
                val state = params.entity.state
                val centerPoint = GeoPoint.from(state.center).toPoint()
                Feature.fromGeometry(
                    Point.fromLngLat(centerPoint.longitude(), centerPoint.latitude()),
                    JsonObject().apply {
                        addProperty(MapboxCircleLayer.Prop.RADIUS, state.radiusMeters)
                        addProperty(MapboxCircleLayer.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
                        addProperty(MapboxCircleLayer.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                        addProperty(MapboxCircleLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
                    },
                    "circle-${state.id}",
                )
            }
        layer.source.updateGeoJSONSourceFeatures(features)
        return features
    }

    fun redraw() {
        val circles = circleOverlayManager.getAllEntities()
        coroutine.launch {
            layer.draw(circles)
        }
    }
}