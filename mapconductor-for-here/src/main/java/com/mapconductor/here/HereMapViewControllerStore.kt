package com.mapconductor.here

import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.mapview.HereMap
import com.here.sdk.mapview.MapView
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.StaticHolder
import android.content.Context
import android.content.pm.PackageManager

typealias HereMapViewHolder = MapViewHolder<MapView, HereMap>

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

    fun getOrCreate(
        context: Context,
        id: String,
        options: HereMapViewInitOptions,
    ): HereMapViewController {
        val existing = this.get(id)
        if (existing != null) {
            return existing
        }
        initSDK(context.applicationContext)

        val holder =
            HereMapViewHolderImpl.create(
                context.applicationContext,
            )

//        val mapView = newHolder.mapView
//        options.let { it ->
//            suspendCancellableCoroutine { cont ->
//                mapView.mapScene.loadScene(it.scheme) { mapError ->
//                    if (mapError != null) {
//                        // Log.e("HereMapViewState", "Loading map failed: mapError: " + mapError.name)
//                        cont.resumeWithException(IllegalStateException(mapError.toString()))
//                        return@loadScene
//                    }
//
//                    mapView.camera.applyUpdate(
//                        options.camera.toMapCameraUpdate(),
//                    )
//                    cont.resume(Unit)
//                }
//            }
//        }

        val controller =
            HereMapViewController(
                holder = holder,
            )
        this.set(id, controller)
        return controller
    }

//    fun release() {
//        mapCount--
//        if (mapCount > 0) return
//
//        // Dispose the shared instance when all maps are removed.
//        SDKNativeEngine.getSharedInstance()?.dispose()
//        SDKNativeEngine.setSharedInstance(null)
//    }
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
