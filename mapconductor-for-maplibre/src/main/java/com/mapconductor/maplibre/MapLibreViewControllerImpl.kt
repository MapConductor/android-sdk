package com.mapconductor.maplibre

import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.MapViewState
import kotlinx.coroutines.CoroutineScope


typealias MapLibreDesignTypeChangeHandler = (MapLibreMapDesignType) -> Unit

class MapLibreMapViewControllerImpl(
    override val holder: MapViewHolder<*, *>,
    override val coroutine: CoroutineScope
) :
    BaseMapViewController(),
    MapLibreMapViewController {
    override suspend fun clearOverlays() {
        TODO("Not yet implemented")
    }

    override fun moveCamera(
        position: MapCameraPositionImpl,
        listener: MapViewState.MoveCameraCallback?
    ) {
        TODO("Not yet implemented")
    }

    override fun animateCamera(
        position: MapCameraPositionImpl,
        duration: Long,
        listener: MapViewState.MoveCameraCallback?
    ) {
        TODO("Not yet implemented")
    }

    override fun setMapDesignType(value: MapLibreMapDesignType) {
        TODO("Not yet implemented")
    }

    override fun setMapDesignTypeChangeListener(listener: MapLibreDesignTypeChangeHandler) {
        TODO("Not yet implemented")
    }

}
