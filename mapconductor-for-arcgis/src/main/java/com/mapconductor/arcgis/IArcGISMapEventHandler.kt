package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.Camera
import com.mapconductor.core.marker.MarkerState

interface IArcGISMapEventHandler {
    fun onCameraMove(cameraPosition: Camera)

    fun onMarkerRemove(id: String)

    fun onMarkerAdd(state: MarkerState)
}
