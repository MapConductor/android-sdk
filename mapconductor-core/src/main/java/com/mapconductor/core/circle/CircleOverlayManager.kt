package com.mapconductor.core.circle

import com.mapconductor.core.circle.CircleRenderer.UpdateParams
import com.mapconductor.core.features.IGeoPoint
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

interface CircleOverlayManager<ActualCircle> {
    suspend fun addCircles(circles: List<CircleState>)

    suspend fun updateCircle(circle: CircleState)

    suspend fun clearOverlays()

    fun getCircleState(id: String): CircleState?

    fun getAllEntities(): List<CircleEntity<ActualCircle>>

    fun find(position: IGeoPoint): CircleEntity<ActualCircle>?
}

class CircleOverlayManagerImpl<ActualCircle>(
    val onAdd: suspend (List<CircleState>) -> List<ActualCircle?>,
    val onChange: suspend (List<UpdateParams<ActualCircle>>) -> List<ActualCircle?>,
    val onRemove: suspend (List<CircleEntity<ActualCircle>>) -> Unit,
    val onPostProcess: (suspend () -> Unit)? = null,
    val circleManager: CircleManager<ActualCircle> = CircleManager<ActualCircle>(),
) : CircleOverlayManager<ActualCircle> {
    val semaphore = Semaphore(1)

    override suspend fun addCircles(circles: List<CircleState>) {
        semaphore.withPermit {
            val previous = circleManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<CircleState>()
            val updated = mutableListOf<UpdateParams<ActualCircle>>()
            val removed = mutableListOf<CircleEntity<ActualCircle>>()

            circles.forEach {
                // If the same id exists in previous key set, we have the circle.
                // So, we should update it.
                circleManager.getEntity(it.id)?.let { prevEntity ->
                    updated.add(
                        object : UpdateParams<ActualCircle> {
                            override val entity: CircleEntity<ActualCircle> =
                                CircleEntityImpl(
                                    state = it,
                                    circle = prevEntity.circle,
                                )
                            override val prevEntity: CircleEntity<ActualCircle> = prevEntity
                        },
                    )
                    previous.remove(it.id)
                    return@forEach
                }

                // If we don't have the id, it's a new circle.
                added.add(it)
                previous.remove(it.id)
            }

            // Remained previous IDs has been removed.
            previous.forEach { remainId ->
                circleManager.removeEntity(remainId)?.let { removedEntity ->
                    removed.add(removedEntity)
                }
            }

            if (added.isNotEmpty()) {
                val actualCircles = onAdd(added)
                actualCircles.forEachIndexed { index, actualCircle ->
                    actualCircle?.let {
                        val state = added[index]
                        val entity =
                            CircleEntityImpl<ActualCircle>(
                                circle = it,
                                state = state,
                            )
                        circleManager.registerEntity(entity)
                    }
                }
            }

            if (updated.isNotEmpty()) {
                val actualCircles: List<ActualCircle?> = onChange(updated)
                actualCircles.forEachIndexed { index, actualCircle ->
                    actualCircle?.let {
                        val state = updated[index].entity.state
                        val entity =
                            CircleEntityImpl<ActualCircle>(
                                circle = it,
                                state = state,
                            )
                        circleManager.updateEntity(entity)
                    }
                }
            }

            if (removed.isNotEmpty()) {
                onRemove(removed)
            }
            onPostProcess?.invoke()
        }
    }

    override suspend fun updateCircle(circle: CircleState) {
        semaphore.withPermit {
            circleManager.getEntity(circle.id)?.let { prevEntity ->

                val updates =
                    listOf(
                        object : UpdateParams<ActualCircle> {
                            override val entity: CircleEntity<ActualCircle> =
                                CircleEntityImpl(
                                    state = circle,
                                    circle = prevEntity.circle,
                                )
                            override val prevEntity: CircleEntity<ActualCircle> = prevEntity
                        },
                    )

                val actualCircles: List<ActualCircle?> = onChange(updates)
                actualCircles.forEachIndexed { index, actualCircle ->
                    actualCircle?.let {
                        val entity =
                            CircleEntityImpl<ActualCircle>(
                                circle = it,
                                state = circle,
                            )
                        circleManager.updateEntity(entity)
                    }
                }
            }
            onPostProcess?.invoke()
        }
    }

    override suspend fun clearOverlays() {
        semaphore.withPermit {
            val entities = circleManager.allEntities()
            onRemove(entities)
            circleManager.clear()
        }
    }

    override fun getCircleState(id: String): CircleState? = circleManager.getEntity(id)?.state

    override fun getAllEntities(): List<CircleEntity<ActualCircle>> = circleManager.allEntities()

    override fun find(position: IGeoPoint): CircleEntity<ActualCircle>? = circleManager.find(position)
}
