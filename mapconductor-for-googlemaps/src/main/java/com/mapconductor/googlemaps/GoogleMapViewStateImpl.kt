package com.mapconductor.googlemaps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapPaddings
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateInterface
import java.util.UUID
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface GoogleMapViewStateInterface : MapViewStateInterface<GoogleMapDesignType>

class GoogleMapViewState(
    override val id: String,
    mapDesignType: GoogleMapDesignType,
    cameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewState<GoogleMapDesignType>(),
    GoogleMapViewStateInterface {
    private var _cameraPosition: MapCameraPosition = cameraPosition
    override val cameraPosition: MapCameraPosition
        get() = _cameraPosition

    // Map padding
    private val _padding = MutableStateFlow(MapPaddings.Zeros)
    val padding: StateFlow<MapPaddings> = _padding.asStateFlow()

    private var _mapDesignType: GoogleMapDesignType = mapDesignType

    override var mapDesignType: GoogleMapDesignType
        set(value) {
            _mapDesignType = value
            this.controller?.setMapDesignType(value)
        }
        get() = _mapDesignType
    private var controller: GoogleMapViewControllerInterface? = null

    internal fun setController(controller: GoogleMapViewControllerInterface) {
        this.controller = controller
//        _mapDesignType.let {
//            controller.setMapDesignType(it)
//        }
        controller.moveCamera(cameraPosition)
    }

    internal fun onMapDesignTypeChange(value: GoogleMapDesignType) {
        _mapDesignType = value
    }

    override fun moveCameraTo(
        position: GeoPoint,
        durationMillis: Long?,
    ) {
        val newPosition =
            this.cameraPosition.copy(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMillis)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getMapViewHolder(): GoogleMapViewHolder? = controller?.holder as? GoogleMapViewHolder

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

// GoogleMapViewSaver implementation
class GoogleMapViewSaver : BaseMapViewSaver<GoogleMapViewState>() {
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
            cameraPosition = cameraPosition,
        )

    override fun getStateId(state: GoogleMapViewState): String = state.id
}

@Composable
fun rememberGoogleMapViewState(
    mapDesign: GoogleMapDesign = GoogleMapDesign.Normal,
    cameraPosition: MapCameraPositionInterface = MapCameraPosition.Default,
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
                    cameraPosition = MapCameraPosition.from(cameraPosition),
                ),
            )
        }

    return state.value
}
