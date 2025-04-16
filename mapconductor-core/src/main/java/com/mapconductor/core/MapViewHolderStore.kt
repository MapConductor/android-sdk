package com.mapconductor.core

import android.content.Context
import androidx.annotation.Keep

abstract class MapViewHolderStoreBase<T, M> {
    private val holders = mutableMapOf<String, MapViewHolderImpl<T, M>>()

    fun has(id: String): Boolean {
        return holders.containsKey(id)
    }

    fun get(id: String): MapViewHolderImpl<T, M>? = holders[id]
    fun set(id: String, viewHolder: MapViewHolderImpl<T, M>) {
        holders[id] = viewHolder
    }

    fun clear(id: String) {
        holders.remove(id)?.destroy()
    }

    @Keep
    fun clearAll() {
        holders.values.forEach { it.destroy() }
        holders.clear()
    }
}

abstract class MapViewHolderStoreBaseAsync<T, M>: MapViewHolderStoreBase<T, M>() {
    abstract suspend fun getOrCreate(
        context: Context,
        id: String,
    ): MapViewHolderImpl<T, M>

}

abstract class MapViewHolderStoreBaseSync<T, M>: MapViewHolderStoreBase<T, M>() {
    abstract fun getOrCreate(
        context: Context,
        id: String,
    ): MapViewHolderImpl<T, M>
}

