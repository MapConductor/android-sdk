package com.mapconductor.core.circle

import java.util.concurrent.ConcurrentHashMap

class CircleManager<ActualCircle> {
    private val entities: ConcurrentHashMap<String, CircleEntity<ActualCircle>> = ConcurrentHashMap()

    fun getEntity(id: String): CircleEntity<ActualCircle>? = entities.get(id)

    fun removeEntity(id: String): CircleEntity<ActualCircle>? {
        val removed = entities.remove(id)
        return removed
    }

    fun registerEntity(entity: CircleEntity<ActualCircle>) {
        entities[entity.state.id] = entity
    }

    fun updateEntity(entity: CircleEntity<ActualCircle>) {
        entities[entity.state.id] = entity
    }

    fun allEntities(): List<CircleEntity<ActualCircle>> = entities.values.toList()

    fun clear() {
        entities.clear()
    }
}
