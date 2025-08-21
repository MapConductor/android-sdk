package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.SceneView
import com.arcgismaps.mapping.view.SurfacePlacement
import com.mapconductor.arcgis.marker.ArcGISMarkerController
import com.mapconductor.arcgis.marker.ArcGISMarkerRenderer
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.StaticHolder
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.projection.WebMercator
import android.content.Context

typealias ArcGISMapViewHolder = MapViewHolder<WrapSceneView, SceneView>

object ArcGISViewControllerStore :
    StaticHolder<ArcGISMapViewController>() {
    fun hasCache(id: String): Boolean = this.has(id)

    fun getOrCreate(
        context: Context,
        id: String,
        options: ArcGISMapViewInitOptions,
    ): ArcGISMapViewController {
        val existing = this.get(id)
        if (existing != null) return existing

        val holder =
            ArcGISMapViewHolderImpl.create(
                context = context.applicationContext,
                options = options,
            )

        val controller =
            ArcGISMapViewController(
                holder = holder,
                markerController = getMarkerController(holder),
            )
        this.set(id, controller)
        return controller
    }

    private fun getMarkerController(holder: ArcGISMapViewHolder): ArcGISMarkerController {
        val hexGeocell =
            HexGeocell(
                projection = WebMercator,
                baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
            )
        val manager = MarkerManager<ArcGISActualMarker>(hexGeocell)

        val markerLayer: GraphicsOverlay =
            GraphicsOverlay().apply {
                sceneProperties.surfacePlacement = SurfacePlacement.Relative
            }

        val renderer =
            ArcGISMarkerRenderer(
                markerLayer = markerLayer,
                holder = holder,
            )

        val controller =
            ArcGISMarkerController(
                markerManager = manager,
                renderer = renderer,
            )
        return controller
    }
}
