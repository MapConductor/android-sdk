package com.mapconductor.mapbox

import MapboxMapViewController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.InitState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import com.mapconductor.mapbox.MapboxMapDesign.Standard
import java.util.UUID
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface MapboxViewState : MapViewState<MapboxDesignType>

class MapboxViewStateImpl(
    mapDesignType: MapboxDesignType,
    override val id: String,
    override val initCameraPosition: MapCameraPositionImpl = MapCameraPositionImpl.Default,
) : MapViewStateImpl<MapboxDesignType>(),
    MapboxViewState {
    private var controller: MapboxMapViewController? = null

    // Camera center position
    private val _cameraPosition = MutableStateFlow(initCameraPosition)
    override val cameraPosition: StateFlow<MapCameraPositionImpl> = _cameraPosition.asStateFlow()

    private var _mapDesignType: MapboxDesignType = mapDesignType

    override var mapDesignType: MapboxDesignType
        set(value) {
            value?.let {
                _mapDesignType = it
                this.controller?.setMapDesignType(it)
            }
        }
        get() = _mapDesignType

    internal fun setController(controller: MapboxMapViewController) {
        this.controller = controller
        _mapDesignType?.let {
            controller.setMapDesignType(it)
        }
        controller.moveCamera(_cameraPosition.value)
    }

    internal fun onMapDesignTypeChange(value: MapboxDesignType) {
        _mapDesignType = value
    }

    override fun moveCameraTo(
        position: GeoPointImpl,
        durationMs: Long?,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        if (this.isInitialized.value != InitState.Initialized) {
            _cameraPosition.value =
                MapCameraPositionImpl(
                    position = position,
                )
            listener?.onComplete()
            return
        }
        val currentPosition = this.cameraPosition.value
        val newPosition =
            currentPosition.copy(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMs, listener)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getMapViewHolder(): MapboxMapViewHolder? = controller?.holder as? MapboxMapViewHolder

    override fun moveCameraTo(
        cameraPosition: MapCameraPositionImpl,
        durationMs: Long?,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        controller?.let { ctrl ->
            if (this.isInitialized.value == InitState.Initialized) {
                val dstCameraPosition = MapCameraPositionImpl.from(cameraPosition)
                if (durationMs == null || durationMs == 0L) {
                    ctrl.moveCamera(dstCameraPosition, listener)
                } else {
                    ctrl.animateCamera(dstCameraPosition, durationMs, listener)
                }
                return
            }
        }
        _cameraPosition.value = cameraPosition
        listener?.onComplete()
    }

    internal fun onCameraChange(cameraPosition: MapCameraPositionImpl) {
        _cameraPosition.value = cameraPosition
    }
}

class MapboxMapViewSaver : BaseMapViewSaver<MapboxViewStateImpl>() {
    override fun extractCameraPosition(state: MapboxViewStateImpl): MapCameraPositionImpl? = state.cameraPosition.value

    override fun saveMapDesign(
        state: MapboxViewStateImpl,
        bundle: Bundle,
    ) {
        bundle.putString("id", state.mapDesignType?.id ?: "null")
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
            initCameraPosition = cameraPosition,
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
                    initCameraPosition = MapCameraPositionImpl.from(cameraPosition),
                ),
            )
        }

    return state.value
}
