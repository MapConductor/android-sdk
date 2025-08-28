package com.mapconductor.mapbox.polygon

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Polygon
import com.mapbox.maps.extension.style.sources.removeGeoJSONSourceFeatures
import com.mapbox.maps.extension.style.sources.updateGeoJSONSourceFeatures
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.mapbox.MapboxActualPolygon
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.toMapboxColorString
import com.mapconductor.mapbox.toPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapboxPolygonController(
    override val renderer: MapboxPolygonOverlayRenderer,
    polygonManager: PolygonManager<MapboxActualPolygon> = renderer.polygonManager,
) : PolygonController<MapboxActualPolygon>(polygonManager, renderer)


class MapboxPolygonOverlayRenderer(
    val layer: MapboxPolygonLayer,
    val polygonManager: PolygonManager<MapboxActualPolygon>,
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<MapboxActualPolygon>() {

    override suspend fun onRemove(data: List<PolygonEntity<MapboxActualPolygon>>) {
        val featureIds = data.map { entity ->
            entity.polygon.getStringProperty("id")
        }
        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun onPostProcess() {
        val polygons = getAllPolygonEntities()
        coroutine.launch {
            layer.draw(polygons)
        }
    }

    override suspend fun removePolygon(entity: PolygonEntity<MapboxActualPolygon>) {
        val featureIds =
            listOf(entity.polygon.getStringProperty("id"))
        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun createPolygon(state: PolygonState): MapboxActualPolygon? {
        val points = state.points.map { GeoPoint.from(it).toPoint() }
        // Close the polygon by adding the first point at the end if not already closed
        val closedPoints =
            if (points.first() != points.last()) {
                points + points.first()
            } else {
                points
            }
        return Feature.fromGeometry(
            Polygon.fromLngLats(listOf(closedPoints)),
            JsonObject().apply {
                addProperty(MapboxPolygonLayer.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                addProperty(MapboxPolygonLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
                addProperty(MapboxPolygonLayer.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
            },
            "polygon-${state.id}",
        )
    }

    override suspend fun updatePolygonProperties(
        polygon: MapboxActualPolygon,
        current: PolygonEntity<MapboxActualPolygon>,
        prev: PolygonEntity<MapboxActualPolygon>
    ): MapboxActualPolygon? {
        val state = current.state
        val points = state.points.map { GeoPoint.from(it).toPoint() }
        // Close the polygon by adding the first point at the end if not already closed
        val closedPoints =
            if (points.first() != points.last()) {
                points + points.first()
            } else {
                points
            }
        val feature = Feature.fromGeometry(
            Polygon.fromLngLats(listOf(closedPoints)),
            JsonObject().apply {
                addProperty(MapboxPolygonLayer.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                addProperty(MapboxPolygonLayer.Prop.STROKE_WIDTH, ResourceProvider.dpToPx(state.strokeWidth.value))
                addProperty(MapboxPolygonLayer.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
            },
            "polygon-${state.id}",
        )
        layer.source.updateGeoJSONSourceFeatures(listOf(feature))
        return feature
    }

    private fun getAllPolygonEntities(): List<PolygonEntity<MapboxActualPolygon>> {
        // This would need access to the polygon manager
        // For now, we'll implement a simple workaround
        return polygonManager.allEntities()
    }
}
