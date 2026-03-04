package com.mapconductor.here.marker

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.core.marker.MarkerEventControllerInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.here.HereActualMarker

internal interface HereMarkerEventControllerInterface : MarkerEventControllerInterface<HereActualMarker> {
    fun find(position: GeoPoint): MarkerEntityInterface<HereActualMarker>?

    fun getSelectedMarker(): MarkerEntityInterface<HereActualMarker>?

    fun setSelectedMarker(entity: MarkerEntityInterface<HereActualMarker>?)

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
) : HereMarkerEventControllerInterface {
    override fun find(position: GeoPoint): MarkerEntityInterface<HereActualMarker>? = controller.find(position)

    override fun getSelectedMarker(): MarkerEntityInterface<HereActualMarker>? = controller.selectedMarker

    override fun setSelectedMarker(entity: MarkerEntityInterface<HereActualMarker>?) {
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
) : HereMarkerEventControllerInterface {
    private var selectedMarker: MarkerEntityInterface<HereActualMarker>? = null

    override fun find(position: GeoPoint): MarkerEntityInterface<HereActualMarker>? = controller.find(position)

    override fun getSelectedMarker(): MarkerEntityInterface<HereActualMarker>? = selectedMarker

    override fun setSelectedMarker(entity: MarkerEntityInterface<HereActualMarker>?) {
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
