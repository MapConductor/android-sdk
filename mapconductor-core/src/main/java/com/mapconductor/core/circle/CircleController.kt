package com.mapconductor.core.circle

import com.mapconductor.core.controller.OverlayControllerInterface
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

abstract class CircleController<ActualCircle>(
    val circleManager: CircleManagerInterface<ActualCircle>,
    open val renderer: CircleOverlayRendererInterface<ActualCircle>,
    override var clickListener: OnCircleEventHandler? = null,
) : OverlayControllerInterface<
        CircleState,
        CircleEntityInterface<ActualCircle>,
        CircleEvent,
    > {
    override val zIndex: Int = 3
    val semaphore = Semaphore(1)

    fun dispatchClick(event: CircleEvent) {
        event.state.onClick?.invoke(event)
        clickListener?.invoke(event)
    }

    override suspend fun add(data: List<CircleState>) {
        semaphore.withPermit {
            val modifiedEntities = mutableListOf<CircleEntityInterface<ActualCircle>>()
            val previous = circleManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<CircleOverlayRendererInterface.AddParamsInterface>()
            val updated = mutableListOf<CircleOverlayRendererInterface.ChangeParamsInterface<ActualCircle>>()
            val removed = mutableListOf<CircleEntityInterface<ActualCircle>>()

            data.forEach { state ->
                if (previous.contains(state.id)) {
                    val prevEntity = circleManager.getEntity(state.id)!!
                    updated.add(
                        object : CircleOverlayRendererInterface.ChangeParamsInterface<ActualCircle> {
                            override val current: CircleEntityInterface<ActualCircle> =
                                CircleEntity(
                                    state = state,
                                    circle = prevEntity.circle,
                                )
                            override val prev: CircleEntityInterface<ActualCircle> = prevEntity
                        },
                    )
                    previous.remove(state.id)
                } else {
                    added.add(
                        object : CircleOverlayRendererInterface.AddParamsInterface {
                            override val state: CircleState = state
                        },
                    )
                    previous.remove(state.id)
                }
            }

            previous.forEach { remainId ->
                circleManager.removeEntity(remainId)?.let { removedEntity ->
                    removed.add(removedEntity)
                }
            }

            // Remove circle
            if (removed.isNotEmpty()) {
                renderer.onRemove(removed)
            }

            // Add new circles
            if (added.isNotEmpty()) {
                val actualCircles: List<ActualCircle?> = renderer.onAdd(added)
                actualCircles.forEachIndexed { index, circle ->
                    circle?.let {
                        val entity =
                            CircleEntity<ActualCircle>(
                                circle = circle,
                                state = added[index].state,
                            )
                        circleManager.registerEntity(entity)
                        modifiedEntities.add(entity)
                    }
                }
            }

            // Update changed circles
            if (updated.isNotEmpty()) {
                val actualCircles: List<ActualCircle?> = renderer.onChange(updated)
                actualCircles.forEachIndexed { index, circle ->
                    circle?.let {
                        val params = updated[index]
                        val entity =
                            CircleEntity<ActualCircle>(
                                state = params.current.state,
                                circle = circle,
                            )
                        circleManager.registerEntity(entity)
                    }
                }
            }

            renderer.onPostProcess()
        }
    }

    override suspend fun update(state: CircleState) {
        semaphore.withPermit {
            val prevEntity = circleManager.getEntity(state.id) ?: return
            val currentFinger = state.fingerPrint()
            val prevFinger = prevEntity.fingerPrint
            if (currentFinger == prevFinger) {
                return
            }

            val circle = prevEntity.circle
            val entity =
                CircleEntity(
                    circle = circle,
                    state = state,
                )
            val circleParams =
                object : CircleOverlayRendererInterface.ChangeParamsInterface<ActualCircle> {
                    override val current: CircleEntityInterface<ActualCircle> = entity
                    override val prev: CircleEntityInterface<ActualCircle> = prevEntity
                }
            val circles = renderer.onChange(listOf(circleParams))

            circles[0]?.let {
                val entity =
                    CircleEntity<ActualCircle>(
                        circle = it,
                        state = state,
                    )
                circleManager.registerEntity(entity)
            }
        }
    }

    override suspend fun clear() {
        semaphore.withPermit {
            val entities: List<CircleEntityInterface<ActualCircle>> = circleManager.allEntities()
            renderer.onRemove(entities)
            circleManager.clear()
        }
    }

    override fun find(position: GeoPointInterface): CircleEntityInterface<ActualCircle>? = circleManager.find(position)

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {}

    override fun destroy() {
        // No native resources to clean up for circles
    }
}
