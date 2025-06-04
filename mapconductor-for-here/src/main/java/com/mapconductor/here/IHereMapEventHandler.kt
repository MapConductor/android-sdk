package com.mapconductor.here

import com.here.sdk.mapview.MapCamera
import com.mapconductor.core.marker.MarkerState

interface IHereMapEventHandler {
    fun onCameraMove(cameraState: MapCamera.State)

    fun onMarkerRemove(id: String)

    fun onMarkerAdd(state: MarkerState)
}
