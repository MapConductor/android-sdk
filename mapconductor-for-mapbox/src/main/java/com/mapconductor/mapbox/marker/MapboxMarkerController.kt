package com.mapconductor.mapbox.marker

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.settings.Settings

class MapboxMarkerController(
    override val renderer: MapboxMarkerOverlayRenderer,
) : AbstractMarkerController<MapboxActualMarker>(
        markerManager = renderer.markerManager,
        renderer = renderer,
    ) {
    private var internalSelectedMarker: MarkerEntityInterface<MapboxActualMarker>? = null

    internal var selectedMarker: MarkerEntityInterface<MapboxActualMarker>?
        set(value) {
            if (value == null) {
                internalSelectedMarker?.let {
                    renderer.dragLayer.updatePosition(GeoPoint.from(it.state.position))
                    // Restore the recomposition for the position property
                    setDraggingState(it.state, false)
                    // Clear drag layer selection to avoid duplicate icon after drop
                    renderer.dragLayer.selected = null
                    renderer.drawDragLayer()
                    markerManager.registerEntity(it)
                    renderer.redraw()
                }
                internalSelectedMarker = null
                return
            }
            internalSelectedMarker = value
            markerManager.removeEntity(value.state.id)
            // Suppress the recomposition for the position property
            setDraggingState(value.state, true)
            renderer.dragLayer.selected = value
            renderer.dragLayer.updatePosition(GeoPoint.from(value.state.position))
            renderer.redraw()
            renderer.drawDragLayer()
        }
        get() = internalSelectedMarker

    override fun find(position: GeoPointInterface): MarkerEntityInterface<MapboxActualMarker>? {
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
}
