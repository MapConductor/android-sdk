package com.mapconductor.mapbox.polyline

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.maps.extension.style.sources.removeGeoJSONSourceFeatures
import com.mapconductor.core.createInterpolatePoints
import com.mapconductor.core.createLinearInterpolatePoints
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.features.normalize
import com.mapconductor.core.polyline.AbstractPolylineRenderer
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineOverlayManagerImpl
import com.mapconductor.core.polyline.PolylineRenderer.UpdateParams
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.splitByMeridian
import com.mapconductor.mapbox.MapboxActualPolyline
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.toMapboxColorString
import com.mapconductor.mapbox.toPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DefaultMapboxPolylineRenderer : PolylineRendererFactory<MapboxActualPolyline> {
    override fun create(
        onAdd: suspend (List<PolylineState>) -> List<MapboxActualPolyline?>,
        onChange: suspend (List<UpdateParams<MapboxActualPolyline>>) -> List<MapboxActualPolyline?>,
        onRemove: suspend (List<PolylineEntity<MapboxActualPolyline>>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): PolylineOverlayManager<MapboxActualPolyline> =
        PolylineOverlayManagerImpl(
            onRemove = onRemove,
            onAdd = onAdd,
            onChange = onChange,
            onPostProcess = onPostProcess,
        )
}

class MapboxPolylineRenderer(
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope,
    private val layer: MapboxPolylineLayer,
) : AbstractPolylineRenderer<MapboxActualPolyline>() {
    override suspend fun addPolylines(newLines: List<PolylineState>): List<MapboxActualPolyline?> =
        newLines.map { state -> createMapboxLines(state) }

    private fun createMapboxLines(state: PolylineState): List<Feature> {
        val geoPoints: List<IGeoPoint> =
            when (state.geodesic) {
                true -> createInterpolatePoints(state.points)
                false -> createLinearInterpolatePoints(state.points)
            }.map { it.normalize() }
        return splitByMeridian(geoPoints, state.geodesic).mapIndexed { index, linePoints ->
            val points = linePoints.map { GeoPoint.from(it).toPoint() }
            val id = "polyline-${state.id}-$index"

            return@mapIndexed Feature.fromGeometry(
                LineString.fromLngLats(points),
                JsonObject().apply {
                    addProperty(MapboxPolylineLayer.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                    addProperty(MapboxPolylineLayer.Prop.STROKE_WIDTH, state.strokeWidth.value)
                    addProperty("id", id)
                },
                id,
            )
        }
    }

    override suspend fun removePolylines(removeEntities: List<PolylineEntity<MapboxActualPolyline>>) {
        val featureIds =
            removeEntities.map { entity ->
                entity.polyline.map { feature ->
                    feature.getStringProperty("id")
                }
            }
        layer.source.removeGeoJSONSourceFeatures(featureIds.flatten())
    }

    override suspend fun changePolylines(
        changes: List<UpdateParams<MapboxActualPolyline>>,
    ): List<MapboxActualPolyline> = changes.map { params -> createMapboxLines(params.entity.state) }

    fun redraw() {
        val polylines = polylineOverlayManager.getAllEntities()
        coroutine.launch {
            layer.draw(polylines)
        }
    }
}
