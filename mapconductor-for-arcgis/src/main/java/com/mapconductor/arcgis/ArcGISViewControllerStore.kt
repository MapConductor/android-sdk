package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.SceneView
import com.mapconductor.arcgis.map.ArcGISMapViewControllerImpl
import com.mapconductor.arcgis.map.WrapSceneView
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.StaticHolder

typealias ArcGISMapViewHolder = MapViewHolder<WrapSceneView, SceneView>

object ArcGISViewControllerStore :
    StaticHolder<ArcGISMapViewControllerImpl>()
