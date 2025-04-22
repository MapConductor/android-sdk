package com.mapconductor.core

import android.content.Context
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleOwner

abstract class MapViewHolderStoreBase<T, M> {
    private val holders = mutableMapOf<String, MapViewHolder<T, M>>()

    fun has(id: String): Boolean {
        return holders.containsKey(id)
    }

    fun get(id: String): MapViewHolder<T, M>? = holders[id]
    fun set(id: String, viewHolder: MapViewHolder<T, M>) {
        holders[id] = viewHolder
    }

    fun clear(id: String, owner: LifecycleOwner? = null) {
        holders.remove(id)?.destroy(owner)
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
    ): MapViewHolder<T, M>

}

abstract class MapViewHolderStoreBaseSync<T, M>: MapViewHolderStoreBase<T, M>() {
    abstract fun getOrCreate(
        context: Context,
        id: String,
    ): MapViewHolder<T, M>
}

abstract class MapViewHolderStoreBaseSyncWithLifeCycleOwner<T, M>: MapViewHolderStoreBase<T, M>() {
    abstract fun getOrCreate(
        context: Context,
        id: String,
        owner: LifecycleOwner,
    ): MapViewHolder<T, M>
}

