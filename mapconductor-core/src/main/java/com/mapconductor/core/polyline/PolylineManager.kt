package com.mapconductor.core.polyline

import com.mapconductor.core.features.IGeoPoint


interface PolylineManager<ActualPolyline> {
    fun registerEntity(entity: PolylineEntity<ActualPolyline>)

    fun removeEntity(id: String): PolylineEntity<ActualPolyline>?

    fun getEntity(id: String): PolylineEntity<ActualPolyline>?

    fun allEntities(): List<PolylineEntity<ActualPolyline>>

    fun clear()

    fun find(position: IGeoPoint): PolylineEntity<ActualPolyline>?
}

class PolylineManagerImpl<ActualPolyline> : PolylineManager<ActualPolyline> {
    private val entities = mutableMapOf<String, PolylineEntity<ActualPolyline>>()

    override fun registerEntity(entity: PolylineEntity<ActualPolyline>) {
        entities[entity.state.id] = entity
    }

    override fun removeEntity(id: String): PolylineEntity<ActualPolyline>? = entities.remove(id)

    override fun getEntity(id: String): PolylineEntity<ActualPolyline>? = entities[id]

    override fun allEntities(): List<PolylineEntity<ActualPolyline>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(position: IGeoPoint): PolylineEntity<ActualPolyline>? = entities.values.firstOrNull()
}
