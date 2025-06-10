package com.mapconductor.googlemaps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.android.gms.maps.model.CameraPosition
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.InitState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapPaddingsImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import android.os.Bundle

interface IGoogleMapViewState : MapViewState<Int>

class GoogleMapViewState(
    override val id: String,
    override val mapDesignType: GoogleMapDesignType,
    override val initCameraPosition: MapCameraPosition,
) : MapViewStateImpl<Int>(),
    IGoogleMapViewState {
    // Map padding
    private val _padding = MutableStateFlow(MapPaddingsImpl.Zeros)
    val padding: StateFlow<MapPaddingsImpl> = _padding.asStateFlow()

    // Camera position
    private val cameraPosition = MutableStateFlow<CameraPosition>(initCameraPosition.toCameraPosition())
    override val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraPosition.map { it.toMapCameraPosition(padding.value) }.stateIn(
            scope = mainCoroutine,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

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
        val currCameraPosition = this.mapCameraPosition.value
        if (currCameraPosition == null) {
            listener?.onComplete(false)
            return
        }
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

    internal fun OnCameraChange(cameraPosition: CameraPosition) {
        this.cameraPosition.value = cameraPosition
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
    override fun extractCameraPosition(state: GoogleMapViewState): MapCameraPosition? = state.mapCameraPosition.value

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
    mapDesign: GoogleMapDesignType = GoogleMapDesign.Normal,
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
