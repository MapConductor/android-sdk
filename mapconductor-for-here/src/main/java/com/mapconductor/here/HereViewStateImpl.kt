package com.mapconductor.here

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.here.sdk.mapview.MapScheme
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

interface HereViewState : MapViewState<MapScheme>

class HereViewStateImpl(
    override val id: String,
    override val mapDesignType: HereMapDesignType,
    override val initCameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewStateImpl<MapScheme>(),
    HereViewState {
    internal var controller: IHereMapViewController? = null

    // Camera center position
    private val _cameraPosition = MutableStateFlow<MapCameraPosition>(initCameraPosition)
    override val cameraPosition: StateFlow<MapCameraPosition> = _cameraPosition.asStateFlow()

    override fun moveCameraTo(
        position: GeoPoint,
        durationMs: Long,
        listener: MoveCameraCallback?,
    ) {
        if (this.isInitialized.value != InitState.Initialized) {
            this.warningLog("moveCameraTo() called before map is initialized.")
            listener?.onComplete(false)
            return
        }
        val currCameraPosition = this.cameraPosition.value
        val newPosition =
            currCameraPosition.copy(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMs, listener)
    }

    override fun moveCameraTo(
        cameraPosition: MapCameraPosition,
        durationMs: Long,
        listener: MoveCameraCallback?,
    ) {
        if (this.isInitialized.value != InitState.Initialized) {
            this.warningLog("moveCameraTo() called before map is initialized.")
            listener?.onComplete(false)
            return
        }

        if (controller == null) {
            listener?.onComplete(false)
            return
        }

        if (durationMs == 0L) {
            controller!!.moveCamera(cameraPosition, listener)
        } else {
            controller!!.animateCamera(cameraPosition, durationMs.toLong(), listener)
        }
    }

    internal fun onCameraChange(cameraState: MapCameraPosition) {
        this._cameraPosition.value = cameraState
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
