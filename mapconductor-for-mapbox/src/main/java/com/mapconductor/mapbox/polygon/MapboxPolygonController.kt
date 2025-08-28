package com.mapconductor.mapbox.polygon

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Polygon
import com.mapbox.maps.extension.style.sources.removeGeoJSONSourceFeatures
import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.mapbox.MapboxActualPolygon
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.polyline.MapboxPolylineOverlayRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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
    override suspend fun createPolygon(state: PolygonState): MapboxActualPolygon? = createMapboxPolygons(state)

    override suspend fun updatePolygonProperties(
        polygon: MapboxActualPolygon,
        current: PolygonEntity<MapboxActualPolygon>,
        prev: PolygonEntity<MapboxActualPolygon>
    ): MapboxActualPolygon? {
        return createMapboxPolygons(current.state)
    }

    override suspend fun removePolygon(entity: PolygonEntity<MapboxActualPolygon>) {
        val featureIds =
            entity.polygon.map { feature ->
                feature.getStringProperty("id")
            }
        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    private fun createMapboxPolygons(state: PolygonState): List<MapboxActualPolygon> {
        val geoPoints: List<IGeoPoint> = state.points.map { GeoPoint.from(it).toPoint() }
        // Close the polygon by adding the first point at the end if not already closed
        val closedPoints =
            if (geoPoints.first() != geoPoints.last()) {
                geoPoints + geoPoints.first()
            } else {
                geoPoints
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
}
