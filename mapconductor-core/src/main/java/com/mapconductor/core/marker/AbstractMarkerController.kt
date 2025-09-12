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
    private val renderingStrategy: MarkerRenderingStrategy<ActualMarker>? = null,
) : OverlayController<
        MarkerState,
        MarkerEntity<ActualMarker>,
        MarkerState,
    > {
    override val zIndex: Int = 10
    val semaphore = Semaphore(1)

    // Lazy initialization of the default strategy to use the actual semaphore instance
    protected open val actualRenderingStrategy: MarkerRenderingStrategy<ActualMarker> by lazy {
        renderingStrategy ?: DefaultMarkerRenderingStrategy(semaphore = semaphore)
    }

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
        semaphore.withPermit {
            val defaultIcon = DefaultIcon()
            val defaultIconBitmapIcon = defaultIcon.toBitmapIcon()
            val modifiedEntities = mutableListOf<MarkerEntity<ActualMarker>>()
            val previous = markerManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<MarkerOverlayRenderer.AddParams>()
            val updated = mutableListOf<MarkerOverlayRenderer.ChangeParams<ActualMarker>>()
            val removed = mutableListOf<MarkerEntity<ActualMarker>>()
            val viewportBounds = mapCameraPosition?.visibleRegion?.bounds ?: worldBounds

            data.forEach { state ->
                val isInViewport = viewportBounds.contains(state.position)

                if (previous.contains(state.id)) {
                    val prevEntity = markerManager.getEntity(state.id)!!
                    val markerIcon = state.icon ?: defaultIcon

                    // Only add to update list if marker is in viewport
                    if (isInViewport) {
                        updated.add(
                            object : MarkerOverlayRenderer.ChangeParams<ActualMarker> {
                                override val current: MarkerEntity<ActualMarker> =
                                    MarkerEntityImpl(
                                        state = state,
                                        marker = prevEntity.marker,
                                        isRendered = true,
                                    )
                                override val bitmapIcon: BitmapIcon = markerIcon.toBitmapIcon()
                                override val prev: MarkerEntity<ActualMarker> = prevEntity
                            },
                        )
                    } else {
                        // Register entity without rendering for markers outside viewport
                        val entity =
                            MarkerEntityImpl(
                                state = state,
                                marker = prevEntity.marker,
                                isRendered = false,
                            )
                        markerManager.registerEntity(entity)
                    }
                    previous.remove(state.id)
                } else {
                    // Only add to render list if marker is in viewport
                    if (isInViewport) {
                        added.add(
                            object : MarkerOverlayRenderer.AddParams {
                                override val state: MarkerState = state
                                override val bitmapIcon: BitmapIcon =
                                    state.icon?.toBitmapIcon() ?: defaultIconBitmapIcon
                            },
                        )
                    } else {
                        // Register entity without rendering for new markers outside viewport
                        val entity =
                            MarkerEntityImpl<ActualMarker>(
                                marker = null,
                                state = state,
                                isRendered = false,
                            )
                        markerManager.registerEntity(entity)
                    }
                    previous.remove(state.id)
                }
            }

            previous.forEach { remainId ->
                markerManager.removeEntity(remainId)?.let { removedEntity ->
                    removed.add(removedEntity)
                }
            }

            // Remove markers
            if (removed.isNotEmpty()) {
                renderer.onRemove(removed)
            }

            // Add new markers
            if (added.isNotEmpty()) {
                val actualMarkers: List<ActualMarker?> = renderer.onAdd(added)
                actualMarkers.forEachIndexed { index, actualMarker ->
                    actualMarker?.let {
                        val entity =
                            MarkerEntityImpl<ActualMarker>(
                                marker = actualMarker,
                                state = added[index].state,
                                isRendered = true,
                            )
                        markerManager.registerEntity(entity)
                        modifiedEntities.add(entity)
                    }
                }
            }

            // Update changed markers
            if (updated.isNotEmpty()) {
                val actualMarkers: List<ActualMarker?> = renderer.onChange(updated)

                actualMarkers.forEachIndexed { index, actualMarker ->
                    actualMarker?.let {
                        val params = updated[index]
                        val entity =
                            MarkerEntityImpl<ActualMarker>(
                                state = params.current.state,
                                marker = actualMarker,
                                isRendered = true,
                            )
                        markerManager.registerEntity(entity)
                    }
                }
            }
            modifiedEntities.forEach { entity ->
                entity.state.getAnimation()?.let {
                    renderer.onAnimate(entity)
                }
            }
            renderer.onPostProcess()
        }
    }

    override suspend fun update(state: MarkerState) {
        // Fast path: Check entity existence without semaphore to avoid blocking during initial marker addition
        if (!markerManager.hasEntity(state.id)) return

        semaphore.withPermit {
            val prevEntity = markerManager.getEntity(state.id) ?: return
            val currentFinger = state.fingerPrint()
            val prevFinger = prevEntity.fingerPrint
            if (currentFinger == prevFinger) {
                return
            }

            val marker = prevEntity.marker
            val defaultIcon = DefaultIcon()
            val markerIcon = state.icon ?: defaultIcon

            val entity =
                MarkerEntityImpl(
                    marker = marker,
                    state = state,
                )
            val markerParams =
                object : MarkerOverlayRenderer.ChangeParams<ActualMarker> {
                    override val current: MarkerEntity<ActualMarker> = entity
                    override val bitmapIcon: BitmapIcon = markerIcon.toBitmapIcon()
                    override val prev: MarkerEntity<ActualMarker> = prevEntity
                }
            val markers = renderer.onChange(listOf(markerParams))

            markers[0]?.let {
                val entity =
                    MarkerEntityImpl<ActualMarker>(
                        marker = it,
                        state = state,
                    )
                markerManager.registerEntity(entity)

                // Execute the animation property
                if (prevFinger.animation != currentFinger.animation) {
                    state.getAnimation()?.let {
                        renderer.onAnimate(entity)
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
        actualRenderingStrategy.onCameraChanged(mapCameraPosition, markerManager, renderer)
    }
    
    /**
     * Properly cleanup native resources when disposing of the controller
     * IMPORTANT: Call this when switching map providers or disposing the map
     */
    override fun destroy() {
        markerManager.destroy()
    }
}
