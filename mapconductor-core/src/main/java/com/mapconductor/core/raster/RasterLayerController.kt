package com.mapconductor.core.raster

import com.mapconductor.core.controller.OverlayControllerInterface
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

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

    override suspend fun update(state: RasterLayerState) {
        semaphore.withPermit {
            val prevEntity = rasterLayerManager.getEntity(state.id) ?: return
            val currentFinger = state.fingerPrint()
            val prevFinger = prevEntity.fingerPrint
            if (currentFinger == prevFinger) {
                return
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

    override suspend fun clear() {
        semaphore.withPermit {
            val entities: List<RasterLayerEntityInterface<ActualLayer>> = rasterLayerManager.allEntities()
            renderer.onRemove(entities)
            renderer.onPostProcess()
            rasterLayerManager.clear()
        }
    }

    override fun find(position: GeoPointInterface): RasterLayerEntityInterface<ActualLayer>? = null

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        renderer.onCameraChanged(mapCameraPosition)
    }

    override fun destroy() {
        // No native resources to clean up
    }
}
