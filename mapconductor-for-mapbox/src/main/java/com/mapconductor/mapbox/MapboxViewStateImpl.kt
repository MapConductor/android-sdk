package com.mapconductor.mapbox

import MapboxMapViewControllerInterface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.mapbox.MapboxMapDesign.Standard
import java.util.UUID
import android.os.Bundle

interface MapboxViewStateInterface : MapViewStateInterface<MapboxDesignType>

class MapboxViewState(
    mapDesignType: MapboxDesignType,
    override val id: String,
    cameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewState<MapboxDesignType>(),
    MapboxViewStateInterface {
    private var controller: MapboxMapViewControllerInterface? = null

    private var _mapDesignType: MapboxDesignType = mapDesignType

    override var mapDesignType: MapboxDesignType
        set(value) {
            _mapDesignType = value
            this.controller?.setMapDesignType(value)
        }
        get() = _mapDesignType

    private var _cameraPosition: MapCameraPosition = cameraPosition
    override val cameraPosition: MapCameraPosition
        get() = _cameraPosition

    internal fun setController(controller: MapboxMapViewControllerInterface) {
        this.controller = controller
        controller.moveCamera(this.cameraPosition)
    }

    internal fun onMapDesignTypeChange(value: MapboxDesignType) {
        _mapDesignType = value
    }

    override fun moveCameraTo(
        position: GeoPoint,
        durationMillis: Long?,
    ) {
        val currentPosition = this.cameraPosition
        val newPosition =
            currentPosition.copy(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMillis)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getMapViewHolder(): MapboxMapViewHolder? = controller?.holder as? MapboxMapViewHolder

    override fun moveCameraTo(
        cameraPosition: MapCameraPosition,
        durationMillis: Long?,
    ) {
        controller?.let { ctrl ->
            val dstCameraPosition = MapCameraPosition.from(cameraPosition)
            if (durationMillis == null || durationMillis == 0L) {
                ctrl.moveCamera(dstCameraPosition)
            } else {
                ctrl.animateCamera(dstCameraPosition, durationMillis)
            }
            return@let
        }
        this._cameraPosition = cameraPosition
    }

    internal fun updateCameraPosition(cameraPosition: MapCameraPosition) {
        this._cameraPosition = cameraPosition
    }
}

class MapboxMapViewSaver : BaseMapViewSaver<MapboxViewState>() {
    override fun saveMapDesign(
        state: MapboxViewState,
        bundle: Bundle,
    ) {
        bundle.putString("id", state.mapDesignType.id)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition,
    ): MapboxViewState =
        MapboxViewState(
            id = stateId,
            mapDesignType =
                MapboxMapDesign.Create(
                    layerId = mapDesignBundle?.getString("id") ?: Standard.id,
                ),
            cameraPosition = cameraPosition,
        )

    override fun getStateId(state: MapboxViewState): String = state.id
}

@Composable
fun rememberMapboxMapViewState(
    mapDesign: MapboxDesignType = Standard,
    cameraPosition: MapCameraPositionInterface = MapCameraPosition.Default,
): MapboxViewState {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = MapboxMapViewSaver().createSaver(),
        ) {
            mutableStateOf(
                MapboxViewState(
                    id = stateId,
                    mapDesignType = mapDesign,
                    cameraPosition = MapCameraPosition.from(cameraPosition),
                ),
            )
        }

    return state.value
}
