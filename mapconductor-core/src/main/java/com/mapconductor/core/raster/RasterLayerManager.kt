package com.mapconductor.core.raster

import com.mapconductor.core.features.GeoPointInterface

interface RasterLayerManagerInterface<ActualLayer> {
    fun registerEntity(entity: RasterLayerEntityInterface<ActualLayer>)

    fun removeEntity(id: String): RasterLayerEntityInterface<ActualLayer>?

    fun getEntity(id: String): RasterLayerEntityInterface<ActualLayer>?

    fun hasEntity(id: String): Boolean

    fun allEntities(): List<RasterLayerEntityInterface<ActualLayer>>

    fun clear()

    fun find(position: GeoPointInterface): RasterLayerEntityInterface<ActualLayer>?
}

class RasterLayerManager<ActualLayer> : RasterLayerManagerInterface<ActualLayer> {
    private val entities = mutableMapOf<String, RasterLayerEntityInterface<ActualLayer>>()

    override fun registerEntity(entity: RasterLayerEntityInterface<ActualLayer>) {
        entities[entity.state.id] = entity
    }

    override fun removeEntity(id: String): RasterLayerEntityInterface<ActualLayer>? = entities.remove(id)

    override fun getEntity(id: String): RasterLayerEntityInterface<ActualLayer>? = entities[id]

    override fun hasEntity(id: String): Boolean = entities.containsKey(id)

    override fun allEntities(): List<RasterLayerEntityInterface<ActualLayer>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun find(position: GeoPointInterface): RasterLayerEntityInterface<ActualLayer>? = null
}
