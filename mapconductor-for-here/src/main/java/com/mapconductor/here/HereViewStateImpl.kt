package com.mapconductor.here

import HereMapViewControllerInterface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapPaddings
import com.mapconductor.core.map.MapPaddingsInterface
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateInterface
import java.util.UUID
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle

interface HereViewStateInterface : MapViewStateInterface<HereMapDesignType>

class HereViewState(
    override val id: String,
    mapDesignType: HereMapDesignType,
    cameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewState<HereMapDesignType>(),
    HereViewStateInterface {
    private var _cameraPosition: MapCameraPosition = cameraPosition
    override val cameraPosition: MapCameraPosition
        get() = _cameraPosition
    private var controller: HereMapViewControllerInterface? = null

    private var _mapDesignType: HereMapDesignType = mapDesignType

    override var mapDesignType: HereMapDesignType
        set(value) {
            _mapDesignType = value
            this.controller?.setMapDesignType(value)
        }
        get() = _mapDesignType

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
    override fun getMapViewHolder(): HereViewHolder? = controller?.holder as? HereViewHolder

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

    internal fun setController(controller: HereMapViewControllerInterface) {
        this.controller = controller
//        controller.setMapDesignType(_mapDesignType)
        controller.moveCamera(this.cameraPosition)
    }

    internal fun onMapDesignTypeChange(value: HereMapDesignType) {
        _mapDesignType = value
    }

    internal fun updateCameraPosition(cameraPosition: MapCameraPosition) {
        this._cameraPosition = cameraPosition
    }
}

class HereMapViewSaver : BaseMapViewSaver<HereViewState>() {
    override fun saveMapDesign(
        state: HereViewState,
        bundle: Bundle,
    ) {
        bundle.putInt("id", state.mapDesignType.getValue().value)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition,
    ): HereViewState =
        HereViewState(
            id = stateId,
            mapDesignType =
                HereMapDesign.CreateById(
                    id = mapDesignBundle?.getInt("id") ?: HereMapDesign.NormalDay.id.value,
                ),
            cameraPosition = cameraPosition,
        )

    override fun getCameraPaddings(): MapPaddingsInterface? = MapPaddings.Zeros

    override fun getStateId(state: HereViewState): String = state.id
}

@Composable
fun rememberHereMapViewState(
    mapDesign: HereMapDesign = HereMapDesign.NormalDay,
    cameraPosition: MapCameraPositionInterface = MapCameraPosition.Default,
): HereViewState {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = HereMapViewSaver().createSaver(),
        ) {
            mutableStateOf(
                HereViewState(
                    id = stateId,
                    mapDesignType = mapDesign,
                    cameraPosition = MapCameraPosition.from(cameraPosition),
                ),
            )
        }

    return state.value
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
