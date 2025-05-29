package com.mapconductor.core

import android.content.Context

abstract class StaticHolder<ValueType> {
    private val holders = mutableMapOf<String, ValueType>()

    fun has(id: String): Boolean {
        return holders.containsKey(id)
    }

    fun get(id: String): ValueType? = holders[id]
    fun set(id: String, viewHolder: ValueType) {
        holders[id] = viewHolder
    }

    fun remove(id: String) {
        holders.remove(id)
    }

    fun clearAll() {
        holders.clear()
    }
}

abstract class MapViewHolderStoreBaseAsync<
        TMapView,
        TMap,
        TOptions
    >: StaticHolder<MapViewHolder<TMapView, TMap>>() {
    abstract suspend fun getOrCreate(
        context: Context,
        id: String,
        options: TOptions,
    ): MapViewHolder<TMapView, TMap>

}
