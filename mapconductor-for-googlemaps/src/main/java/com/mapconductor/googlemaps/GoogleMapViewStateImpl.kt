package com.mapconductor.googlemaps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapPaddingsImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import java.util.UUID
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface GoogleMapViewState : MapViewState<GoogleMapDesignType>

class GoogleMapViewStateImpl(
    override val id: String,
    mapDesignType: GoogleMapDesignType,
    override val initCameraPosition: MapCameraPositionImpl,
) : MapViewStateImpl<GoogleMapDesignType>(),
    GoogleMapViewState {
    // Map padding
    private val _padding = MutableStateFlow(MapPaddingsImpl.Zeros)
    val padding: StateFlow<MapPaddingsImpl> = _padding.asStateFlow()

    // Camera position
    private val _cameraPosition = MutableStateFlow<MapCameraPositionImpl>(initCameraPosition)
    override val cameraPosition: StateFlow<MapCameraPositionImpl> = _cameraPosition.asStateFlow()

    private var _mapDesignType: GoogleMapDesignType = mapDesignType

    override var mapDesignType: GoogleMapDesignType
        set(value) {
            _mapDesignType = value
            this.controller?.setMapDesignType(value)
        }
        get() = _mapDesignType
    private var controller: GoogleMapViewController? = null

    internal fun setController(controller: GoogleMapViewController) {
        this.controller = controller
        _mapDesignType.let {
            controller.setMapDesignType(it)
        }
        controller.moveCamera(_cameraPosition.value)
    }

    internal fun onMapDesignTypeChange(value: GoogleMapDesignType) {
        _mapDesignType = value
    }

    override fun moveCameraTo(
        position: GeoPointImpl,
        durationMs: Long?,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val currentPosition = this.cameraPosition.value
        val newPosition =
            currentPosition.copy(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMs, listener)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getMapViewHolder(): GoogleMapViewHolder? = controller?.holder as? GoogleMapViewHolder

    override fun moveCameraTo(
        cameraPosition: MapCameraPositionImpl,
        durationMs: Long?,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        controller?.let { ctrl ->
            val dstCameraPosition = MapCameraPositionImpl.from(cameraPosition)
            if (durationMs == null || durationMs == 0L) {
                ctrl.moveCamera(dstCameraPosition, listener)
            } else {
                ctrl.animateCamera(dstCameraPosition, durationMs, listener)
            }
            return@let
        }
        _cameraPosition.value = cameraPosition
        listener?.onComplete()
    }

    internal fun onCameraChange(cameraPosition: MapCameraPositionImpl) {
        this._cameraPosition.value = cameraPosition
    }
}

// GoogleMapViewSaver implementation
class GoogleMapViewSaver : BaseMapViewSaver<GoogleMapViewStateImpl>() {
    override fun extractCameraPosition(state: GoogleMapViewStateImpl): MapCameraPositionImpl? =
        state.cameraPosition.value

    override fun saveMapDesign(
        state: GoogleMapViewStateImpl,
        bundle: Bundle,
    ) {
        bundle.putInt("id", state.mapDesignType.id)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPositionImpl,
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
    mapDesign: GoogleMapDesign = GoogleMapDesign.Normal,
    cameraPosition: MapCameraPosition = MapCameraPositionImpl.Default,
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
                    initCameraPosition = MapCameraPositionImpl.from(cameraPosition),
                ),
            )
        }

    return state.value
}
