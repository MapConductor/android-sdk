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
import java.util.UUID
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface GoogleMapViewState : MapViewState<Int>

class GoogleMapViewStateImpl(
    override val id: String,
    override val mapDesignType: GoogleMapDesignType,
    override val initCameraPosition: MapCameraPosition,
) : MapViewStateImpl<Int>(),
    GoogleMapViewState {
    // Map padding
    private val _padding = MutableStateFlow(MapPaddingsImpl.Zeros)
    val padding: StateFlow<MapPaddingsImpl> = _padding.asStateFlow()

    // Camera position
    private val _cameraPosition = MutableStateFlow<MapCameraPosition?>(initCameraPosition)
    override val cameraPosition: StateFlow<MapCameraPosition?> = _cameraPosition.asStateFlow()

    internal var controller: IGoogleMapViewController? = null

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
        this.cameraPosition.value?.let { current ->
            val newPosition =
                current.copy(
                    position = position,
                )
            this.moveCameraTo(newPosition, durationMs, listener)
        }
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
class GoogleMapViewSaver : BaseMapViewSaver<GoogleMapViewStateImpl>() {
    override fun extractCameraPosition(state: GoogleMapViewStateImpl): MapCameraPosition? = state.cameraPosition.value

    override fun saveMapDesign(
        state: GoogleMapViewStateImpl,
        bundle: Bundle,
    ) {
        bundle.putInt("id", state.mapDesignType.id)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition,
    ): GoogleMapViewStateImpl =
        GoogleMapViewStateImpl(
            id = stateId,
            mapDesignType =
                GoogleMapDesign.Create(
                    id = mapDesignBundle?.getInt("id") ?: GoogleMapDesign.Normal.id,
                ),
            initCameraPosition = cameraPosition,
        )

    override fun getStateId(state: GoogleMapViewStateImpl): String = state.id
}

@Composable
fun rememberGoogleMapViewState(
    mapDesign: GoogleMapDesignType = GoogleMapDesign.Normal,
    cameraPosition: IMapCameraPosition = MapCameraPosition.Default,
): GoogleMapViewStateImpl {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = GoogleMapViewSaver().createSaver(),
        ) {
            mutableStateOf(
                GoogleMapViewStateImpl(
                    id = stateId,
                    mapDesignType = mapDesign,
                    initCameraPosition = MapCameraPosition.from(cameraPosition),
                ),
            )
        }

    return state.value
}
