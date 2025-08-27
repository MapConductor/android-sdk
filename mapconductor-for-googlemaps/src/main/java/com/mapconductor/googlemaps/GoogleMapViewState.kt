package com.mapconductor.googlemaps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.InitState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapPaddingsImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import com.mapconductor.core.state.StateOrValue
import java.util.UUID
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface IGoogleMapViewState : MapViewState<GoogleMapDesign>

class GoogleMapViewState(
    override val id: String,
    override var mapDesignType: GoogleMapDesign,
    override val initCameraPosition: MapCameraPosition,
) : MapViewStateImpl<GoogleMapDesign>(),
    IGoogleMapViewState {
    // Map padding
    private val _padding = MutableStateFlow(MapPaddingsImpl.Zeros)
    val padding: StateFlow<MapPaddingsImpl> = _padding.asStateFlow()

    // Camera position
    private val _cameraPosition = MutableStateFlow(initCameraPosition)
    override val cameraPosition: StateFlow<MapCameraPosition> = _cameraPosition.asStateFlow()

    internal var controller: IGoogleMapViewController? = null

    override fun changeMapDesignType(
        value: GoogleMapDesign,
    ){
        this.mapDesignType = value
        this.controller?.changeMapDesign(value.getValue())
    }

    override fun moveCameraTo(
        position: GeoPoint,
        durationMs: Long,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        if (this.isInitialized.value != InitState.Initialized) {
            this.warningLog("moveCameraTo() called before map is initialized.")
            listener?.onComplete(false)
            return
        }
        val currCameraPosition = this.cameraPosition.value
        val newPosition =
            currCameraPosition.copy(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMs, listener)
    }

    override fun moveCameraTo(
        cameraPosition: MapCameraPosition,
        durationMs: Long,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        if (this.isInitialized.value != InitState.Initialized) {
            this.warningLog("moveCameraTo() called before map is initialized.")
            listener?.onComplete(false)
            return
        }

        val dstCameraPosition = MapCameraPosition.from(cameraPosition)
        controller?.let {
            if (durationMs == 0L) {
                it.moveCamera(dstCameraPosition, listener)
            } else {
                it.animateCamera(dstCameraPosition, durationMs.toInt(), listener)
            }
        } ?: listener?.onComplete(false)
    }

    internal fun onCameraChange(cameraPosition: MapCameraPosition) {
        this._cameraPosition.value = cameraPosition
    }

//    override fun onCameraMoveStart(cameraPosition: CameraPosition) {
//        this._cameraPosition.value = cameraPosition
//    }
//
//    override fun onCameraMove(cameraPosition: CameraPosition) {
//        this._cameraPosition.value = cameraPosition
//    }
//
//    override fun onCameraMoveEnd(cameraPosition: CameraPosition) {
//        this._cameraPosition.value = cameraPosition
//    }
//
//    override fun onMarkerAdd(state: MarkerState) {
//        // Do nothing here
//    }
//
//    override fun onMarkerRemove(id: String) {
//        // Do nothing here
//    }
}

// GoogleMapViewSaver implementation
class GoogleMapViewSaver : BaseMapViewSaver<GoogleMapViewState>() {
    override fun extractCameraPosition(state: GoogleMapViewState): MapCameraPosition? = state.cameraPosition.value

    override fun saveMapDesign(
        state: GoogleMapViewState,
        bundle: Bundle,
    ) {
        bundle.putInt("id", state.mapDesignType.id)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition,
    ): GoogleMapViewState =
        GoogleMapViewState(
            id = stateId,
            mapDesignType =
                GoogleMapDesign.Create(
                    id = mapDesignBundle?.getInt("id") ?: GoogleMapDesign.Normal.id,
                ),
            initCameraPosition = cameraPosition,
        )

    override fun getStateId(state: GoogleMapViewState): String = state.id
}

@Composable
fun rememberGoogleMapViewState(
    mapDesign: GoogleMapDesign = GoogleMapDesign.Normal,
    cameraPosition: IMapCameraPosition = MapCameraPosition.Default,
): GoogleMapViewState {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = GoogleMapViewSaver().createSaver(),
        ) {
            mutableStateOf(
                GoogleMapViewState(
                    id = stateId,
                    mapDesignType = mapDesign,
                    initCameraPosition = MapCameraPosition.from(cameraPosition),
                ),
            )
        }

    return state.value
}
