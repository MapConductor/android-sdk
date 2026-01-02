package com.mapconductor.here

import HereMapViewController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapPaddings
import com.mapconductor.core.map.MapPaddingsImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import java.util.UUID
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle

interface HereViewState : MapViewState<HereMapDesignType>

class HereViewStateImpl(
    override val id: String,
    mapDesignType: HereMapDesignType,
    cameraPosition: MapCameraPositionImpl = MapCameraPositionImpl.Default,
) : MapViewStateImpl<HereMapDesignType>(),
    HereViewState {
    private var _cameraPosition: MapCameraPositionImpl = cameraPosition
    override val cameraPosition: MapCameraPositionImpl
        get() = _cameraPosition
    private var controller: HereMapViewController? = null

    private var _mapDesignType: HereMapDesignType = mapDesignType

    override var mapDesignType: HereMapDesignType
        set(value) {
            _mapDesignType = value
            this.controller?.setMapDesignType(value)
        }
        get() = _mapDesignType

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
    override fun getMapViewHolder(): HereViewHolder? = controller?.holder as? HereViewHolder

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

    internal fun setController(controller: HereMapViewController) {
        this.controller = controller
//        controller.setMapDesignType(_mapDesignType)
        controller.moveCamera(this.cameraPosition)
    }

    internal fun onMapDesignTypeChange(value: HereMapDesignType) {
        _mapDesignType = value
    }

    internal fun updateCameraPosition(cameraPosition: MapCameraPositionImpl) {
        this._cameraPosition = cameraPosition
    }
}

class HereMapViewSaver : BaseMapViewSaver<HereViewStateImpl>() {
    override fun saveMapDesign(
        state: HereViewStateImpl,
        bundle: Bundle,
    ) {
        bundle.putInt("id", state.mapDesignType.getValue().value)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPositionImpl,
    ): HereViewStateImpl =
        HereViewStateImpl(
            id = stateId,
            mapDesignType =
                HereMapDesign.CreateById(
                    id = mapDesignBundle?.getInt("id") ?: HereMapDesign.NormalDay.id.value,
                ),
            cameraPosition = cameraPosition,
        )

    override fun getCameraPaddings(): MapPaddings? = MapPaddingsImpl.Zeros

    override fun getStateId(state: HereViewStateImpl): String = state.id
}

@Composable
fun rememberHereMapViewState(
    mapDesign: HereMapDesign = HereMapDesign.NormalDay,
    cameraPosition: MapCameraPosition = MapCameraPositionImpl.Default,
): HereViewStateImpl {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = HereMapViewSaver().createSaver(),
        ) {
            mutableStateOf(
                HereViewStateImpl(
                    id = stateId,
                    mapDesignType = mapDesign,
                    cameraPosition = MapCameraPositionImpl.from(cameraPosition),
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
