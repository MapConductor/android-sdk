package com.mapconductor.here

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoCoordinatesUpdate
import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.mapview.HereMap
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapRenderMode
import com.here.sdk.mapview.MapView
import com.here.sdk.mapview.MapViewOptions

class MapViewHolder private constructor(
    val mapView: MapView
) {
    lateinit var map: HereMap

    companion object {
        private var mapCount: Int = 0

        fun create(context: Context, accessKeyId: String, accessKeySecret: String): MapViewHolder {

            if (this.mapCount == 0) {
                val authenticationMode = AuthenticationMode.withKeySecret(
                    accessKeyId,
                    accessKeySecret,
                )
                val options = SDKOptions(authenticationMode)
                SDKNativeEngine.makeSharedInstance(context, options)
            }
            this.mapCount++

            // TEXTUREモードにしないとデバイスが回転したときに再描画を適切に行わない
            val options = MapViewOptions()
            options.renderMode = MapRenderMode.TEXTURE

            val mapView = MapView(context, options).also { it ->
                it.onCreate(null)
                it.camera.lookAt(GeoCoordinates(0.0, 0.0), MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 4.0))
            }

            val holder = MapViewHolder(mapView)
            holder.map = mapView.hereMap
            return holder
        }
    }

    fun detach() {
        (mapView.parent as? ViewGroup)?.removeView(mapView)
    }

    fun attachTo(container: ViewGroup) {
        if (mapView.parent === container) return

        (mapView.parent as? ViewGroup)?.removeView(mapView)
        container.addView(mapView)
    }

    fun destroy() {
        mapView.onPause()
        mapView.onDestroy()
        MapViewHolder.mapCount--
        if (MapViewHolder.mapCount > 0) return

        // Dispose the shared instance when all maps are removed.
        SDKNativeEngine.getSharedInstance()?.dispose()
        SDKNativeEngine.setSharedInstance(null)
    }
}
