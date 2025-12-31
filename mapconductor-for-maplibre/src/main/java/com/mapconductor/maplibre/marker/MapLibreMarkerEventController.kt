package com.mapconductor.maplibre.marker

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.maplibre.MapLibreActualMarker

internal interface MapLibreMarkerEventController {
    val renderer: MapLibreMarkerOverlayRenderer

    fun find(position: GeoPoint): MarkerEntity<MapLibreActualMarker>?

    fun getSelectedMarker(): MarkerEntity<MapLibreActualMarker>?

    fun setSelectedMarker(entity: MarkerEntity<MapLibreActualMarker>?)

    fun dispatchClick(state: MarkerState)

    fun dispatchDragStart(state: MarkerState)

    fun dispatchDrag(state: MarkerState)

    fun dispatchDragEnd(state: MarkerState)

    fun setClickListener(listener: OnMarkerEventHandler?)

    fun setDragStartListener(listener: OnMarkerEventHandler?)

    fun setDragListener(listener: OnMarkerEventHandler?)

    fun setDragEndListener(listener: OnMarkerEventHandler?)

    fun setAnimateStartListener(listener: OnMarkerEventHandler?)

    fun setAnimateEndListener(listener: OnMarkerEventHandler?)
}

internal class DefaultMapLibreMarkerEventController(
    private val controller: MapLibreMarkerController,
) : MapLibreMarkerEventController {
    override val renderer: MapLibreMarkerOverlayRenderer = controller.renderer

    override fun find(position: GeoPoint): MarkerEntity<MapLibreActualMarker>? = controller.find(position)

    override fun getSelectedMarker(): MarkerEntity<MapLibreActualMarker>? = controller.selectedMarker

    override fun setSelectedMarker(entity: MarkerEntity<MapLibreActualMarker>?) {
        controller.selectedMarker = entity
    }

    override fun dispatchClick(state: MarkerState) = controller.dispatchClick(state)

    override fun dispatchDragStart(state: MarkerState) = controller.dispatchDragStart(state)

    override fun dispatchDrag(state: MarkerState) = controller.dispatchDrag(state)

    override fun dispatchDragEnd(state: MarkerState) = controller.dispatchDragEnd(state)

    override fun setClickListener(listener: OnMarkerEventHandler?) {
        controller.clickListener = listener
    }

    override fun setDragStartListener(listener: OnMarkerEventHandler?) {
        controller.dragStartListener = listener
    }

    override fun setDragListener(listener: OnMarkerEventHandler?) {
        controller.dragListener = listener
    }

    override fun setDragEndListener(listener: OnMarkerEventHandler?) {
        controller.dragEndListener = listener
    }

    override fun setAnimateStartListener(listener: OnMarkerEventHandler?) {
        controller.animateStartListener = listener
    }

    override fun setAnimateEndListener(listener: OnMarkerEventHandler?) {
        controller.animateEndListener = listener
    }
}

internal class StrategyMapLibreMarkerEventController(
    private val controller: StrategyMarkerController<MapLibreActualMarker>,
    override val renderer: MapLibreMarkerOverlayRenderer,
) : MapLibreMarkerEventController {
    private var selectedMarker: MarkerEntity<MapLibreActualMarker>? = null

    override fun find(position: GeoPoint): MarkerEntity<MapLibreActualMarker>? = controller.find(position)

    override fun getSelectedMarker(): MarkerEntity<MapLibreActualMarker>? = selectedMarker

    override fun setSelectedMarker(entity: MarkerEntity<MapLibreActualMarker>?) {
        if (entity == null) {
            selectedMarker?.let {
                renderer.dragLayer.updatePosition(GeoPointImpl.from(it.state.position))
                renderer.dragLayer.selected = null
                renderer.drawDragLayer()
                controller.markerManager.registerEntity(it)
                renderer.redraw()
            }
            selectedMarker = null
            return
        }
        selectedMarker = entity
        controller.markerManager.removeEntity(entity.state.id)
        renderer.dragLayer.selected = entity
        renderer.dragLayer.updatePosition(GeoPointImpl.from(entity.state.position))
        renderer.redraw()
        renderer.drawDragLayer()
    }

    override fun dispatchClick(state: MarkerState) = controller.dispatchClick(state)

    override fun dispatchDragStart(state: MarkerState) = controller.dispatchDragStart(state)

    override fun dispatchDrag(state: MarkerState) = controller.dispatchDrag(state)

    override fun dispatchDragEnd(state: MarkerState) = controller.dispatchDragEnd(state)

    override fun setClickListener(listener: OnMarkerEventHandler?) {
        controller.clickListener = listener
    }

    override fun setDragStartListener(listener: OnMarkerEventHandler?) {
        controller.dragStartListener = listener
    }

    override fun setDragListener(listener: OnMarkerEventHandler?) {
        controller.dragListener = listener
    }

    override fun setDragEndListener(listener: OnMarkerEventHandler?) {
        controller.dragEndListener = listener
    }

    override fun setAnimateStartListener(listener: OnMarkerEventHandler?) {
        controller.animateStartListener = listener
    }

    override fun setAnimateEndListener(listener: OnMarkerEventHandler?) {
        controller.animateEndListener = listener
    }
}
