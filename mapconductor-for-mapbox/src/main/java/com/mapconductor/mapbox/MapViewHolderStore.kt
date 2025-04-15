package com.mapconductor.mapbox

import android.content.Context

object MapViewHolderStore {
    private val holders = mutableMapOf<String, MapViewHolder>()

    suspend fun getOrCreate(id: String, context: Context): MapViewHolder {
        val existing = holders[id]
        if (existing != null) return existing

        val newHolder = MapViewHolder.create(context.applicationContext)
        holders[id] = newHolder
        return newHolder
    }
    fun has(id: String): Boolean {
        return holders.containsKey(id)
    }

    fun get(id: String): MapViewHolder? = holders[id]

    fun clear(id: String) {
        holders.remove(id)?.destroy()
    }

    fun clearAll() {
        holders.values.forEach { it.destroy() }
        holders.clear()
    }
}
