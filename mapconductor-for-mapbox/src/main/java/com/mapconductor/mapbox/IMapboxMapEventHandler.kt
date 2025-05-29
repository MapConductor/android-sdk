package com.mapconductor.mapbox

import com.mapbox.maps.CameraState
import com.mapconductor.core.marker.MarkerState

internal interface IMapboxMapEventHandler {
    fun onCameraMove(cameraState: CameraState)
    fun onMarkerAdd(state: MarkerState)
    fun onMarkerRemove(id: String)
}