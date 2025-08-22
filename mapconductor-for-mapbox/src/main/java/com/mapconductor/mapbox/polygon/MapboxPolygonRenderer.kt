package com.mapconductor.mapbox.polygon

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Polygon
import com.mapbox.maps.extension.style.sources.removeGeoJSONSourceFeatures
import com.mapbox.maps.extension.style.sources.updateGeoJSONSourceFeatures
import com.mapconductor.core.controller.OverlayRenderer
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.mapbox.MapboxActualPolygon
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.toMapboxColorString
import com.mapconductor.mapbox.toPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MapboxPolygonRenderer(
    val holder: MapboxMapViewHolder,
    val coroutine: CoroutineScope,
    private val layer: MapboxPolygonLayer,
) : OverlayRenderer<MapboxActualPolygon, PolygonState, PolygonEntity<MapboxActualPolygon>> {
    override suspend fun onAdd(data: List<PolygonState>): List<MapboxActualPolygon?> {
        val polygons =
            data.map { state ->
                val points = state.points.map { GeoPoint.from(it).toPoint() }
                // Close the polygon by adding the first point at the end if not already closed
                val closedPoints =
                    if (points.first() != points.last()) {
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

    override suspend fun onRemove(data: List<PolygonEntity<MapboxActualPolygon>>) {
        val featureIds = data.map { "polygon-${it.state.id}" }
        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun onPostProcess() {
        // Do nothing here
    }

    override suspend fun onChange(
        data: List<OverlayRenderer.ChangeParams<PolygonEntity<MapboxActualPolygon>>>,
    ): List<MapboxActualPolygon?> {
        val features =
            data.map { params ->
                val state = params.current.state
                val points = state.points.map { GeoPoint.from(it).toPoint() }
                // Close the polygon by adding the first point at the end if not already closed
                val closedPoints =
                    if (points.first() != points.last()) {
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
        // Note: This method needs to be updated to work with the new architecture
        // when a polygon overlay manager is available
        coroutine.launch {
            // TODO: Update this method when MapViewController integration is complete
        }
    }
}
