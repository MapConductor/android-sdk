package com.mapconductor.openmobilemaps

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.InitState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapDesignType
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import io.openmobilemaps.mapscore.shared.map.MapCameraInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface IOpenMobileMapViewState : MapViewState<String>


class OpenMobileMapViewState (
    override val id: String,
    override val initCameraPosition: MapCameraPosition,
    override val mapDesignType: OpenMobileMapDesign,
) : MapViewStateImpl<String>(),
    IOpenMobileMapViewState {

    private val cameraPosition = MutableStateFlow<MapCameraPosition?>(null)
    override val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraPosition.stateIn(
            scope = mainCoroutine,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    override fun moveCameraTo(
        cameraPosition: MapCameraPosition,
        durationMs: Long,
        listener: MapViewState.MoveCameraCallback?
    ) {
        if (this.isInitialized.value != InitState.Initialized) {
            this.warningLog("moveCameraTo() called before map is initialized.")
            listener?.onComplete(false)
            return
        }
    }

    override fun moveCameraTo(position: GeoPoint, durationMs: Long, listener: MapViewState.MoveCameraCallback?) {
        if (this.isInitialized.value != InitState.Initialized) {
            this.warningLog("moveCameraTo() called before map is initialized.")
            listener?.onComplete(false)
            return
        }
    }

}
