package com.mapconductor.mapbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapbox.maps.CameraState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.InitState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import com.mapconductor.mapbox.MapboxMapDesign.Standard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import android.os.Bundle

interface IMapboxMapViewState : MapViewState<String>

class MapboxMapViewState(
    override val id: String,
    override val mapDesignType: MapboxDesignType,
    override val initCameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewStateImpl<String>(),
    IMapboxMapViewState {
    internal var controller: IMapboxMapViewController? = null

    // Camera center position
    private val cameraState = MutableStateFlow<CameraState>(initCameraPosition.toCameraState())
    override val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraState.map { it.toMapCameraPosition() }.stateIn(
            scope = this.mainCoroutine,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

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
        val currentPosition = this.mapCameraPosition.value
        if (currentPosition == null) {
            listener?.onComplete(false)
            return
        }
        val newPosition =
            currentPosition.copy(
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
        if (controller == null) {
            listener?.onComplete(false)
            return
        }
        if (durationMs == 0L) {
            controller!!.moveCamera(dstCameraPosition, listener)
        } else {
            controller!!.animateCamera(dstCameraPosition, durationMs.toLong(), listener)
        }
    }

    internal fun OnCameraChange(state: CameraState) {
        cameraState.value = state
    }
}

class MapboxMapViewSaver : BaseMapViewSaver<MapboxMapViewState>() {
    override fun extractCameraPosition(state: MapboxMapViewState): MapCameraPosition? = state.mapCameraPosition.value

    override fun saveMapDesign(
        state: MapboxMapViewState,
        bundle: Bundle,
    ) {
        bundle.putString("id", state.mapDesignType.id)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition,
    ): MapboxMapViewState =
        MapboxMapViewState(
            id = stateId,
            mapDesignType =
                MapboxMapDesign.Create(
                    layerId = mapDesignBundle?.getString("id") ?: Standard.id,
                ),
            initCameraPosition = cameraPosition,
        )

    override fun getStateId(state: MapboxMapViewState): String = state.id
}

@Composable
fun rememberMapboxMapViewState(
    mapDesign: MapboxDesignType = Standard,
    cameraPosition: IMapCameraPosition = MapCameraPosition.Default,
): MapboxMapViewState {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = MapboxMapViewSaver().createSaver(),
        ) {
            mutableStateOf(
                MapboxMapViewState(
                    id = stateId,
                    mapDesignType = mapDesign,
                    initCameraPosition = MapCameraPosition.from(cameraPosition),
                ),
            )
        }

    return state.value
}
