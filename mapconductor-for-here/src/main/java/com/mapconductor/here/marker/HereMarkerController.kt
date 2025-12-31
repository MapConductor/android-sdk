package com.mapconductor.here.marker

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.here.HereActualMarker
import com.mapconductor.here.HereViewHolder
import com.mapconductor.settings.Settings

class HereMarkerController private constructor(
    markerManager: MarkerManager<HereActualMarker>,
    override val renderer: HereMarkerRenderer,
) : AbstractMarkerController<HereActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
    ) {
    private var internalSelectedMarker: MarkerEntity<HereActualMarker>? = null

    internal var selectedMarker: MarkerEntity<HereActualMarker>?
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

    override fun find(position: GeoPoint): MarkerEntity<HereActualMarker>? {
        val nearest = markerManager.findNearest(position) ?: return null

        val touchScreen = renderer.holder.toScreenOffset(position) ?: return null
        val markerScreen = renderer.holder.toScreenOffset(nearest.state.position) ?: return null

        val tolerancePx =
            Settings.Default.tapTolerance.value
                .toDouble() *
                ResourceProvider.getDensity().toDouble()

        val icon = nearest.state.icon

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

        val baseSizePx = ResourceProvider.dpToPxForBitmap(icon.iconSize).toDouble()
        val iconWidthPx = baseSizePx * icon.scale.toDouble()
        val iconHeightPx = baseSizePx * icon.scale.toDouble()

        val anchorX = icon.anchor.x.toDouble()
        val anchorY = icon.anchor.y.toDouble()

        val dx = (touchScreen.x - markerScreen.x).toDouble()
        val dy = (touchScreen.y - markerScreen.y).toDouble()

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
        private const val ZOOM_ADJUST_VALUE = 0.1 // バイナリテストで確定

        fun create(
            holder: HereViewHolder,
        ): HereMarkerController {
            val renderer =
                HereMarkerRenderer(
                    holder = holder,
                )
            val markerManager = MarkerManager.defaultManager<HereActualMarker>()

            val controller =
                HereMarkerController(
                    markerManager = markerManager,
                    renderer = renderer,
                )
            return controller
        }
    }
}
