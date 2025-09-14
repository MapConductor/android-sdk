package com.mapconductor.core.marker

import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexCell
import com.mapconductor.core.geocell.HexCellRegistry
import com.mapconductor.core.geocell.HexGeocell

open class MarkerManager<ActualMarker>(
    geocell: HexGeocell,
) {
    private val entities = mutableMapOf<String, MarkerEntity<ActualMarker>>()
    private val cellRegistry =
        HexCellRegistry<ActualMarker>(
            geocell = geocell,
            // Maximum zoom level
            zoom = 20.0,
        )

    // Native index for performance-critical operations (optional)
    // Uses reflection to load NativeMarkerIndex if the strategy module is available
    private val nativeIndex: Any? =
        try {
            val nativeMarkerIndexClass = Class.forName("com.mapconductor.marker.strategy.NativeMarkerIndex")
            val createMethod = nativeMarkerIndexClass.getDeclaredMethod("create", Int::class.java, Double::class.java)
            createMethod.invoke(null, geocell.baseHexSideLength, 20.0)
        } catch (e: Exception) {
            // NativeMarkerIndex not available (strategy module not included)
            null
        }

    @Volatile
    private var isDestroyed = false

    open fun getEntity(id: String): MarkerEntity<ActualMarker>? {
        checkNotDestroyed()
        return entities.get(id)
    }

    open fun hasEntity(id: String): Boolean {
        checkNotDestroyed()
        return if (nativeIndex != null) {
            try {
                val hasMarkerMethod = nativeIndex.javaClass.getDeclaredMethod("hasMarker", String::class.java)
                hasMarkerMethod.invoke(nativeIndex, id) as Boolean
            } catch (e: Exception) {
                entities.containsKey(id)
            }
        } else {
            entities.containsKey(id)
        }
    }

    open fun removeEntity(id: String): MarkerEntity<ActualMarker>? {
        checkNotDestroyed()
        val removed =
            entities.remove(id)?.also {
                cellRegistry.removePoint(it)
                if (nativeIndex != null) {
                    try {
                        val removeMarkerMethod =
                            nativeIndex.javaClass
                                .getDeclaredMethod("removeMarker", String::class.java)
                        removeMarkerMethod.invoke(nativeIndex, id)
                    } catch (e: Exception) {
                        // Fallback: native index not available
                    }
                }
            }
        return removed
    }

    open fun metersPerPixel(
        position: IGeoPoint,
        zoom: Double,
        pixels: Double,
        tileSize: Int = 256,
    ): Double {
        checkNotDestroyed()
        return if (nativeIndex != null) {
            try {
                val metersPerPixelMethod =
                    nativeIndex.javaClass.getDeclaredMethod(
                        "metersPerPixel",
                        com.mapconductor.core.features.IGeoPoint::class.java,
                        Double::class.java,
                        Double::class.java,
                        Int::class.java,
                    )
                metersPerPixelMethod.invoke(nativeIndex, position, zoom, pixels, tileSize) as Double
            } catch (e: Exception) {
                // Fallback calculation when native index is not available
                val earthCircumference = 40075017.0 // meters
                val pixelsAtZoom = tileSize * Math.pow(2.0, zoom)
                earthCircumference / pixelsAtZoom * Math.cos(Math.toRadians(position.latitude)) * pixels
            }
        } else {
            // Fallback calculation when native index is not available
            val earthCircumference = 40075017.0 // meters
            val pixelsAtZoom = tileSize * Math.pow(2.0, zoom)
            earthCircumference / pixelsAtZoom * Math.cos(Math.toRadians(position.latitude)) * pixels
        }
    }

    open fun findNearest(position: IGeoPoint): MarkerEntity<ActualMarker>? {
        checkNotDestroyed()
        val nearestId =
            if (nativeIndex != null) {
                try {
                    val findNearestMethod =
                        nativeIndex.javaClass
                            .getDeclaredMethod("findNearest", com.mapconductor.core.features.IGeoPoint::class.java)
                    findNearestMethod.invoke(nativeIndex, position) as String?
                } catch (e: Exception) {
                    // Fallback: find nearest using brute force
                    entities.values
                        .minByOrNull { entity ->
                            val dx = entity.state.position.latitude - position.latitude
                            val dy = entity.state.position.longitude - position.longitude
                            dx * dx + dy * dy
                        }?.state
                        ?.id
                }
            } else {
                // Fallback: find nearest using brute force
                entities.values
                    .minByOrNull { entity ->
                        val dx = entity.state.position.latitude - position.latitude
                        val dy = entity.state.position.longitude - position.longitude
                        dx * dx + dy * dy
                    }?.state
                    ?.id
            } ?: return null
        return entities[nearestId]
    }

    open fun findByIdPrefix(prefix: String): List<HexCell> {
        checkNotDestroyed()
        return cellRegistry.findByIdPrefix(prefix)
    }

    open fun registerEntity(entity: MarkerEntity<ActualMarker>) {
        checkNotDestroyed()
        entities[entity.state.id] = entity
        cellRegistry.setPoint(entity)
        if (nativeIndex != null) {
            try {
                val registerMarkerMethod =
                    nativeIndex.javaClass.getDeclaredMethod(
                        "registerMarker",
                        String::class.java,
                        com.mapconductor.core.features.IGeoPoint::class.java,
                        Boolean::class.java,
                    )
                registerMarkerMethod.invoke(nativeIndex, entity.state.id, entity.state.position, entity.state.clickable)
            } catch (e: Exception) {
                // Fallback: native index not available
            }
        }
    }

    open fun updateEntity(entity: MarkerEntity<ActualMarker>) {
        checkNotDestroyed()
        entities[entity.state.id] = entity
        cellRegistry.setPoint(entity)
        if (nativeIndex != null) {
            try {
                val updateMarkerMethod =
                    nativeIndex.javaClass.getDeclaredMethod(
                        "updateMarker",
                        String::class.java,
                        com.mapconductor.core.features.IGeoPoint::class.java,
                        Boolean::class.java,
                    )
                updateMarkerMethod.invoke(nativeIndex, entity.state.id, entity.state.position, entity.state.clickable)
            } catch (e: Exception) {
                // Fallback: native index not available
            }
        }
    }

    open fun allEntities(): List<MarkerEntity<ActualMarker>> {
        checkNotDestroyed()
        return entities.values.toList()
    }

    open fun clear() {
        checkNotDestroyed()
        entities.clear()
        cellRegistry.clear()
        if (nativeIndex != null) {
            try {
                val clearMethod = nativeIndex.javaClass.getDeclaredMethod("clear")
                clearMethod.invoke(nativeIndex)
            } catch (e: Exception) {
                // Fallback: native index not available
            }
        }
    }

    open fun findMarkersInBounds(bounds: com.mapconductor.core.features.GeoRectBounds): List<MarkerEntity<ActualMarker>> {
        checkNotDestroyed()
        if (bounds.isEmpty) return emptyList()

        val markerIds =
            if (nativeIndex != null) {
                try {
                    val findMarkersInBoundsMethod =
                        nativeIndex.javaClass.getDeclaredMethod(
                            "findMarkersInBounds",
                            com.mapconductor.core.features.GeoRectBounds::class.java,
                        )
                    @Suppress("UNCHECKED_CAST")
                    findMarkersInBoundsMethod.invoke(nativeIndex, bounds) as List<String>
                } catch (e: Exception) {
                    // Fallback: filter all entities by bounds
                    entities.values
                        .filter { entity ->
                            bounds.contains(entity.state.position)
                        }.map { it.state.id }
                }
            } else {
                // Fallback: filter all entities by bounds
                entities.values
                    .filter { entity ->
                        bounds.contains(entity.state.position)
                    }.map { it.state.id }
            }
        return markerIds.mapNotNull { id -> entities[id] }
    }

    /**
     * Properly destroy native resources when switching map providers
     * IMPORTANT: Call this when disposing of the MarkerManager
     */
    open fun destroy() {
        if (!isDestroyed) {
            isDestroyed = true
            entities.clear()
            cellRegistry.clear()
            if (nativeIndex != null) {
                try {
                    val destroyMethod = nativeIndex.javaClass.getDeclaredMethod("destroy")
                    destroyMethod.invoke(nativeIndex)
                } catch (e: Exception) {
                    // Fallback: native index not available
                }
            }
        }
    }

    private fun checkNotDestroyed() {
        if (isDestroyed) {
            throw IllegalStateException("MarkerManager has been destroyed")
        }
    }

    protected open fun finalize() {
        destroy()
    }
}
