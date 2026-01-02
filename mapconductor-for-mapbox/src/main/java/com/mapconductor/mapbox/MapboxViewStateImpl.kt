package com.mapconductor.mapbox

import MapboxMapViewController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import com.mapconductor.mapbox.MapboxMapDesign.Standard
import java.util.UUID
import android.os.Bundle

interface MapboxViewState : MapViewState<MapboxDesignType>

class MapboxViewStateImpl(
    mapDesignType: MapboxDesignType,
    override val id: String,
    cameraPosition: MapCameraPositionImpl = MapCameraPositionImpl.Default,
) : MapViewStateImpl<MapboxDesignType>(),
    MapboxViewState {
    private var controller: MapboxMapViewController? = null

    private var _mapDesignType: MapboxDesignType = mapDesignType

    override var mapDesignType: MapboxDesignType
        set(value) {
            _mapDesignType = value
            this.controller?.setMapDesignType(value)
        }
        get() = _mapDesignType

    private var _cameraPosition: MapCameraPositionImpl = cameraPosition
    override val cameraPosition: MapCameraPositionImpl
        get() = _cameraPosition

    internal fun setController(controller: MapboxMapViewController) {
        this.controller = controller
        controller.moveCamera(this.cameraPosition)
    }

    internal fun onMapDesignTypeChange(value: MapboxDesignType) {
        _mapDesignType = value
    }

    override fun moveCameraTo(
        position: GeoPointImpl,
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
        cameraPosition: MapCameraPositionImpl,
        durationMillis: Long?,
    ) {
        controller?.let { ctrl ->
            val dstCameraPosition = MapCameraPositionImpl.from(cameraPosition)
            if (durationMillis == null || durationMillis == 0L) {
                ctrl.moveCamera(dstCameraPosition)
            } else {
                ctrl.animateCamera(dstCameraPosition, durationMillis)
            }
            return@let
        }
        this._cameraPosition = cameraPosition
    }

    internal fun updateCameraPosition(cameraPosition: MapCameraPositionImpl) {
        this._cameraPosition = cameraPosition
    }
}

class MapboxMapViewSaver : BaseMapViewSaver<MapboxViewStateImpl>() {
    override fun saveMapDesign(
        state: MapboxViewStateImpl,
        bundle: Bundle,
    ) {
        bundle.putString("id", state.mapDesignType.id)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPositionImpl,
    ): MapboxViewStateImpl =
        MapboxViewStateImpl(
            id = stateId,
            mapDesignType =
                MapboxMapDesign.Create(
                    layerId = mapDesignBundle?.getString("id") ?: Standard.id,
                ),
            cameraPosition = cameraPosition,
        )

    override fun getStateId(state: MapboxViewStateImpl): String = state.id
}

@Composable
fun rememberMapboxMapViewState(
    mapDesign: MapboxDesignType = Standard,
    cameraPosition: MapCameraPosition = MapCameraPositionImpl.Default,
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
                    cameraPosition = MapCameraPositionImpl.from(cameraPosition),
                ),
            )
        }

    return state.value
}
