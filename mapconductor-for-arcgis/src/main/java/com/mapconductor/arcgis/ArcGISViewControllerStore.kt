package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.SceneView
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.StaticHolder

typealias ArcGISMapViewHolder = MapViewHolder<WrapSceneView, SceneView>

object ArcGISViewControllerStore :
    StaticHolder<ArcGISMapViewControllerImpl>()
