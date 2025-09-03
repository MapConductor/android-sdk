package com.mapconductor.core.circle

import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.spherical.haversineDistance
import java.util.concurrent.ConcurrentHashMap

interface CircleManager<ActualCircle> {
    fun registerEntity(entity: CircleEntity<ActualCircle>)

    fun removeEntity(id: String): CircleEntity<ActualCircle>?

    fun getEntity(id: String): CircleEntity<ActualCircle>?

    fun allEntities(): List<CircleEntity<ActualCircle>>

    fun clear()

    fun find(position: IGeoPoint): CircleEntity<ActualCircle>?
}

class CircleManagerImpl<ActualCircle> : CircleManager<ActualCircle> {
    private val entities: ConcurrentHashMap<String, CircleEntity<ActualCircle>> = ConcurrentHashMap()

    override fun getEntity(id: String): CircleEntity<ActualCircle>? = entities.get(id)

    override fun removeEntity(id: String): CircleEntity<ActualCircle>? {
        val removed = entities.remove(id)
        return removed
    }

    override fun registerEntity(entity: CircleEntity<ActualCircle>) {
        entities[entity.state.id] = entity
    }

    fun updateEntity(entity: CircleEntity<ActualCircle>) {
        entities[entity.state.id] = entity
    }

    override fun allEntities(): List<CircleEntity<ActualCircle>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(position: IGeoPoint): CircleEntity<ActualCircle>? {
        val filtered =
            allEntities().filter { entity ->
                val centerPos = entity.state.center
                val distance = haversineDistance(centerPos, position)
                return@filter (distance <= entity.state.radiusMeters) && entity.state.clickable
            }

        if (filtered.isEmpty()) {
            return null
        }

        var maxZIndex = Int.MIN_VALUE
        var maxEntity = filtered[0]

        filtered.forEach {
            val zIndex = it.state.zIndex ?: calculateZIndex(it.state.center)
            if (maxZIndex < zIndex) {
                maxZIndex = zIndex
                maxEntity = it
            }
        }
        return maxEntity
    }
}
