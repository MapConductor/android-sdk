package com.mapconductor.here

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.mapview.HereMap
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapRenderMode
import com.here.sdk.mapview.MapView
import com.here.sdk.mapview.MapViewOptions
import com.mapconductor.core.MapViewHolder

class MapViewHolderImpl private constructor(
    override val mapView: MapView
): MapViewHolder<MapView, HereMap> {
    override lateinit var map: HereMap

    companion object {
        private var mapCount: Int = 0

        fun create(context: Context, accessKeyId: String, accessKeySecret: String):
                MapViewHolder<MapView, HereMap> {

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

            val mapView = MapView(context, options).apply {
                onCreate(null)
                camera.lookAt(GeoCoordinates(0.0, 0.0), MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 4.0))
            }

            val holder = MapViewHolderImpl(mapView)
            holder.map = mapView.hereMap
            return holder
        }
    }

    override fun detach() {
        (mapView.parent as? ViewGroup)?.removeView(mapView)
    }

    override fun attachTo(container: ViewGroup) {
        if (mapView.parent === container) return

        this.detach()
        container.addView(mapView)
    }

    override fun destroy(owner: LifecycleOwner?) {
        mapView.onPause()
        mapView.onDestroy()
        mapCount--
        if (mapCount > 0) return

        // Dispose the shared instance when all maps are removed.
        SDKNativeEngine.getSharedInstance()?.dispose()
        SDKNativeEngine.setSharedInstance(null)
    }
}
