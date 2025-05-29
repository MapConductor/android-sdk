package com.mapconductor.here

import android.content.Context
import com.here.sdk.mapview.HereMap
import com.here.sdk.mapview.MapRenderMode
import com.here.sdk.mapview.MapView
import com.here.sdk.mapview.MapViewOptions
import com.mapconductor.core.MapViewHolder

internal class HereMapViewHolderImpl private constructor(
    override val mapView: MapView,
): MapViewHolder<MapView, HereMap> {
    override lateinit var map: HereMap

    companion object {

        fun create(
            context: Context,
        ): MapViewHolder<MapView, HereMap> {

            // TEXTUREモードにしないとデバイスが回転したときに再描画を適切に行わない
            val viewOptions = MapViewOptions().also {
                it.renderMode = MapRenderMode.TEXTURE
            }

            val mapView = MapView(context, viewOptions).apply {
                onCreate(null)
                onResume()
            }

            val holder = HereMapViewHolderImpl(mapView)
            holder.map = mapView.hereMap
            return holder
        }
    }
}
