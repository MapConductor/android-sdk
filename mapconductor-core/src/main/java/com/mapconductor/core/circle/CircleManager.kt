package com.mapconductor.core.circle

import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.spherical.Spherical
import java.util.concurrent.ConcurrentHashMap

interface CircleManagerInterface<ActualCircle> {
    fun registerEntity(entity: CircleEntityInterface<ActualCircle>)

    fun removeEntity(id: String): CircleEntityInterface<ActualCircle>?

    fun getEntity(id: String): CircleEntityInterface<ActualCircle>?

    fun hasEntity(id: String): Boolean

    fun allEntities(): List<CircleEntityInterface<ActualCircle>>

    fun clear()

    fun find(position: GeoPointInterface): CircleEntityInterface<ActualCircle>?
}

class CircleManager<ActualCircle> : CircleManagerInterface<ActualCircle> {
    private val entities: ConcurrentHashMap<String, CircleEntityInterface<ActualCircle>> = ConcurrentHashMap()

    override fun getEntity(id: String): CircleEntityInterface<ActualCircle>? = entities.get(id)

    override fun hasEntity(id: String): Boolean = entities.containsKey(id)

    override fun removeEntity(id: String): CircleEntityInterface<ActualCircle>? {
        val removed = entities.remove(id)
        return removed
    }

    override fun registerEntity(entity: CircleEntityInterface<ActualCircle>) {
        entities[entity.state.id] = entity
    }

    fun updateEntity(entity: CircleEntityInterface<ActualCircle>) {
        entities[entity.state.id] = entity
    }

    override fun allEntities(): List<CircleEntityInterface<ActualCircle>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(position: GeoPointInterface): CircleEntityInterface<ActualCircle>? {
        val filtered =
            allEntities().filter { entity ->
                val centerPos = entity.state.center
                val distance = Spherical.computeDistanceBetween(centerPos, position)
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
