package com.mapconductor.kml

import android.graphics.Paint

/**
 * 1 フィーチャーを描くのに必要な形に前処理したもの。
 *
 * 元の緯度経度ジオメトリは**捨てる**（`source` は `KMLGeometry.Empty` に差し替える）。
 * 座標は `worldGeometry` が世界座標で持っており、描画も当たり判定もそちらを使う。
 * 両方持つとメモリが倍になり、大きなデータで OOM になる。
 */
internal data class RenderFeature(
    val source: KMLFeature,
    val worldGeometry: WorldGeometry,
    val bounds: WorldBounds,
    val fillPaint: Paint,
    val strokePaint: Paint?,
    val pointRadius: Float,
)

/**
 * スタイルを解決し、[RenderFeature] を組み立てる部分。
 *
 * `Paint` はここで 1 回だけ作る。タイルごとに作ると、1 画面数十枚 ×
 * フィーチャー数だけ確保が走る。
 */
internal object KMLRenderFeatureBuilder {
    fun build(
        feature: KMLFeature,
        layerStyle: KMLTileRenderer.LayerStyle,
        styleProvider: KMLStyleProviderInterface,
    ): RenderFeature {
        val style = styleProvider.getStyle(feature, layerStyle)
        return buildRenderFeatureFromStyle(
            feature,
            feature.geometry,
            style.strokeColor,
            style.fillColor,
            style.strokeWidth,
            style.pointRadius,
        )
    }

    fun build(
        state: KMLFeatureState,
        layerStyle: KMLTileRenderer.LayerStyle,
        styleProvider: KMLStyleProviderInterface,
    ): RenderFeature {
        val source =
            KMLFeature(
                id = state.id,
                geometry = state.geometry,
                properties = state.properties,
                strokeColor = state.strokeColor,
                fillColor = state.fillColor,
                strokeWidth = state.strokeWidth,
                pointRadius = state.pointRadius,
                visible = state.visible,
            )
        val style = styleProvider.getStyle(source, layerStyle)
        return buildRenderFeatureFromStyle(
            source,
            source.geometry,
            style.strokeColor,
            style.fillColor,
            style.strokeWidth,
            style.pointRadius,
        )
    }

    private fun buildRenderFeatureFromStyle(
        source: KMLFeature,
        geometry: KMLGeometry,
        strokeColor: Int,
        fillColor: Int,
        strokeWidth: Float,
        pointRadius: Float,
    ): RenderFeature {
        val fillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = fillColor
            }
        val strokePaint =
            if (android.graphics.Color.alpha(strokeColor) > 0 && strokeWidth > 0f) {
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = strokeColor
                    this.strokeWidth = strokeWidth
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                }
            } else {
                null
            }

        val worldGeometry = KMLWorld.toWorldGeometry(geometry)
        val bounds = KMLWorld.computeBounds(worldGeometry)
        // Strip the geometry from source: worldGeometry already holds all coordinates in world
        // space for rendering and hit-testing. Keeping lat/lon coords here doubles memory usage,
        // which causes OOM on large datasets with multiple renderers.
        return RenderFeature(
            source = source.copy(geometry = KMLGeometry.Empty),
            worldGeometry = worldGeometry,
            bounds = bounds,
            fillPaint = fillPaint,
            strokePaint = strokePaint,
            pointRadius = pointRadius,
        )
    }
}
