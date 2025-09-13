package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.SceneView
import com.arcgismaps.mapping.view.SurfacePlacement
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayController
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayRenderer
import com.mapconductor.arcgis.marker.ArcGISMarkerController
import com.mapconductor.arcgis.marker.ArcGISMarkerRenderer
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayRenderer
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayController
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayRenderer
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.StaticHolder
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.projection.WebMercator
import android.content.Context

typealias ArcGISMapViewHolder = MapViewHolder<WrapSceneView, SceneView>

object ArcGISViewControllerStore :
    StaticHolder<ArcGISMapViewControllerImpl>() {
    fun hasCache(id: String): Boolean = this.has(id)

    suspend fun getOrCreate(
        context: Context,
        id: String,
        options: ArcGISMapViewInitOptions,
        markerRenderingStrategy: MarkerRenderingStrategy<ArcGISActualMarker>? = null,
    ): ArcGISMapViewControllerImpl {
        val existing = this.get(id)
        if (existing != null) return existing

        val holder =
            ArcGISMapViewHolderImpl.create(
                context = context.applicationContext,
                options = options,
            )

        val controller =
            ArcGISMapViewControllerImpl(
                holder = holder,
                markerController =
                    getMarkerController(
                        holder = holder,
                        renderingStrategy = markerRenderingStrategy,
                    ),
                polylineController = getPolylineController(holder),
                polygonController = getPolygonController(holder),
                circleController = getCircleController(holder),
            )
        this.set(id, controller)
        return controller
    }

    private fun getCircleController(holder: ArcGISMapViewHolder): ArcGISCircleOverlayController {
        val circleLayer: GraphicsOverlay =
            GraphicsOverlay().apply {
                sceneProperties.surfacePlacement = SurfacePlacement.DrapedFlat
            }

        val renderer =
            ArcGISCircleOverlayRenderer(
                circleLayer = circleLayer,
                holder = holder,
            )

        val controller =
            ArcGISCircleOverlayController(
                renderer = renderer,
            )
        return controller
    }

    private fun getPolylineController(holder: ArcGISMapViewHolder): ArcGISPolylineOverlayController {
        val polylineLayer: GraphicsOverlay =
            GraphicsOverlay().apply {
                sceneProperties.surfacePlacement = SurfacePlacement.DrapedBillboarded
            }

        val renderer =
            ArcGISPolylineOverlayRenderer(
                polylineLayer = polylineLayer,
                holder = holder,
            )

        val controller =
            ArcGISPolylineOverlayController(
                renderer = renderer,
            )
        return controller
    }

    private fun getPolygonController(holder: ArcGISMapViewHolder): ArcGISPolygonOverlayController {
        val polygonLayer: GraphicsOverlay =
            GraphicsOverlay().apply {
                sceneProperties.surfacePlacement = SurfacePlacement.DrapedBillboarded
            }

        val renderer =
            ArcGISPolygonOverlayRenderer(
                polygonLayer = polygonLayer,
                holder = holder,
            )

        val controller =
            ArcGISPolygonOverlayController(
                renderer = renderer,
            )
        return controller
    }

    private fun getMarkerController(
        holder: ArcGISMapViewHolder,
        renderingStrategy: MarkerRenderingStrategy<ArcGISActualMarker>? = null,
    ): ArcGISMarkerController {
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
                renderingStrategy = renderingStrategy,
            )
        return controller
    }
}
