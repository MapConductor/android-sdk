package com.mapconductor.core.raster

import com.mapconductor.core.controller.OverlayControllerInterface
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

abstract class RasterLayerController<ActualLayer : Any>(
    val rasterLayerManager: RasterLayerManagerInterface<ActualLayer>,
    open val renderer: RasterLayerOverlayRendererInterface<ActualLayer>,
    override var clickListener: OnRasterLayerEventHandler? = null,
) : OverlayControllerInterface<
        RasterLayerState,
        RasterLayerEntityInterface<ActualLayer>,
        RasterLayerEvent,
    > {
    override val zIndex: Int = 0
    val semaphore = Semaphore(1)

    override suspend fun add(data: List<RasterLayerState>) {
        withContext(renderer.coroutine.coroutineContext) {
            semaphore.withPermit {
                val previous = rasterLayerManager.allEntities().map { it.state.id }.toMutableSet()
                val added = mutableListOf<RasterLayerOverlayRendererInterface.AddParamsInterface>()
                val updated = mutableListOf<RasterLayerOverlayRendererInterface.ChangeParamsInterface<ActualLayer>>()
                val removed = mutableListOf<RasterLayerEntityInterface<ActualLayer>>()

                data.forEach { state ->
                    if (previous.contains(state.id)) {
                        val prevEntity = rasterLayerManager.getEntity(state.id) ?: return@forEach
                        updated.add(
                            object : RasterLayerOverlayRendererInterface.ChangeParamsInterface<ActualLayer> {
                                override val current: RasterLayerEntityInterface<ActualLayer> =
                                    RasterLayerEntity(
                                        layer = prevEntity.layer,
                                        state = state,
                                    )
                                override val prev: RasterLayerEntityInterface<ActualLayer> = prevEntity
                            },
                        )
                        previous.remove(state.id)
                    } else {
                        added.add(
                            object : RasterLayerOverlayRendererInterface.AddParamsInterface {
                                override val state: RasterLayerState = state
                            },
                        )
                        previous.remove(state.id)
                    }
                }

                previous.forEach { remainId ->
                    rasterLayerManager.removeEntity(remainId)?.let { removedEntity ->
                        removed.add(removedEntity)
                    }
                }

                if (removed.isNotEmpty()) {
                    renderer.onRemove(removed)
                }

                if (added.isNotEmpty()) {
                    val actualLayers = renderer.onAdd(added)
                    actualLayers.forEachIndexed { index, actualLayer ->
                        actualLayer?.let {
                            val entity =
                                RasterLayerEntity(
                                    layer = it,
                                    state = added[index].state,
                                )
                            rasterLayerManager.registerEntity(entity)
                        }
                    }
                }

                if (updated.isNotEmpty()) {
                    val actualLayers = renderer.onChange(updated)
                    actualLayers.forEachIndexed { index, actualLayer ->
                        actualLayer?.let {
                            val state = updated[index].current.state
                            val entity =
                                RasterLayerEntity(
                                    layer = it,
                                    state = state,
                                )
                            rasterLayerManager.registerEntity(entity)
                        }
                    }
                }

                renderer.onPostProcess()
            }
        }
    }

    override suspend fun update(state: RasterLayerState) {
        withContext(renderer.coroutine.coroutineContext) {
            semaphore.withPermit {
                val prevEntity = rasterLayerManager.getEntity(state.id) ?: return@withPermit
                val currentFinger = state.fingerPrint()
                val prevFinger = prevEntity.fingerPrint
                if (currentFinger == prevFinger) {
                    return@withPermit
                }

                val entity =
                    RasterLayerEntity(
                        layer = prevEntity.layer,
                        state = state,
                    )
                val params =
                    object : RasterLayerOverlayRendererInterface.ChangeParamsInterface<ActualLayer> {
                        override val current: RasterLayerEntityInterface<ActualLayer> = entity
                        override val prev: RasterLayerEntityInterface<ActualLayer> = prevEntity
                    }
                val layers = renderer.onChange(listOf(params))
                layers[0]?.let {
                    val updatedEntity =
                        RasterLayerEntity(
                            layer = it,
                            state = state,
                        )
                    rasterLayerManager.registerEntity(updatedEntity)
                }
                renderer.onPostProcess()
            }
        }
    }

    /**
     * Adds or updates a single layer without removing other existing layers.
     *
     * This is useful for internal layers (e.g. marker tiling) that want to manage a raster layer
     * independently from the app's RasterLayers composition.
     */
    suspend fun upsert(state: RasterLayerState) {
        withContext(renderer.coroutine.coroutineContext) {
            semaphore.withPermit {
                val prevEntity = rasterLayerManager.getEntity(state.id)
                if (prevEntity == null) {
                    val params =
                        object : RasterLayerOverlayRendererInterface.AddParamsInterface {
                            override val state: RasterLayerState = state
                        }
                    val layers = renderer.onAdd(listOf(params))
                    layers.firstOrNull()?.let { layer ->
                        val entity =
                            RasterLayerEntity(
                                layer = layer,
                                state = state,
                            )
                        rasterLayerManager.registerEntity(entity)
                    }
                    renderer.onPostProcess()
                    return@withPermit
                }

                val currentFinger = state.fingerPrint()
                val prevFinger = prevEntity.fingerPrint
                if (currentFinger == prevFinger) return@withPermit

                val nextEntity =
                    RasterLayerEntity(
                        layer = prevEntity.layer,
                        state = state,
                    )
                val changeParams =
                    object : RasterLayerOverlayRendererInterface.ChangeParamsInterface<ActualLayer> {
                        override val current: RasterLayerEntityInterface<ActualLayer> = nextEntity
                        override val prev: RasterLayerEntityInterface<ActualLayer> = prevEntity
                    }
                val layers = renderer.onChange(listOf(changeParams))
                layers.firstOrNull()?.let { layer ->
                    val entity =
                        RasterLayerEntity(
                            layer = layer,
                            state = state,
                        )
                    rasterLayerManager.registerEntity(entity)
                }
                renderer.onPostProcess()
            }
        }
    }

    /**
     * Removes a single layer by id without clearing other layers.
     */
    suspend fun removeById(id: String) {
        withContext(renderer.coroutine.coroutineContext) {
            semaphore.withPermit {
                val entity = rasterLayerManager.removeEntity(id) ?: return@withPermit
                renderer.onRemove(listOf(entity))
                renderer.onPostProcess()
            }
        }
    }

    override suspend fun clear() {
        withContext(renderer.coroutine.coroutineContext) {
            semaphore.withPermit {
                val entities: List<RasterLayerEntityInterface<ActualLayer>> = rasterLayerManager.allEntities()
                renderer.onRemove(entities)
                renderer.onPostProcess()
                rasterLayerManager.clear()
            }
        }
    }

    override fun find(position: GeoPointInterface): RasterLayerEntityInterface<ActualLayer>? = null

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        withContext(renderer.coroutine.coroutineContext) {
            renderer.onCameraChanged(mapCameraPosition)
        }
    }

    override fun destroy() {
        // No native resources to clean up
    }
}
