package com.mapconductor.core.marker

/**
 * Shared ingestion logic for marker controllers.
 *
 * This engine diffs incoming [MarkerState] lists against the current [MarkerManager] state,
 * drives the [MarkerOverlayRendererInterface], and updates the [MarkerManager].
 *
 * SDK-specific controllers can inject:
 * - how to decide "tiled vs native" via [shouldTile]
 * - where to keep tiled IDs via [tiledMarkerIds]
 *
 * The tile overlay itself (RasterLayer state, cache busting, etc.) remains controller-specific.
 */
object MarkerIngestionEngine {
    data class Result(
        val tiledDataChanged: Boolean,
        val hasTiledMarkers: Boolean,
    )

    suspend fun <ActualMarker : Any> ingest(
        data: List<MarkerState>,
        markerManager: MarkerManager<ActualMarker>,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
        defaultMarkerIcon: BitmapIcon,
        tilingEnabled: Boolean,
        tiledMarkerIds: MutableSet<String>,
        shouldTile: (MarkerState) -> Boolean,
    ): Result {
        val previousIds =
            markerManager
                .allEntities()
                .asSequence()
                .map { it.state.id }
                .toMutableSet()

        val added = mutableListOf<MarkerOverlayRendererInterface.AddParamsInterface>()
        val updated = mutableListOf<MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker>>()
        val removedActualMarkers = mutableListOf<MarkerEntityInterface<ActualMarker>>()
        var tiledDataChanged = false

        data.forEach { state ->
            val wantsTiled = tilingEnabled && shouldTile(state)
            val markerIcon = state.icon?.toBitmapIcon() ?: defaultMarkerIcon

            if (previousIds.contains(state.id)) {
                val prevEntity = markerManager.getEntity(state.id)!!
                val wasTiled = tiledMarkerIds.contains(state.id)

                if (wantsTiled) {
                    if (!wasTiled) {
                        prevEntity.marker?.let { removedActualMarkers.add(prevEntity) }
                        tiledMarkerIds.add(state.id)
                    }
                    markerManager.updateEntity(
                        MarkerEntity(
                            marker = null,
                            state = state,
                            visible = prevEntity.visible,
                            isRendered = true,
                        ),
                    )
                    tiledDataChanged = true
                } else {
                    if (wasTiled) {
                        tiledMarkerIds.remove(state.id)
                        tiledDataChanged = true
                    }
                    updated.add(
                        object : MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker> {
                            override val current: MarkerEntityInterface<ActualMarker> =
                                MarkerEntity(
                                    state = state,
                                    marker = prevEntity.marker,
                                    visible = prevEntity.visible,
                                    isRendered = true,
                                )
                            override val bitmapIcon: BitmapIcon = markerIcon
                            override val prev: MarkerEntityInterface<ActualMarker> = prevEntity
                        },
                    )
                }
                previousIds.remove(state.id)
            } else {
                if (wantsTiled) {
                    tiledMarkerIds.add(state.id)
                    markerManager.registerEntity(
                        MarkerEntity(
                            marker = null,
                            state = state,
                            visible = true,
                            isRendered = true,
                        ),
                    )
                    tiledDataChanged = true
                } else {
                    added.add(
                        object : MarkerOverlayRendererInterface.AddParamsInterface {
                            override val state: MarkerState = state
                            override val bitmapIcon: BitmapIcon = markerIcon
                        },
                    )
                }
            }
        }

        // Lock the entire marker manage.
        // Avoid the incorrect marker tile rendering
        markerManager.lock()

        previousIds.forEach { remainId ->
            markerManager.removeEntity(remainId)?.let { removedEntity ->
                if (tiledMarkerIds.remove(remainId)) {
                    tiledDataChanged = true
                } else {
                    removedEntity.marker?.let { removedActualMarkers.add(removedEntity) }
                }
            }
        }

        if (removedActualMarkers.isNotEmpty()) {
            renderer.onRemove(removedActualMarkers)
        }

        if (added.isNotEmpty()) {
            val actualMarkers = renderer.onAdd(added)
            actualMarkers.forEachIndexed { index, actualMarker ->
                actualMarker ?: return@forEachIndexed
                val state = added[index].state
                val entity =
                    MarkerEntity(
                        marker = actualMarker,
                        state = state,
                        visible = true,
                        isRendered = true,
                    )
                markerManager.registerEntity(entity)
                state.getAnimation()?.let { renderer.onAnimate(entity) }
            }
        }

        if (updated.isNotEmpty()) {
            val actualMarkers = renderer.onChange(updated)
            actualMarkers.forEachIndexed { index, actualMarker ->
                val params = updated[index]
                markerManager.updateEntity(
                    MarkerEntity(
                        marker = actualMarker ?: params.prev.marker,
                        state = params.current.state,
                        visible = params.current.visible,
                        isRendered = true,
                    ),
                )

                val prevFinger = params.prev.fingerPrint
                val currentFinger = params.current.fingerPrint
                if (prevFinger.animation != currentFinger.animation) {
                    params.current.state.getAnimation()?.let {
                        markerManager.getEntity(params.current.state.id)?.let { entity -> renderer.onAnimate(entity) }
                    }
                }
            }
        }

        // Unlock the entire marker manager lock
        markerManager.unlock()

        renderer.onPostProcess()
        return Result(tiledDataChanged = tiledDataChanged, hasTiledMarkers = tiledMarkerIds.isNotEmpty())
    }
}
