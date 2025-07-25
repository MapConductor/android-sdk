package com.mapconductor.core.polygon

import com.mapconductor.core.polygon.PolygonRenderer.UpdateParams
import kotlinx.coroutines.sync.Semaphore

interface PolygonOverlayManager<ActualPolygon> {
    suspend fun addPolygons(polygons: List<PolygonState>)

    suspend fun updatePolygon(polygon: PolygonState)

    suspend fun clearOverlays()

    fun getPolygonState(id: String): PolygonState?

    fun getAllEntities(): List<PolygonEntity<ActualPolygon>>
}

class PolygonOverlayManagerImpl<ActualPolygon>(
    val onAdd: suspend (List<PolygonState>) -> List<ActualPolygon?>,
    val onChange: suspend (List<UpdateParams<ActualPolygon>>) -> List<ActualPolygon?>,
    val onRemove: suspend (List<PolygonEntity<ActualPolygon>>) -> Unit,
    val onPostProcess: (suspend () -> Unit)? = null,
) : PolygonOverlayManager<ActualPolygon> {
    val polygonEntities = mutableMapOf<String, PolygonEntity<ActualPolygon>>()

    val semaphore = Semaphore(1)

    override suspend fun addPolygons(polygons: List<PolygonState>) {
        semaphore.acquire()
        val previous = polygonEntities.keys.toMutableSet()
        val added = mutableListOf<PolygonState>()
        val updated = mutableListOf<UpdateParams<ActualPolygon>>()
        val removed = mutableListOf<PolygonEntity<ActualPolygon>>()
        polygons.forEach {
            if (previous.contains(it.id)) {
                val prevEntity = polygonEntities.get(it.id)!!
                updated.add(
                    object : UpdateParams<ActualPolygon> {
                        override val entity: PolygonEntity<ActualPolygon> =
                            PolygonEntityImpl(
                                state = it,
                                polygon = prevEntity.polygon,
                            )
                        override val prevEntity: PolygonEntity<ActualPolygon> = prevEntity
                    },
                )
                previous.remove(it.id)
                return@forEach
            }
            added.add(it)
            previous.remove(it.id)
        }
        previous.forEach { remainId ->
            polygonEntities.remove(remainId)?.let { removedEntity ->
                removed.add(removedEntity)
            }
        }

        if (added.isNotEmpty()) {
            val actualPolygons = onAdd(added)
            actualPolygons.forEachIndexed { index, actualPolygon ->
                actualPolygon?.let {
                    val state = added[index]
                    val entity =
                        PolygonEntityImpl<ActualPolygon>(
                            polygon = it,
                            state = state,
                        )
                    polygonEntities[state.id] = entity
                }
            }
        }

        if (updated.isNotEmpty()) {
            val actualPolygons: List<ActualPolygon?> = onChange(updated)
            actualPolygons.forEachIndexed { index, actualPolygon ->
                actualPolygon?.let {
                    val state = updated[index].entity.state
                    val entity =
                        PolygonEntityImpl<ActualPolygon>(
                            polygon = it,
                            state = state,
                        )
                    polygonEntities[state.id] = entity
                }
            }
        }

        if (removed.isNotEmpty()) {
            onRemove(removed)
        }
        onPostProcess?.invoke()

        semaphore.release()
    }

    override suspend fun updatePolygon(polygon: PolygonState) {
        semaphore.acquire()
        semaphore.release()
    }

    override suspend fun clearOverlays() {
        semaphore.acquire()
        val entities = polygonEntities.values.toList()
        onRemove(entities)
        polygonEntities.clear()
        semaphore.release()
    }

    override fun getPolygonState(id: String): PolygonState? = polygonEntities.get(id)?.state

    override fun getAllEntities(): List<PolygonEntity<ActualPolygon>> = polygonEntities.values.toList()
}