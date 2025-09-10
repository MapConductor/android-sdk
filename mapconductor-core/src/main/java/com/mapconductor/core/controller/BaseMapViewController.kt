package com.mapconductor.core.controller

import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler

abstract class BaseMapViewController : MapViewController {
    private var overlayControllers = mutableListOf<OverlayController<*, *, *>>()
    protected var cameraMoveCallback: OnCameraMoveHandler? = null
    protected var mapClickCallback: OnMapEventHandler? = null
    protected var mapLongClickCallback: OnMapEventHandler? = null

    override fun setCameraMoveListener(listener: OnCameraMoveHandler?) {
        this.cameraMoveCallback = listener
    }

    override fun setMapClickListener(listener: OnMapEventHandler?) {
        this.mapClickCallback = listener
    }

    override fun setMapLongClickListener(listener: OnMapEventHandler?) {
        this.mapClickCallback = listener
    }
    protected fun registerController(controller: OverlayController<*, *, *>) {
        overlayControllers.add(controller)
    }

    protected suspend fun notifyMapCameraPosition(mapCameraPosition: MapCameraPosition) {
        overlayControllers.forEach {
            it.onCameraChanged(mapCameraPosition)
        }
        cameraMoveCallback?.let { callBack ->
            callBack(mapCameraPosition)
        }
    }
}
