package com.mapconductor.maplibre.circle

import com.mapconductor.core.circle.CircleEntityInterface
import com.mapconductor.core.projection.Earth
import com.mapconductor.maplibre.MapLibreActualCircle
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection

class MapLibreCircleLayer(
    val sourceId: String,
    val layerId: String,
) {
    object Prop {
        const val RADIUS = "radius"
        const val LATITUDE_CORRECTION = "latitudeCorrection"
        const val FILL_COLOR = "fillColor"
        const val STROKE_COLOR = "strokeColor"
        const val STROKE_WIDTH = "strokeWidth"
        const val Z_INDEX = "zIndex"
    }

    companion object {
        private const val TILE_SIZE = 512.0
    }

    private fun radiusExpression(): Expression =
        Expression.interpolate(
            Expression.exponential(2.0),
            Expression.zoom(),
            // zoom = 0
            Expression.stop(
                0.0,
                Expression.product(
                    Expression.get(Prop.RADIUS),
                    Expression.division(
                        Expression.literal(TILE_SIZE),
                        Expression.product(
                            Expression.get(Prop.LATITUDE_CORRECTION),
                            Expression.literal(Earth.CIRCUMFERENCE_METERS),
                        ),
                    ),
                ),
            ),
            // zoom = 22
            Expression.stop(
                22.0,
                Expression.product(
                    Expression.get(Prop.RADIUS),
                    Expression.division(
                        Expression.literal(TILE_SIZE),
                        Expression.product(
                            Expression.get(Prop.LATITUDE_CORRECTION),
                            Expression.literal(Earth.CIRCUMFERENCE_METERS),
                        ),
                    ),
                    Expression.literal(4194304.0), // 2^22
                ),
            ),
        )

    val source: GeoJsonSource = GeoJsonSource(sourceId)

    val layer: CircleLayer =
        CircleLayer(layerId, sourceId).apply {
            setProperties(
                circleRadius(radiusExpression()),
                circleColor(Expression.get(Prop.FILL_COLOR)),
                circleStrokeColor(Expression.get(Prop.STROKE_COLOR)),
                circleStrokeWidth(Expression.get(Prop.STROKE_WIDTH)),
                org.maplibre.android.style.layers.PropertyFactory
                    .circleSortKey(Expression.get(Prop.Z_INDEX)),
            )
        }

    fun draw(
        entities: List<CircleEntityInterface<MapLibreActualCircle>>,
        style: org.maplibre.android.maps.Style,
    ) {
        val features = entities.map { it.circle }
        val styleSource =
            try {
                style.getSource(sourceId)
            } catch (_: IllegalStateException) {
                null
            }
        if (styleSource is GeoJsonSource) {
            try {
                styleSource.setGeoJson(FeatureCollection.fromFeatures(features))
                return
            } catch (_: IllegalStateException) {
            }
        }
        source.setGeoJson(FeatureCollection.fromFeatures(features))
    }
}
