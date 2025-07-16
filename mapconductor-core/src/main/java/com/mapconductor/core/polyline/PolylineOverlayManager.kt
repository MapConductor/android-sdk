package com.mapconductor.core.polyline

import kotlinx.coroutines.sync.Semaphore

interface PolylineOverlayManager<ActualPolyline> {
    suspend fun addPolylines(polylines: List<PolylineState>)

    suspend fun updatePolyline(polyline: PolylineState)
    suspend fun clearOverlays()

    fun getPolylineState(id: String): PolylineState?
}

interface UpdateParams<ActualPolyline> {
    val entity: PolylineEntity<ActualPolyline>
    val prevEntity: PolylineEntity<ActualPolyline>
}

class PolylineOverlayManagerImpl<ActualPolyline>(
    val onAdd: suspend (List<PolylineState>) -> List<ActualPolyline?>,
    val onChange: suspend (List<UpdateParams<ActualPolyline>>) -> List<ActualPolyline?>,
    val onRemove: suspend (List<PolylineEntity<ActualPolyline>>) -> Unit,
    val onPostProcess: (suspend () -> Unit)? = null,
): PolylineOverlayManager<ActualPolyline> {
    val polylineEntities = mutableMapOf<String, PolylineEntity<ActualPolyline>>()

    val semaphore = Semaphore(1)

    override suspend fun addPolylines(polylines: List<PolylineState>) {
        semaphore.acquire()
        val previous = polylineEntities.keys.toMutableSet()
        val added = mutableListOf<PolylineState>()
        val updated = mutableListOf<UpdateParams<ActualPolyline>>()
        val removed = mutableListOf<PolylineEntity<ActualPolyline>>()
        polylines.forEach {
            if (previous.contains(it.id)) {
                val prevEntity = polylineEntities.get(it.id)!!
                updated.add(object : UpdateParams<ActualPolyline> {
                    override val entity: PolylineEntity<ActualPolyline> = PolylineEntityImpl(
                        state = it,
                        polyline = prevEntity.polyline,
                    )
                    override val prevEntity: PolylineEntity<ActualPolyline> = prevEntity
                })
                previous.remove(it.id)
                return@forEach
            }
            added.add(it)
            previous.remove(it.id)
        }
        previous.forEach { remainId ->
            polylineEntities.remove(remainId)?.let { removedEntity ->
                removed.add(removedEntity)
            }
        }

        if (added.isNotEmpty()) {
            val actualPolylines = onAdd(added)
            actualPolylines.forEachIndexed { index, actualPolyline ->
                actualPolyline?.let {
                    val state = added[index]
                    val entity = PolylineEntityImpl<ActualPolyline>(
                        polyline = it,
                        state = state,
                    )
                    polylineEntities[state.id] = entity
                }
            }
        }

        if (updated.isNotEmpty()) {
            val actualPolylines: List<ActualPolyline?> = onChange(updated)
            actualPolylines.forEachIndexed { index, actualPolyline ->
                actualPolyline?.let {
                    val state = updated[index].entity.state
                    val entity = PolylineEntityImpl<ActualPolyline>(
                        polyline = it,
                        state = state,
                    )
                    polylineEntities[state.id] = entity
                }
            }
        }

        if (removed.isNotEmpty()) {
            onRemove(removed)
        }
        onPostProcess?.invoke()

        semaphore.release()
    }

    override suspend fun updatePolyline(polyline: PolylineState) {
        semaphore.acquire()
        semaphore.release()
    }

    override suspend fun clearOverlays() {
        semaphore.acquire()
        val entities = polylineEntities.values.toList()
        onRemove(entities)
        polylineEntities.clear()
        semaphore.release()
    }

    override fun getPolylineState(id: String): PolylineState? = polylineEntities.get(id)?.state
}
