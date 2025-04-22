package com.mapconductor.here

import android.content.Context
import android.content.pm.PackageManager
import com.here.sdk.mapview.HereMap
import com.here.sdk.mapview.MapView
import com.mapconductor.core.MapViewHolder
import com.mapconductor.core.MapViewHolderStoreBaseSync

internal object MapViewHolderStore : MapViewHolderStoreBaseSync<MapView, HereMap>() {

    override fun getOrCreate(
        context: Context,
        id: String,
    ): MapViewHolder<MapView, HereMap> {
        val existing = this.get(id)
        if (existing != null) return existing

        val accessKeyId = context.applicationContext.getHereAccessKeyId()
        val accessKeySecret = context.applicationContext.getHereAccessKeySecret()
        if (accessKeyId == null) throw Exception("<meta-data android:name=\"HERE_ACCESS_KEY_ID\" /> is required")
        if (accessKeySecret == null) throw Exception("<meta-data android:name=\"HERE_ACCESS_KEY_SECRET\" /> is required")

        val newHolder = MapViewHolderImpl.create(
            context.applicationContext,
            accessKeyId,
            accessKeySecret,
        )
        this.set(id, newHolder)
        return newHolder
    }
}

internal fun Context.getHereAccessKeyId(): String? =
    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData?.getString("HERE_ACCESS_KEY_ID")

internal fun Context.getHereAccessKeySecret(): String? =
    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData?.getString("HERE_ACCESS_KEY_SECRET")