package com.mapconductor.core.circle

import com.mapconductor.core.circle.CircleRenderer.UpdateParams
import kotlinx.coroutines.sync.Semaphore

interface CircleOverlayManager<ActualCircle> {
    suspend fun addCircles(circles: List<CircleState>)

    suspend fun updateCircle(circle: CircleState)

    suspend fun clearOverlays()

    fun getCircleState(id: String): CircleState?

    fun getAllEntities(): List<CircleEntity<ActualCircle>>
}

class CircleOverlayManagerImpl<ActualCircle>(
    val onAdd: suspend (List<CircleState>) -> List<ActualCircle?>,
    val onChange: suspend (List<UpdateParams<ActualCircle>>) -> List<ActualCircle?>,
    val onRemove: suspend (List<CircleEntity<ActualCircle>>) -> Unit,
    val onPostProcess: (suspend () -> Unit)? = null,
) : CircleOverlayManager<ActualCircle> {
    val circleEntities = mutableMapOf<String, CircleEntity<ActualCircle>>()

    val semaphore = Semaphore(1)

    override suspend fun addCircles(circles: List<CircleState>) {
        semaphore.acquire()
        val previous = circleEntities.keys.toMutableSet()
        val added = mutableListOf<CircleState>()
        val updated = mutableListOf<UpdateParams<ActualCircle>>()
        val removed = mutableListOf<CircleEntity<ActualCircle>>()
        circles.forEach {
            if (previous.contains(it.id)) {
                val prevEntity = circleEntities.get(it.id)!!
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
            added.add(it)
            previous.remove(it.id)
        }
        previous.forEach { remainId ->
            circleEntities.remove(remainId)?.let { removedEntity ->
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
                    circleEntities[state.id] = entity
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
                    circleEntities[state.id] = entity
                }
            }
        }

        if (removed.isNotEmpty()) {
            onRemove(removed)
        }
        onPostProcess?.invoke()

        semaphore.release()
    }

    override suspend fun updateCircle(circle: CircleState) {
        semaphore.acquire()
        semaphore.release()
    }

    override suspend fun clearOverlays() {
        semaphore.acquire()
        val entities = circleEntities.values.toList()
        onRemove(entities)
        circleEntities.clear()
        semaphore.release()
    }

    override fun getCircleState(id: String): CircleState? = circleEntities.get(id)?.state

    override fun getAllEntities(): List<CircleEntity<ActualCircle>> = circleEntities.values.toList()
}