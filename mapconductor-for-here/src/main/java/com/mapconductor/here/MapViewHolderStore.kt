package com.mapconductor.here

import android.content.Context
import android.content.pm.PackageManager

object MapViewHolderStore {
    private val holders = mutableMapOf<String, MapViewHolder>()

    fun getOrCreate(
        id: String,
        context: Context,
    ): MapViewHolder {
        val existing = holders[id]
        if (existing != null) return existing

        val accessKeyId = context.applicationContext.getHereAccessKeyId()
        val accessKeySecret = context.applicationContext.getHereAccessKeySecret()
        if (accessKeyId == null) throw Exception("<meta-data android:name=\"HERE_ACCESS_KEY_ID\" /> is required")
        if (accessKeySecret == null) throw Exception("<meta-data android:name=\"HERE_ACCESS_KEY_SECRET\" /> is required")

        val newHolder = MapViewHolder.create(
            context.applicationContext,
            accessKeyId,
            accessKeySecret,
        )
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
internal fun Context.getHereAccessKeyId(): String? =
    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData?.getString("HERE_ACCESS_KEY_ID")

internal fun Context.getHereAccessKeySecret(): String? =
    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData?.getString("HERE_ACCESS_KEY_SECRET")