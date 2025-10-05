package com.mapconductor.mapbox.circle

import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapconductor.core.circle.CircleEntity
import com.mapconductor.core.projection.Earth
import com.mapconductor.mapbox.MapboxActualCircle

class MapboxCircleLayer(
    val sourceId: String,
    val layerId: String,
) {
    object Prop {
        const val RADIUS = "radius"
        const val LATITUDE_CORRECTION = "latitudeCorrection"
        const val FILL_COLOR = "fillColor"
        const val STROKE_COLOR = "strokeColor"
        const val STROKE_WIDTH = "strokeWidth"
    }

    companion object {
        private const val TILE_SIZE = 512.0
    }

    /**
     * ズームレベルに基づく半径の式を作成
     */
    private fun createZoomBasedRadiusExpressionWithProperty(): Expression =
        interpolate {
            exponential(2.0) // ズームレベルは指数的に変化
            zoom()

            // ズームレベル0での半径
            stop {
                literal(0.0)
                product {
                    get { literal(Prop.RADIUS) }
                    division {
                        literal(TILE_SIZE)
                        product {
                            get { literal(Prop.LATITUDE_CORRECTION) }
                            literal(Earth.CIRCUMFERENCE_METERS)
                        }
                    }
                }
            }

            // ズームレベル22での半径
            stop {
                literal(22.0)
                product {
                    get { literal(Prop.RADIUS) }
                    division {
                        literal(TILE_SIZE)
                        product {
                            get { literal(Prop.LATITUDE_CORRECTION) }
                            literal(Earth.CIRCUMFERENCE_METERS)
                        }
                    }
                    literal(4194304.0) // 2^22
                }
            }
        }

    val layer =
        circleLayer(layerId, sourceId) {
            circleRadius(createZoomBasedRadiusExpressionWithProperty())
            circleColor(
                get { literal(Prop.FILL_COLOR) },
            )
            circleStrokeColor(
                get { literal(Prop.STROKE_COLOR) },
            )
            circleStrokeWidth(
                get { literal(Prop.STROKE_WIDTH) },
            )
        }

    val source: GeoJsonSource = geoJsonSource(sourceId)

    fun draw(entities: List<CircleEntity<MapboxActualCircle>>) {
        val features = entities.map { it.circle }
        source.featureCollection(
            FeatureCollection.fromFeatures(features),
        )
    }
}
