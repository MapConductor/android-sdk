package com.mapconductor.core.groundimage

import com.mapconductor.core.features.GeoPointInterface

interface GroundImageManagerInterface<ActualGroundImage> {
    fun registerEntity(entity: GroundImageEntityInterface<ActualGroundImage>)

    fun removeEntity(id: String): GroundImageEntityInterface<ActualGroundImage>?

    fun getEntity(id: String): GroundImageEntityInterface<ActualGroundImage>?

    fun hasEntity(id: String): Boolean

    fun allEntities(): List<GroundImageEntityInterface<ActualGroundImage>>

    fun clear()

    fun find(position: GeoPointInterface): GroundImageEntityInterface<ActualGroundImage>?
}

class GroundImageManager<ActualGroundImage> : GroundImageManagerInterface<ActualGroundImage> {
    private val entities = mutableMapOf<String, GroundImageEntityInterface<ActualGroundImage>>()

    override fun registerEntity(entity: GroundImageEntityInterface<ActualGroundImage>) {
        entities[entity.state.id] = entity
    }

    override fun removeEntity(id: String): GroundImageEntityInterface<ActualGroundImage>? = entities.remove(id)

    override fun getEntity(id: String): GroundImageEntityInterface<ActualGroundImage>? = entities[id]

    override fun hasEntity(id: String): Boolean = entities.containsKey(id)

    override fun allEntities(): List<GroundImageEntityInterface<ActualGroundImage>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(position: GeoPointInterface): GroundImageEntityInterface<ActualGroundImage>? =
        entities.values.firstOrNull { entity ->
            entity.state.bounds.contains(position)
        }
}
