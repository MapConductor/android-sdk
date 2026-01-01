package com.mapconductor.core.raster

import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPositionImpl
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

abstract class RasterLayerController<ActualLayer : Any>(
    val rasterLayerManager: RasterLayerManager<ActualLayer>,
    open val renderer: RasterLayerOverlayRenderer<ActualLayer>,
    override var clickListener: OnRasterLayerEventHandler? = null,
) : OverlayController<
        RasterLayerState,
        RasterLayerEntity<ActualLayer>,
        RasterLayerEvent,
    > {
    override val zIndex: Int = 0
    val semaphore = Semaphore(1)

    override suspend fun add(data: List<RasterLayerState>) {
        semaphore.withPermit {
            val previous = rasterLayerManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<RasterLayerOverlayRenderer.AddParams>()
            val updated = mutableListOf<RasterLayerOverlayRenderer.ChangeParams<ActualLayer>>()
            val removed = mutableListOf<RasterLayerEntity<ActualLayer>>()

            data.forEach { state ->
                if (previous.contains(state.id)) {
                    val prevEntity = rasterLayerManager.getEntity(state.id) ?: return@forEach
                    updated.add(
                        object : RasterLayerOverlayRenderer.ChangeParams<ActualLayer> {
                            override val current: RasterLayerEntity<ActualLayer> =
                                RasterLayerEntityImpl(
                                    layer = prevEntity.layer,
                                    state = state,
                                )
                            override val prev: RasterLayerEntity<ActualLayer> = prevEntity
                        },
                    )
                    previous.remove(state.id)
                } else {
                    added.add(
                        object : RasterLayerOverlayRenderer.AddParams {
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
                            RasterLayerEntityImpl(
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
                            RasterLayerEntityImpl(
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
                RasterLayerEntityImpl(
                    layer = prevEntity.layer,
                    state = state,
                )
            val params =
                object : RasterLayerOverlayRenderer.ChangeParams<ActualLayer> {
                    override val current: RasterLayerEntity<ActualLayer> = entity
                    override val prev: RasterLayerEntity<ActualLayer> = prevEntity
                }
            val layers = renderer.onChange(listOf(params))
            layers[0]?.let {
                val updatedEntity =
                    RasterLayerEntityImpl(
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
            val entities: List<RasterLayerEntity<ActualLayer>> = rasterLayerManager.allEntities()
            renderer.onRemove(entities)
            renderer.onPostProcess()
            rasterLayerManager.clear()
        }
    }

    override fun find(position: GeoPoint): RasterLayerEntity<ActualLayer>? = null

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPositionImpl) {}

    override fun destroy() {
        // No native resources to clean up
    }
}
