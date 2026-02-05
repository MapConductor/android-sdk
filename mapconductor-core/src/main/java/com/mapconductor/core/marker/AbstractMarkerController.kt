package com.mapconductor.core.marker

import com.mapconductor.core.controller.OverlayControllerInterface
import com.mapconductor.core.map.MapCameraPosition
import android.util.Log
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.yield

abstract class AbstractMarkerController<ActualMarker>(
    val markerManager: MarkerManager<ActualMarker>,
    renderer: MarkerOverlayRendererInterface<ActualMarker>,
    override var clickListener: OnMarkerEventHandler? = null,
) : OverlayControllerInterface<
        MarkerState,
        MarkerEntityInterface<ActualMarker>,
        MarkerState,
    > {
    open val renderer: MarkerOverlayRendererInterface<ActualMarker> = renderer
    override val zIndex: Int = 10
    val semaphore = Semaphore(1)
    private val defaultMarkerIcon = DefaultMarkerIcon().toBitmapIcon()

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

    protected fun setDraggingState(
        markerState: MarkerState,
        dragging: Boolean,
    ) {
        // Since this "isDragging" property is internal accessor,
        // childViewControllers must call this method instead of "isDragging = true/false".
    }

    override suspend fun add(data: List<MarkerState>) {
        semaphore.withPermit {
            Log.d("DEBUG", "-------->add start")
            val modifiedEntities = mutableListOf<MarkerEntityInterface<ActualMarker>>()
            val previous = markerManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<MarkerOverlayRendererInterface.AddParamsInterface>()
            val updated = mutableListOf<MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker>>()
            val removed = mutableListOf<MarkerEntityInterface<ActualMarker>>()

            data.forEach { state ->

                if (previous.contains(state.id)) {
                    val prevEntity = markerManager.getEntity(state.id)!!
                    val markerIcon = state.icon?.toBitmapIcon() ?: defaultMarkerIcon

                    updated.add(
                        object : MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker> {
                            override val current: MarkerEntityInterface<ActualMarker> =
                                MarkerEntity(
                                    state = state,
                                    marker = prevEntity.marker,
                                    isRendered = true,
                                )
                            override val bitmapIcon: BitmapIcon = markerIcon
                            override val prev: MarkerEntityInterface<ActualMarker> = prevEntity
                        },
                    )
                    previous.remove(state.id)
                } else {
                    added.add(
                        object : MarkerOverlayRendererInterface.AddParamsInterface {
                            override val state: MarkerState = state
                            override val bitmapIcon: BitmapIcon =
                                state.icon?.toBitmapIcon() ?: defaultMarkerIcon
                        },
                    )
                    previous.remove(state.id)
                }
            }

            markerManager.lock()

            previous.forEach { remainId ->
                markerManager.removeEntity(remainId)?.let { removedEntity ->
                    removed.add(removedEntity)
                }
            }

            // Remove markers
            if (removed.isNotEmpty()) {
                renderer.onRemove(removed)
                // Give the UI thread a chance to breathe when removing many markers.
                if (removed.size >= MARKER_RENDER_BATCH_SIZE) {
                    yield()
                }
            }

            // Add new markers
            if (added.isNotEmpty()) {
                added.chunked(MARKER_RENDER_BATCH_SIZE).forEach { batch ->
                    val actualMarkers: List<ActualMarker?> = renderer.onAdd(batch)
                    actualMarkers.forEachIndexed { index, actualMarker ->
                        actualMarker?.let {
                            val entity =
                                MarkerEntity<ActualMarker>(
                                    marker = actualMarker,
                                    state = batch[index].state,
                                    isRendered = true,
                                )
                            markerManager.registerEntity(entity)
                            modifiedEntities.add(entity)
                        }
                    }
                    yield()
                }
            }

            // Update changed markers
            if (updated.isNotEmpty()) {
                updated.chunked(MARKER_RENDER_BATCH_SIZE).forEach { batch ->
                    val actualMarkers: List<ActualMarker?> = renderer.onChange(batch)
                    actualMarkers.forEachIndexed { index, actualMarker ->
                        actualMarker?.let {
                            val params = batch[index]
                            val entity =
                                MarkerEntity<ActualMarker>(
                                    state = params.current.state,
                                    marker = actualMarker,
                                    isRendered = true,
                                )
                            markerManager.registerEntity(entity)
                        }
                    }
                    yield()
                }
            }

            markerManager.unlock()

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
            MarkerEntity(
                marker = prevEntity.marker,
                state = state,
                isRendered = prevEntity.isRendered,
            )

        markerManager.lock()
        markerManager.updateEntity(entity)

        // Simple fallback: update marker immediately if it's already rendered
        semaphore.withPermit {
            val marker = prevEntity.marker
            val defaultMarkerIcon = DefaultMarkerIcon()
            val markerIcon = state.icon ?: defaultMarkerIcon

            val renderEntity =
                MarkerEntity(
                    marker = marker,
                    state = state,
                    isRendered = true,
                )
            val markerParams =
                object : MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker> {
                    override val current: MarkerEntityInterface<ActualMarker> = renderEntity
                    override val bitmapIcon: BitmapIcon = markerIcon.toBitmapIcon()
                    override val prev: MarkerEntityInterface<ActualMarker> = prevEntity
                }
            val markers = renderer.onChange(listOf(markerParams))

            if (markers.size == 1) {
                markers[0]?.let {
                    val finalEntity =
                        MarkerEntity<ActualMarker>(
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
            renderer.onPostProcess()
        }
        markerManager.unlock()
    }

    override suspend fun clear() {
        semaphore.withPermit {
            val entities: List<MarkerEntityInterface<ActualMarker>> = markerManager.allEntities()
            renderer.onRemove(entities)
            markerManager.clear()
        }
    }

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        // No-op for default marker flow.
    }

    /**
     * Properly cleanup native resources when disposing of the controller
     * IMPORTANT: Call this when switching map providers or disposing the map
     */
    override fun destroy() {
        markerManager.destroy()
    }
}

private const val MARKER_RENDER_BATCH_SIZE = 500
