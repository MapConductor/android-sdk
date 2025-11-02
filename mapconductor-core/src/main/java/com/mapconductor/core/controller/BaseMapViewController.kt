package com.mapconductor.core.controller

import com.mapconductor.core.map.InternalOnMapLoadedHandler
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler

abstract class BaseMapViewController : MapViewController {
    private var overlayControllers = mutableListOf<OverlayController<*, *, *>>()
    protected var cameraMoveCallback: OnCameraMoveHandler? = null
    protected var mapClickCallback: OnMapEventHandler? = null
    protected var mapLongClickCallback: OnMapEventHandler? = null

    protected var mapLoadedCallback: InternalOnMapLoadedHandler? = null

    override fun setCameraMoveListener(listener: OnCameraMoveHandler?) {
        this.cameraMoveCallback = listener
    }

    override fun setMapClickListener(listener: OnMapEventHandler?) {
        this.mapClickCallback = listener
    }

    override fun setMapLongClickListener(listener: OnMapEventHandler?) {
        this.mapLongClickCallback = listener
    }

    protected fun registerController(controller: OverlayController<*, *, *>) {
        overlayControllers.add(controller)
    }

    protected suspend fun notifyMapCameraPosition(mapCameraPosition: MapCameraPositionImpl) {
        overlayControllers.forEach {
            it.onCameraChanged(mapCameraPosition)
        }
        cameraMoveCallback?.let { callBack ->
            callBack(mapCameraPosition)
        }
    }
}
