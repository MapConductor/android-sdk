package com.mapconductor.arcgis.marker

import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.SurfacePlacement
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.getZoomLevel
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.WGS84Geodesic.computeDistanceBetween
import com.mapconductor.settings.Settings

internal data class SelectedMarker(
    val state: MarkerState,
    val graphic: Graphic,
)

class ArcGISMarkerController private constructor(
    markerManager: MarkerManager<ArcGISActualMarker>,
    override val renderer: ArcGISMarkerRenderer,
    renderingStrategy: MarkerRenderingStrategy<ArcGISActualMarker>? = null,
) : AbstractMarkerController<ArcGISActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
        renderingStrategy = renderingStrategy,
    ) {
    private var internalSelectedMarker: SelectedMarker? = null

    internal var selectedMarker: SelectedMarker?
        set(value) {
            if (value == null) {
                internalSelectedMarker?.let {
                    // Restore the recomposition for the position property
                    setDraggingState(it.state, false)
                }
                return
            }
            internalSelectedMarker = value
            // Suppress the recomposition for the position property
            setDraggingState(value.state, true)
        }
        get() = internalSelectedMarker

    override fun find(position: GeoPoint): MarkerEntity<ArcGISActualMarker>? {
        val nearest = markerManager.findNearest(position) ?: return null

        // タップ位置とマーカー位置のスクリーン座標を取得
        val touchScreen = renderer.holder.toScreenOffset(position) ?: return null
        val markerScreen = renderer.holder.toScreenOffset(nearest.state.position) ?: return null

        // 画面上のタップ許容マージン（px）
        val tolerancePx =
            Settings.Default.tapTolerance.value.toDouble() *
                ResourceProvider.getDensity().toDouble()

        val icon = nearest.state.icon

        // アイコン情報がない場合は、タップ位置からの距離のみで判定（後方互換用のフォールバック）
        if (icon == null) {
            val dx = (touchScreen.x - markerScreen.x).toDouble()
            val dy = (touchScreen.y - markerScreen.y).toDouble()
            val distancePx = kotlin.math.hypot(dx, dy)
            return if (distancePx <= tolerancePx) {
                nearest
            } else {
                null
            }
        }

        // アイコンサイズ（Dp）とスケールから、実際のピクセルサイズを計算
        val baseSizePx = ResourceProvider.dpToPxForBitmap(icon.iconSize).toDouble()
        val iconWidthPx = baseSizePx * icon.scale.toDouble()
        val iconHeightPx = baseSizePx * icon.scale.toDouble()

        // アンカー（0,0=左上 ～ 1,1=右下）
        val anchorX = icon.anchor.x.toDouble()
        val anchorY = icon.anchor.y.toDouble()

        // タップ位置を、アンカー位置を原点としたオフセットに変換
        val dx = (touchScreen.x - markerScreen.x).toDouble()
        val dy = (touchScreen.y - markerScreen.y).toDouble()

        // アンカーを基準にしたアイコンの矩形 + タップ許容マージン
        val left = -anchorX * iconWidthPx - tolerancePx
        val right = (1.0 - anchorX) * iconWidthPx + tolerancePx
        val top = -anchorY * iconHeightPx - tolerancePx
        val bottom = (1.0 - anchorY) * iconHeightPx + tolerancePx

        return if (dx in left..right && dy in top..bottom) {
            nearest
        } else {
            null
        }
    }

    companion object {
        fun create(
            holder: ArcGISMapViewHolder,
            renderingStrategy: MarkerRenderingStrategy<ArcGISActualMarker>? = null,
        ): ArcGISMarkerController {
            val markerLayer: GraphicsOverlay =
                GraphicsOverlay().apply {
                    sceneProperties.surfacePlacement = SurfacePlacement.Relative
                }

            val renderer =
                ArcGISMarkerRenderer(
                    markerLayer = markerLayer,
                    holder = holder,
                )

            val markerManager = renderingStrategy?.markerManager ?: MarkerManager.defaultManager()

            val controller =
                ArcGISMarkerController(
                    markerManager = markerManager,
                    renderer = renderer,
                    renderingStrategy = renderingStrategy,
                )
            return controller
        }
    }
}
