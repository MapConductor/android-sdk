package com.mapconductor.mapbox.polygon

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Polygon
import com.mapbox.maps.extension.style.sources.removeGeoJSONSourceFeatures
import com.mapbox.maps.extension.style.sources.updateGeoJSONSourceFeatures
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.AbstractPolygonRenderer
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonOverlayManagerImpl
import com.mapconductor.core.polygon.PolygonRenderer.UpdateParams
import com.mapconductor.core.polygon.PolygonRendererFactory
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.mapbox.MapboxActualPolygon
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.toMapboxColorString
import com.mapconductor.mapbox.toPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DefaultMapboxPolygonRenderer : PolygonRendererFactory<MapboxActualPolygon> {
    override fun create(
        onAdd: suspend (List<PolygonState>) -> List<MapboxActualPolygon?>,
        onChange: suspend (List<UpdateParams<MapboxActualPolygon>>) -> List<MapboxActualPolygon?>,
        onRemove: suspend (List<PolygonEntity<MapboxActualPolygon>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): PolygonOverlayManager<MapboxActualPolygon> =
        PolygonOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class MapboxPolygonRenderer(
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope,
    private val layer: MapboxPolygonLayer,
) : AbstractPolygonRenderer<MapboxActualPolygon>() {
    override suspend fun addPolygons(newPolygons: List<PolygonState>): List<MapboxActualPolygon?> {
        val polygons =
            newPolygons.map { state ->
                val points = state.points.map { GeoPoint.from(it).toPoint() }
                // Close the polygon by adding the first point at the end if not already closed
                val closedPoints = if (points.first() != points.last()) {
                    points + points.first()
                } else {
                    points
                }
                Feature.fromGeometry(
                    Polygon.fromLngLats(listOf(closedPoints)),
                    JsonObject().apply {
                        addProperty(MapboxPolygonLayer.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                        addProperty(MapboxPolygonLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
                        addProperty(MapboxPolygonLayer.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
                    },
                    "polygon-${state.id}",
                )
            }
        return polygons
    }

    override suspend fun removePolygons(removeEntities: List<PolygonEntity<MapboxActualPolygon>>) {
        val featureIds = removeEntities.map { "polygon-${it.state.id}" }
        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun changePolygon(changes: List<UpdateParams<MapboxActualPolygon>>): List<MapboxActualPolygon> {
        val features =
            changes.map { params ->
                val state = params.entity.state
                val points = state.points.map { GeoPoint.from(it).toPoint() }
                // Close the polygon by adding the first point at the end if not already closed
                val closedPoints = if (points.first() != points.last()) {
                    points + points.first()
                } else {
                    points
                }
                Feature.fromGeometry(
                    Polygon.fromLngLats(listOf(closedPoints)),
                    JsonObject().apply {
                        addProperty(MapboxPolygonLayer.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                        addProperty(MapboxPolygonLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
                        addProperty(MapboxPolygonLayer.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
                    },
                    "polygon-${state.id}",
                )
            }
        layer.source.updateGeoJSONSourceFeatures(features)
        return features
    }

    fun redraw() {
        val polygons = polygonOverlayManager.getAllEntities()
        coroutine.launch {
            layer.draw(polygons)
        }
    }
}