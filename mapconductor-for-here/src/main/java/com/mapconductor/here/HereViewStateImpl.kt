package com.mapconductor.here

import HereMapViewController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.InitState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapPaddings
import com.mapconductor.core.map.MapPaddingsImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewState.MoveCameraCallback
import com.mapconductor.core.map.MapViewStateImpl
import java.util.UUID
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface HereViewState : MapViewState<HereMapDesignType>

class HereViewStateImpl(
    override val id: String,
    mapDesignType: HereMapDesignType,
    override val initCameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewStateImpl<HereMapDesignType>(),
    HereViewState {
    private var controller: HereMapViewController? = null

    // Camera center position
    private val _cameraPosition = MutableStateFlow<MapCameraPosition>(initCameraPosition)
    override val cameraPosition: StateFlow<MapCameraPosition> = _cameraPosition.asStateFlow()
    private var _mapDesignType: HereMapDesignType = mapDesignType

    override var mapDesignType: HereMapDesignType
        set(value) {
            value?.let {
                _mapDesignType = value
                this.controller?.setMapDesignType(value)
            }
        }
        get() = _mapDesignType

    override fun moveCameraTo(
        position: GeoPoint,
        durationMs: Long,
        listener: MoveCameraCallback?,
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
    override fun getMapViewHolder(): HereViewHolder? = controller?.holder as? HereViewHolder

    override fun moveCameraTo(
        cameraPosition: MapCameraPosition,
        durationMs: Long,
        listener: MoveCameraCallback?,
    ) {
        controller?.let { ctrl ->
            if (this.isInitialized.value == InitState.Initialized) {
                val dstCameraPosition = MapCameraPosition.from(cameraPosition)
                if (durationMs == 0L) {
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

    internal fun onCameraChange(cameraState: MapCameraPosition) {
        this._cameraPosition.value = cameraState
    }

    internal fun setController(controller: HereMapViewController) {
        this.controller = controller
        _mapDesignType?.let {
            controller.setMapDesignType(it)
        }
        controller.moveCamera(_cameraPosition.value)
    }

    internal fun onMapDesignTypeChange(value: HereMapDesignType) {
        _mapDesignType = value
    }
}

class HereMapViewSaver : BaseMapViewSaver<HereViewStateImpl>() {
    override fun extractCameraPosition(state: HereViewStateImpl): MapCameraPosition? = state.cameraPosition.value

    override fun saveMapDesign(
        state: HereViewStateImpl,
        bundle: Bundle,
    ) {
        bundle.putInt("id", state.mapDesignType.getValue().value)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition,
    ): HereViewStateImpl =
        HereViewStateImpl(
            id = stateId,
            mapDesignType =
                HereMapDesign.CreateById(
                    id = mapDesignBundle?.getInt("id") ?: HereMapDesign.NormalDay.id.value,
                ),
            initCameraPosition = cameraPosition,
        )

    override fun getCameraPaddings(): MapPaddings? = MapPaddingsImpl.Zeros

    override fun getStateId(state: HereViewStateImpl): String = state.id
}

@Composable
fun rememberHereMapViewState(
    mapDesign: HereMapDesign = HereMapDesign.NormalDay,
    cameraPosition: IMapCameraPosition = MapCameraPosition.Default,
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
                    initCameraPosition = MapCameraPosition.from(cameraPosition),
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
