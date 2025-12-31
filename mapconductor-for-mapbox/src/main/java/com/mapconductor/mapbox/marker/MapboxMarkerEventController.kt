package com.mapconductor.mapbox.marker

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.mapbox.MapboxActualMarker

internal interface MapboxMarkerEventController {
    val renderer: MapboxMarkerOverlayRenderer

    fun find(position: GeoPoint): MarkerEntity<MapboxActualMarker>?

    fun getSelectedMarker(): MarkerEntity<MapboxActualMarker>?

    fun setSelectedMarker(entity: MarkerEntity<MapboxActualMarker>?)

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

internal class DefaultMapboxMarkerEventController(
    private val controller: MapboxMarkerController,
) : MapboxMarkerEventController {
    override val renderer: MapboxMarkerOverlayRenderer = controller.renderer

    override fun find(position: GeoPoint): MarkerEntity<MapboxActualMarker>? = controller.find(position)

    override fun getSelectedMarker(): MarkerEntity<MapboxActualMarker>? = controller.selectedMarker

    override fun setSelectedMarker(entity: MarkerEntity<MapboxActualMarker>?) {
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

internal class StrategyMapboxMarkerEventController(
    private val controller: StrategyMarkerController<MapboxActualMarker>,
    override val renderer: MapboxMarkerOverlayRenderer,
) : MapboxMarkerEventController {
    private var selectedMarker: MarkerEntity<MapboxActualMarker>? = null

    override fun find(position: GeoPoint): MarkerEntity<MapboxActualMarker>? = controller.find(position)

    override fun getSelectedMarker(): MarkerEntity<MapboxActualMarker>? = selectedMarker

    override fun setSelectedMarker(entity: MarkerEntity<MapboxActualMarker>?) {
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
