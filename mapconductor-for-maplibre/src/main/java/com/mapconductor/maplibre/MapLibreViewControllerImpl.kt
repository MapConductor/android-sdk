package com.mapconductor.maplibre

import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import org.maplibre.android.camera.CameraUpdateFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


typealias MapLibreDesignTypeChangeHandler = (MapLibreMapDesignType) -> Unit

class MapLibreViewControllerImpl(
    override val holder: MapLibreViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val backCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController(),
    MapLibreViewController {
    override suspend fun clearOverlays() {
        TODO("Not yet implemented")
    }

    override fun moveCamera(
        position: MapCameraPositionImpl,
        listener: MapViewState.MoveCameraCallback?
    ) {
        coroutine.launch {
            val cameraUpdate = CameraUpdateFactory
                .newCameraPosition(position.toCameraPosition())
            holder.map.moveCamera(cameraUpdate)
            listener?.onComplete()
        }
    }

    override fun animateCamera(
        position: MapCameraPositionImpl,
        duration: Long,
        listener: MapViewState.MoveCameraCallback?
    ) {
        coroutine.launch {
            val cameraUpdate = CameraUpdateFactory
                .newCameraPosition(position.toCameraPosition())
            holder.map.animateCamera(cameraUpdate, duration.toInt())
            listener?.onComplete()
        }
    }

    private var mapDesignType: MapLibreMapDesignType = MapLibreMapDesign.DemoTiles

    private var mapDesignTypeChangeListener: MapLibreDesignTypeChangeHandler? = null

    override fun setMapDesignType(value: MapLibreMapDesignType) {
        coroutine.launch {
            holder.map.setStyle(value.styleJsonURL)
        }
    }

    override fun setMapDesignTypeChangeListener(listener: MapLibreDesignTypeChangeHandler) {
        mapDesignTypeChangeListener = listener
        listener(mapDesignType)
    }

}
