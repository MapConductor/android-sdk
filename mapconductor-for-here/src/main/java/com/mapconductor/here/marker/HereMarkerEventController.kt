package com.mapconductor.here.marker

import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.here.HereActualMarker

internal interface HereMarkerEventController {
    fun find(position: GeoPointImpl): MarkerEntity<HereActualMarker>?

    fun getSelectedMarker(): MarkerEntity<HereActualMarker>?

    fun setSelectedMarker(entity: MarkerEntity<HereActualMarker>?)

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

internal class DefaultHereMarkerEventController(
    private val controller: HereMarkerController,
) : HereMarkerEventController {
    override fun find(position: GeoPointImpl): MarkerEntity<HereActualMarker>? = controller.find(position)

    override fun getSelectedMarker(): MarkerEntity<HereActualMarker>? = controller.selectedMarker

    override fun setSelectedMarker(entity: MarkerEntity<HereActualMarker>?) {
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

internal class StrategyHereMarkerEventController(
    private val controller: StrategyMarkerController<HereActualMarker>,
) : HereMarkerEventController {
    private var selectedMarker: MarkerEntity<HereActualMarker>? = null

    override fun find(position: GeoPointImpl): MarkerEntity<HereActualMarker>? = controller.find(position)

    override fun getSelectedMarker(): MarkerEntity<HereActualMarker>? = selectedMarker

    override fun setSelectedMarker(entity: MarkerEntity<HereActualMarker>?) {
        selectedMarker = entity
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
