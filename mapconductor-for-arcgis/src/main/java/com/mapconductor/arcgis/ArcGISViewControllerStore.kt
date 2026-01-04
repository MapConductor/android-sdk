package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.SceneView
import com.mapconductor.arcgis.map.ArcGISMapViewController
import com.mapconductor.arcgis.map.WrapSceneView
import com.mapconductor.core.map.MapViewHolderInterface
import com.mapconductor.core.map.StaticHolder

typealias ArcGISMapViewHolderInterface = MapViewHolderInterface<WrapSceneView, SceneView>

object ArcGISViewControllerStore :
    StaticHolder<ArcGISMapViewController>()
