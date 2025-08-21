package com.mapconductor.mapbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.InitState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import com.mapconductor.mapbox.MapboxMapDesign.Standard
import java.util.UUID
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface MapboxViewState : MapViewState<String>

class MapboxViewStateImpl(
    override val id: String,
    override val mapDesignType: MapboxDesignType,
    override val initCameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewStateImpl<String>(),
    MapboxViewState {
    internal var controller: IMapboxMapViewController? = null

    // Camera center position
    private val _cameraPosition = MutableStateFlow(initCameraPosition)
    override val cameraPosition: StateFlow<MapCameraPosition> = _cameraPosition.asStateFlow()

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
        val currentPosition = this.cameraPosition.value
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

    internal fun onCameraChange(cameraPosition: MapCameraPosition) {
        _cameraPosition.value = cameraPosition
    }
}

class MapboxMapViewSaver : BaseMapViewSaver<MapboxViewStateImpl>() {
    override fun extractCameraPosition(state: MapboxViewStateImpl): MapCameraPosition? = state.cameraPosition.value

    override fun saveMapDesign(
        state: MapboxViewStateImpl,
        bundle: Bundle,
    ) {
        bundle.putString("id", state.mapDesignType.id)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition,
    ): MapboxViewStateImpl =
        MapboxViewStateImpl(
            id = stateId,
            mapDesignType =
                MapboxMapDesign.Create(
                    layerId = mapDesignBundle?.getString("id") ?: Standard.id,
                ),
            initCameraPosition = cameraPosition,
        )

    override fun getStateId(state: MapboxViewStateImpl): String = state.id
}

@Composable
fun rememberMapboxMapViewState(
    mapDesign: MapboxDesignType = Standard,
    cameraPosition: IMapCameraPosition = MapCameraPosition.Default,
): MapboxViewStateImpl {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = MapboxMapViewSaver().createSaver(),
        ) {
            mutableStateOf(
                MapboxViewStateImpl(
                    id = stateId,
                    mapDesignType = mapDesign,
                    initCameraPosition = MapCameraPosition.from(cameraPosition),
                ),
            )
        }

    return state.value
}
