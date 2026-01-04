package com.mapconductor.here

import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.mapview.MapScene
import com.here.sdk.mapview.MapView
import com.mapconductor.core.map.MapViewHolderInterface
import com.mapconductor.core.map.StaticHolder
import android.content.Context
import android.content.pm.PackageManager

typealias HereViewHolderInterface = MapViewHolderInterface<MapView, MapScene>

object HereMapViewControllerStore : StaticHolder<HereMapViewController>() {
    private var mapCount: Int = 0

    fun initSDK(context: Context) {
        if (this.mapCount > 0) {
            return
        }

        // 初めて使うときはApplicationContextで認証する
        val accessKeyId = context.applicationContext.getHereAccessKeyId()
        val accessKeySecret = context.applicationContext.getHereAccessKeySecret()
        if (accessKeyId == null) {
            throw Exception(
                "<meta-data android:name=\"HERE_ACCESS_KEY_ID\" /> is required",
            )
        }
        if (accessKeySecret == null) {
            throw Exception(
                "<meta-data android:name=\"HERE_ACCESS_KEY_SECRET\" /> is required",
            )
        }

        val authenticationMode =
            AuthenticationMode.withKeySecret(
                accessKeyId,
                accessKeySecret,
            )
        val sdkOption = SDKOptions(authenticationMode)
        SDKNativeEngine.makeSharedInstance(context.applicationContext, sdkOption)
        this.mapCount++
    }
}

internal fun Context.getHereAccessKeyId(): String? =
    packageManager
        .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData
        ?.getString("HERE_ACCESS_KEY_ID")

internal fun Context.getHereAccessKeySecret(): String? =
    packageManager
        .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData
        ?.getString("HERE_ACCESS_KEY_SECRET")
