package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.SceneView
import com.arcgismaps.mapping.view.SurfacePlacement
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayController
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayRenderer
import com.mapconductor.arcgis.marker.ArcGISMarkerController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayRenderer
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayController
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayRenderer
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.StaticHolder
import com.mapconductor.core.marker.MarkerRenderingStrategy
import android.content.Context

typealias ArcGISMapViewHolder = MapViewHolder<WrapSceneView, SceneView>

object ArcGISViewControllerStore :
    StaticHolder<ArcGISMapViewControllerImpl>() {

}
