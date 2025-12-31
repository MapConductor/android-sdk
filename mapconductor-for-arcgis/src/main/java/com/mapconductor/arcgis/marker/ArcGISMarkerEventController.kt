package com.mapconductor.arcgis.marker

import com.arcgismaps.geometry.Point
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.marker.StrategyMarkerController

internal interface ArcGISMarkerEventController {
    fun find(position: GeoPointImpl): MarkerEntity<ArcGISActualMarker>?

    fun getSelectedState(): MarkerState?

    fun startDrag(entity: MarkerEntity<ArcGISActualMarker>)

    fun updateDrag(
        point: Point,
        position: GeoPointImpl,
    )

    fun endDrag(
        point: Point,
        position: GeoPointImpl,
    )

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

internal class DefaultArcGISMarkerEventController(
    private val controller: ArcGISMarkerController,
) : ArcGISMarkerEventController {
    override fun find(position: GeoPointImpl): MarkerEntity<ArcGISActualMarker>? = controller.find(position)

    override fun getSelectedState(): MarkerState? = controller.selectedMarker?.state

    override fun startDrag(entity: MarkerEntity<ArcGISActualMarker>) {
        val graphic = entity.marker ?: return
        controller.selectedMarker =
            SelectedMarker(
                state = entity.state,
                graphic = graphic,
            )
    }

    override fun updateDrag(
        point: Point,
        position: GeoPointImpl,
    ) {
        controller.selectedMarker?.also {
            it.graphic.geometry = point
            it.state.position = position
        }
    }

    override fun endDrag(
        point: Point,
        position: GeoPointImpl,
    ) {
        controller.selectedMarker?.also {
            it.graphic.geometry = point
            it.state.position = position
            controller.selectedMarker = null
        }
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

internal class StrategyArcGISMarkerEventController(
    private val controller: StrategyMarkerController<ArcGISActualMarker>,
) : ArcGISMarkerEventController {
    private var selectedMarker: MarkerEntity<ArcGISActualMarker>? = null

    override fun find(position: GeoPointImpl): MarkerEntity<ArcGISActualMarker>? = controller.find(position)

    override fun getSelectedState(): MarkerState? = selectedMarker?.state

    override fun startDrag(entity: MarkerEntity<ArcGISActualMarker>) {
        selectedMarker = entity
    }

    override fun updateDrag(
        point: Point,
        position: GeoPointImpl,
    ) {
        selectedMarker?.also { entity ->
            entity.marker?.geometry = point
            entity.state.position = position
        }
    }

    override fun endDrag(
        point: Point,
        position: GeoPointImpl,
    ) {
        selectedMarker?.also { entity ->
            entity.marker?.geometry = point
            entity.state.position = position
        }
        selectedMarker = null
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
