package com.mapconductor.mapbox

import com.mapbox.common.MapboxOptions
import android.content.Context
import android.content.pm.PackageManager

fun MapboxInitSDK(context: Context) {
    // 初めて使うときはApplicationContextで認証する
    val accessToken = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData
        ?.getString("MAPBOX_ACCESS_TOKEN")
    if (accessToken == null) {
        throw Exception(
            "<meta-data android:name=\"MAPBOX_ACCESS_TOKEN\" /> is required",
        )
    }
    MapboxOptions.accessToken = accessToken
}

