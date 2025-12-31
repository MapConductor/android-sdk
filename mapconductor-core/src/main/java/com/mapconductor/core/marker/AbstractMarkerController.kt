package com.mapconductor.core.marker

import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPositionImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

abstract class AbstractMarkerController<ActualMarker>(
    val markerManager: MarkerManager<ActualMarker>,
    renderer: MarkerOverlayRenderer<ActualMarker>,
    override var clickListener: OnMarkerEventHandler? = null,
    open val renderingStrategy: MarkerRenderingStrategy<ActualMarker>? = null,
) : OverlayController<
        MarkerState,
        MarkerEntity<ActualMarker>,
        MarkerState,
    > {
    open val renderer: MarkerOverlayRenderer<ActualMarker> = renderer
    private val rendererRef: MarkerOverlayRenderer<ActualMarker> = renderer
    override val zIndex: Int = 10
    val semaphore = Semaphore(1)
    private val defaultIcon = DefaultIcon().toBitmapIcon()

    var dragStartListener: OnMarkerEventHandler? = null
    var dragListener: OnMarkerEventHandler? = null
    var dragEndListener: OnMarkerEventHandler? = null
    var animateStartListener: OnMarkerEventHandler? = null
    var animateEndListener: OnMarkerEventHandler? = null
    private var mapCameraPosition: MapCameraPositionImpl? = null

    // Timer-based debounce implementation (ArcGIS Flow-compatible)
    private val debounceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val debounceMutex = Mutex()
    private var pendingCameraPosition: MapCameraPositionImpl? = null
    private var debounceJob: kotlinx.coroutines.Job? = null

    init {
        rendererRef.animateStartListener = { state -> dispatchAnimateStart(state) }
        rendererRef.animateEndListener = { state -> dispatchAnimateEnd(state) }
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

    private suspend fun processCameraChangeDebounced(cameraPosition: MapCameraPositionImpl) {
        debounceMutex.withLock {
            // Store the latest camera position
            pendingCameraPosition = cameraPosition

            // Cancel previous debounce job
            debounceJob?.cancel()

            // Start new debounce job
            debounceJob =
                debounceScope.launch {
                    delay(100) // 100ms debounce delay

                    // Process the latest camera position
                    val latestPosition =
                        debounceMutex.withLock {
                            pendingCameraPosition
                        }

                    latestPosition?.let { position ->
                        this@AbstractMarkerController.mapCameraPosition = position
                        renderingStrategy?.onCameraChanged(position, renderer)
                    }
                }
        }
    }

    private val worldBounds =
        GeoRectBounds(
            southWest = GeoPointImpl(90.0, 180.0),
            northEast = GeoPointImpl(-90.0, -180.0),
        )

    protected fun setDraggingState(
        markerState: MarkerState,
        dragging: Boolean,
    ) {
        // Since this "isDragging" property is internal accessor,
        // childViewControllers must call this method instead of "isDragging = true/false".
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
            val modifiedEntities = mutableListOf<MarkerEntity<ActualMarker>>()
            val previous = markerManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<MarkerOverlayRenderer.AddParams>()
            val updated = mutableListOf<MarkerOverlayRenderer.ChangeParams<ActualMarker>>()
            val removed = mutableListOf<MarkerEntity<ActualMarker>>()

            data.forEach { state ->

                if (previous.contains(state.id)) {
                    val prevEntity = markerManager.getEntity(state.id)!!
                    val markerIcon = state.icon?.toBitmapIcon() ?: defaultIcon

                    updated.add(
                        object : MarkerOverlayRenderer.ChangeParams<ActualMarker> {
                            override val current: MarkerEntity<ActualMarker> =
                                MarkerEntityImpl(
                                    state = state,
                                    marker = prevEntity.marker,
                                    isRendered = true,
                                )
                            override val bitmapIcon: BitmapIcon = markerIcon
                            override val prev: MarkerEntity<ActualMarker> = prevEntity
                        },
                    )
                    previous.remove(state.id)
                } else {
                    added.add(
                        object : MarkerOverlayRenderer.AddParams {
                            override val state: MarkerState = state
                            override val bitmapIcon: BitmapIcon =
                                state.icon?.toBitmapIcon() ?: defaultIcon
                        },
                    )
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

            if (markers.size == 1) {
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

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPositionImpl) {
        if (this.mapCameraPosition == null) {
            // Set the initial camera position
            this.mapCameraPosition = mapCameraPosition
        } else {
            // Use timer-based debounce instead of Flow to avoid ArcGIS SDK conflicts
            processCameraChangeDebounced(mapCameraPosition)
        }
    }

    /**
     * Properly cleanup native resources when disposing of the controller
     * IMPORTANT: Call this when switching map providers or disposing the map
     */
    override fun destroy() {
        debounceJob?.cancel()
        debounceScope.cancel()
        markerManager.destroy()
    }
}
