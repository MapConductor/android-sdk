package com.mapconductor.openmobilemaps

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.Lifecycle
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapViewHolder
import io.openmobilemaps.mapscore.map.util.MapViewInterface
import io.openmobilemaps.mapscore.map.view.MapView
import io.openmobilemaps.mapscore.shared.map.MapConfig
import io.openmobilemaps.mapscore.shared.map.coordinates.CoordinateSystemFactory
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

typealias OpenMobileMapViewHolder = MapViewHolder<FrameLayout, MapViewInterface>

class OpenMobileMapViewHolderImpl private constructor(
    override val mapView: FrameLayout,
    override val map: MapViewInterface,
) : OpenMobileMapViewHolder {

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        return null
    }

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? {
        return null
    }

    companion object {
        fun create(
            context: Context,
            lifecycle: Lifecycle,
        ): OpenMobileMapViewHolder {

            val mapView = MapView(context)
            mapView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            val container = FrameLayout(context)
            container.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            container.addView(mapView)
            val coordinateSystem = CoordinateSystemFactory.getEpsg3857System()
            val config = MapConfig(coordinateSystem)
            mapView.setupMap(config)
            mapView.registerLifecycle(lifecycle)

            val holder = OpenMobileMapViewHolderImpl(
                mapView = container,
                map = mapView,
            )
            return holder
        }
    }
}
