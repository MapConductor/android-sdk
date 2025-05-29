package com.mapconductor.googlemaps

import com.google.android.gms.maps.model.CameraPosition
import com.mapconductor.core.marker.MarkerState

interface IGoogleMapEventHandler {
    fun onCameraMoveStart(cameraPosition: CameraPosition)
    fun onCameraMove(cameraPosition: CameraPosition)
    fun onCameraMoveEnd(cameraPosition: CameraPosition)
    fun onMarkerAdd(state: MarkerState)
    fun onMarkerRemove(id: String)
}