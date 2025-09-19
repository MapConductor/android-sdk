package com.mapconductor.core.marker

import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

interface MarkerCapable {
    suspend fun compositionMarkers(data: List<MarkerState>)

    suspend fun updateMarker(state: MarkerState)

    fun setOnMarkerDragStart(listener: OnMarkerEventHandler?)

    fun setOnMarkerDrag(listener: OnMarkerEventHandler?)

    fun setOnMarkerDragEnd(listener: OnMarkerEventHandler?)

    fun setOnMarkerAnimateStart(listener: OnMarkerEventHandler?)

    fun setOnMarkerAnimateEnd(listener: OnMarkerEventHandler?)

    fun setOnMarkerClickListener(listener: OnMarkerEventHandler?)

    fun hasMarker(state: MarkerState): Boolean
}

interface MarkerOverlayRenderer<ActualMarker> {
    var animateStartListener: OnMarkerEventHandler?
    var animateEndListener: OnMarkerEventHandler?

    interface AddParams {
        val state: MarkerState
        val bitmapIcon: BitmapIcon
    }

    interface ChangeParams<ActualMarker> {
        val current: MarkerEntity<ActualMarker>
        val bitmapIcon: BitmapIcon
        val prev: MarkerEntity<ActualMarker>
    }

    suspend fun onAdd(data: List<AddParams>): List<ActualMarker?>

    suspend fun onChange(data: List<ChangeParams<ActualMarker>>): List<ActualMarker?>

    suspend fun onRemove(data: List<MarkerEntity<ActualMarker>>)

    suspend fun onAnimate(entity: MarkerEntity<ActualMarker>)

    suspend fun onPostProcess()
}

abstract class AbstractMarkerController<ActualMarker>(
    val markerManager: MarkerManager<ActualMarker>,
    open val renderer: MarkerOverlayRenderer<ActualMarker>,
    override var clickListener: OnMarkerEventHandler? = null,
    open val renderingStrategy: MarkerRenderingStrategy<ActualMarker>? = null,
) : OverlayController<
        MarkerState,
        MarkerEntity<ActualMarker>,
        MarkerState,
    > {
    override val zIndex: Int = 10
    val semaphore = Semaphore(1)
    private val defaultIcon = DefaultIcon().toBitmapIcon()

    var dragStartListener: ((MarkerState) -> Unit)? = null
    var dragListener: ((MarkerState) -> Unit)? = null
    var dragEndListener: ((MarkerState) -> Unit)? = null
    private var mapCameraPosition: MapCameraPosition? = null
    private val worldBounds =
        GeoRectBounds(
            southWest = GeoPoint(90.0, 180.0),
            northEast = GeoPoint(-90.0, -180.0),
        )

    protected fun setDraggingState(
        markerState: MarkerState,
        dragging: Boolean,
    ) {
        // Since this "isDragging" property is internal accessor,
        // childViewControllers must call this method instead of "isDragging = true/false".
        markerState.isDragging = dragging
    }

    override suspend fun add(data: List<MarkerState>) {
        renderingStrategy?.let { strategy ->
            mapCameraPosition?.visibleRegion?.bounds?.let { bounds ->
                val processed =
                    strategy.onAdd(
                        data = data,
                        viewport = bounds,
                        renderer = renderer,
                    )
            }
            return
        }

        semaphore.withPermit {
            // Register all markers to the manager first
            val previous = markerManager.allEntities().map { it.state.id }.toMutableSet()
            val markersToRender = mutableListOf<MarkerEntity<ActualMarker>>()

            data.forEach { state ->
                if (previous.contains(state.id)) {
                    // Update existing entity
//                    val prevEntity = markerManager.getEntity(state.id)!!
//                    val entity =
//                        MarkerEntityImpl(
//                            state = state,
//                            marker = prevEntity.marker,
//                            isRendered = prevEntity.isRendered,
//                        )
//                    markerManager.updateEntity(entity)
//                    markersToRender.add(entity)
                    previous.remove(state.id)
                } else {
                    // Register new entity without rendering
                    val entity =
                        MarkerEntityImpl<ActualMarker>(
                            marker = null,
                            state = state,
                            isRendered = false,
                        )
                    markerManager.registerEntity(entity)
                    markersToRender.add(entity)
                }
            }

            if (markersToRender.isNotEmpty()) {
                val addParams =
                    markersToRender.map { entity ->
                        object : MarkerOverlayRenderer.AddParams {
                            override val state: MarkerState = entity.state
                            override val bitmapIcon: BitmapIcon =
                                entity.state.icon?.toBitmapIcon() ?: defaultIcon
                        }
                    }

                val actualMarkers = renderer.onAdd(addParams)
                actualMarkers.forEachIndexed { index, actualMarker ->
                    actualMarker?.let {
                        markersToRender[index].marker = it
                        markersToRender[index].isRendered = true
                        markersToRender[index].visible = true
                    }
                }

                markersToRender.forEach { entity ->
                    entity.state.getAnimation()?.let {
                        renderer.onAnimate(entity)
                    }
                }
                renderer.onPostProcess()
            }
        }
    }

    override suspend fun update(state: MarkerState) {
        // Fast path: Check entity existence without semaphore to avoid blocking during initial marker addition
        if (!markerManager.hasEntity(state.id)) return

        // Always update the entity in the manager
        val prevEntity = markerManager.getEntity(state.id) ?: return
        val currentFinger = state.fingerPrint()
        val prevFinger = prevEntity.fingerPrint
        if (currentFinger == prevFinger) {
            return
        }

        // Update the entity in manager
        val entity =
            MarkerEntityImpl(
                marker = prevEntity.marker,
                state = state,
                isRendered = prevEntity.isRendered,
            )
        markerManager.updateEntity(entity)

        renderingStrategy?.let { strategy ->
            mapCameraPosition?.visibleRegion?.bounds?.let { bounds ->
                val processed =
                    strategy.onUpdate(
                        state = state,
                        viewport = bounds,
                        renderer = renderer,
                    )
                if (processed) {
                    return
                }
            } ?: return
        }

        // Simple fallback: update marker immediately if it's already rendered
        if (prevEntity.isRendered) {
            semaphore.withPermit {
                val marker = prevEntity.marker
                val defaultIcon = DefaultIcon()
                val markerIcon = state.icon ?: defaultIcon

                val renderEntity =
                    MarkerEntityImpl(
                        marker = marker,
                        state = state,
                        isRendered = true,
                    )
                val markerParams =
                    object : MarkerOverlayRenderer.ChangeParams<ActualMarker> {
                        override val current: MarkerEntity<ActualMarker> = renderEntity
                        override val bitmapIcon: BitmapIcon = markerIcon.toBitmapIcon()
                        override val prev: MarkerEntity<ActualMarker> = prevEntity
                    }
                val markers = renderer.onChange(listOf(markerParams))

                markers[0]?.let {
                    val finalEntity =
                        MarkerEntityImpl<ActualMarker>(
                            marker = it,
                            state = state,
                            isRendered = true,
                        )
                    markerManager.updateEntity(finalEntity)

                    // Execute the animation property
                    if (prevFinger.animation != currentFinger.animation) {
                        state.getAnimation()?.let {
                            renderer.onAnimate(finalEntity)
                        }
                    }
                }
            }
        }
    }

    override suspend fun clear() {
        semaphore.withPermit {
            val entities: List<MarkerEntity<ActualMarker>> = markerManager.allEntities()
            renderer.onRemove(entities)
            markerManager.clear()
        }
    }

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        this.mapCameraPosition = mapCameraPosition
        renderingStrategy?.onCameraChanged(mapCameraPosition, renderer)
    }

    /**
     * Properly cleanup native resources when disposing of the controller
     * IMPORTANT: Call this when switching map providers or disposing the map
     */
    override fun destroy() {
        markerManager.destroy()
    }
}
