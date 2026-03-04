package com.mapconductor.core.marker

import com.mapconductor.core.controller.OverlayControllerInterface
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class StrategyMarkerController<ActualMarker>(
    private val strategy: MarkerRenderingStrategyInterface<ActualMarker>,
    private val renderer: MarkerOverlayRendererInterface<ActualMarker>,
    override var clickListener: OnMarkerEventHandler? = null,
) : OverlayControllerInterface<
        MarkerState,
        MarkerEntityInterface<ActualMarker>,
        MarkerState,
    > {
    val markerManager: MarkerManager<ActualMarker> = strategy.markerManager
    override val zIndex: Int = 10
    private var mapCameraPosition: MapCameraPosition? = null
    private val semaphore = Semaphore(1)
    private var pendingStates: List<MarkerState>? = null

    var dragStartListener: OnMarkerEventHandler? = null
    var dragListener: OnMarkerEventHandler? = null
    var dragEndListener: OnMarkerEventHandler? = null
    var animateStartListener: OnMarkerEventHandler? = null
    var animateEndListener: OnMarkerEventHandler? = null

    init {
        renderer.animateStartListener = { state -> dispatchAnimateStart(state) }
        renderer.animateEndListener = { state -> dispatchAnimateEnd(state) }
    }

    fun dispatchClick(state: MarkerState) {
        state.onClick?.invoke(state)
        clickListener?.invoke(state)
    }

    fun dispatchDragStart(state: MarkerState) {
        state.onDragStart?.invoke(state)
        dragStartListener?.invoke(state)
    }

    fun dispatchDrag(state: MarkerState) {
        state.onDrag?.invoke(state)
        dragListener?.invoke(state)
    }

    fun dispatchDragEnd(state: MarkerState) {
        state.onDragEnd?.invoke(state)
        dragEndListener?.invoke(state)
    }

    fun dispatchAnimateStart(state: MarkerState) {
        state.onAnimateStart?.invoke(state)
        animateStartListener?.invoke(state)
    }

    fun dispatchAnimateEnd(state: MarkerState) {
        state.onAnimateEnd?.invoke(state)
        animateEndListener?.invoke(state)
    }

    override suspend fun add(data: List<MarkerState>) {
        val bounds = mapCameraPosition?.visibleRegion?.bounds
        if (bounds == null) {
            pendingStates = data
            return
        }
        semaphore.withPermit {
            strategy.onAdd(
                data = data,
                viewport = bounds,
                renderer = renderer,
            )
        }
    }

    override suspend fun update(state: MarkerState) {
        val bounds = mapCameraPosition?.visibleRegion?.bounds ?: return
        semaphore.withPermit {
            strategy.onUpdate(
                state = state,
                viewport = bounds,
                renderer = renderer,
            )
        }
    }

    override suspend fun clear() {
        strategy.clear()
    }

    fun getEntity(id: String): MarkerEntityInterface<ActualMarker>? = strategy.markerManager.getEntity(id)

    override fun find(position: GeoPointInterface): MarkerEntityInterface<ActualMarker>? =
        strategy.markerManager
            .findNearest(position)

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        this.mapCameraPosition = mapCameraPosition
        semaphore.withPermit {
            strategy.onCameraChanged(mapCameraPosition, renderer)
        }
        val pending = pendingStates ?: return
        pendingStates = null
        add(pending)
    }

    override fun destroy() {
        strategy.clear()
    }
}
