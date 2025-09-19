package com.mapconductor.mapbox

import MapboxMapViewController
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

interface MapboxViewState : MapViewState<MapboxDesignType>

class MapboxViewStateImpl(
    mapDesignType: MapboxDesignType,
    override val id: String,
    override val initCameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewStateImpl<MapboxDesignType>(),
    MapboxViewState {
    private var controller: MapboxMapViewController? = null

    // Camera center position
    private val _cameraPosition = MutableStateFlow(initCameraPosition)
    override val cameraPosition: StateFlow<MapCameraPosition> = _cameraPosition.asStateFlow()

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
        position: GeoPoint,
        durationMs: Long?,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        if (this.isInitialized.value != InitState.Initialized) {
            _cameraPosition.value =
                MapCameraPosition(
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
        cameraPosition: MapCameraPosition,
        durationMs: Long?,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        controller?.let { ctrl ->
            if (this.isInitialized.value == InitState.Initialized) {
                val dstCameraPosition = MapCameraPosition.from(cameraPosition)
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
        bundle.putString("id", state.mapDesignType?.id ?: "null")
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
