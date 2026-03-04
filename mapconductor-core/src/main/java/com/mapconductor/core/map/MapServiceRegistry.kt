package com.mapconductor.core.map

import androidx.compose.runtime.compositionLocalOf
import java.util.concurrent.ConcurrentHashMap

/**
 * Typed service key used to register and retrieve map-scoped services (plugins).
 *
 * Keys are typically defined as singleton `object`s.
 */
interface MapServiceKey<T : Any>

interface MapServiceRegistry {
    fun <T : Any> get(key: MapServiceKey<T>): T?
}

class MutableMapServiceRegistry : MapServiceRegistry {
    private val services = ConcurrentHashMap<MapServiceKey<*>, Any>()

    fun clear() {
        services.clear()
    }

    fun <T : Any> put(
        key: MapServiceKey<T>,
        value: T,
    ) {
        services[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: MapServiceKey<T>): T? = services[key] as? T
}

object EmptyMapServiceRegistry : MapServiceRegistry {
    override fun <T : Any> get(key: MapServiceKey<T>): T? = null
}

val LocalMapServiceRegistry =
    compositionLocalOf<MapServiceRegistry> {
        EmptyMapServiceRegistry
    }
