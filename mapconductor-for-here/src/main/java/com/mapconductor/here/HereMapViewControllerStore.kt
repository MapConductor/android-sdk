package com.mapconductor.here

import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.mapview.MapScene
import com.here.sdk.mapview.MapView
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.StaticHolder
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.here.marker.HereMapMarkerRenderer
import com.mapconductor.here.marker.HereMarkerController
import com.mapconductor.here.polygon.HereMapPolygonRenderer
import com.mapconductor.here.polygon.HerePolygonController
import com.mapconductor.here.polyline.HerePolylineController
import com.mapconductor.here.polyline.HerePolylineOverlayRenderer
import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

typealias HereMapViewHolder = MapViewHolder<MapView, MapScene>

object HereMapViewControllerStore : StaticHolder<HereMapViewControllerImpl>() {
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
    ): HereMapViewControllerImpl {
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
            HereMapViewControllerImpl(
                holder = holder,
                markerController = getMarkerController(holder),
                polylineController = getPolylineController(holder),
                polygonController = getPolygonController(holder),
            )
        this.set(id, controller)
        return controller
    }

    private fun getPolylineController(holder: HereMapViewHolder): HerePolylineController {
        val renderer =
            HerePolylineOverlayRenderer(
                holder = holder,
            )

        val controller =
            HerePolylineController(
                renderer = renderer,
            )
        return controller
    }

    private fun getMarkerController(holder: HereMapViewHolder): HereMarkerController {
        val hexGeocell =
            HexGeocell(
                projection = WebMercator,
                baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
            )
        val manager = MarkerManager<HereMapActualMarker>(hexGeocell)

        val renderer =
            HereMapMarkerRenderer(
                holder = holder,
            )

        val controller =
            HereMarkerController(
                markerManager = manager,
                renderer = renderer,
            )
        return controller
    }

    private fun getPolygonController(holder: HereMapViewHolder): HerePolygonController {
        val renderer =
            HereMapPolygonRenderer(
                holder = holder,
                coroutine = CoroutineScope(Dispatchers.Default),
            )

        val controller =
            HerePolygonController(
                renderer = renderer,
            )
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
