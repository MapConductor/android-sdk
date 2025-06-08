package com.mapconductor.here

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.here.sdk.mapview.MapCamera
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle

interface IHereMapViewState : MapViewState<MapScheme>

class HereMapViewState(
    override val id: String,
    override val mapDesignType: HereMapDesignType,
    override val initCameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewStateImpl<MapScheme>(),
    IHereMapViewState {
    internal var controller: IHereMapViewController? = null

    // Camera center position
    private val cameraPosition = MutableStateFlow<MapCamera.State?>(null)
    override val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraPosition.map { it?.toMapCameraPosition() }.stateIn(
            scope = mainCoroutine,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

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
        val currCameraPosition = this.mapCameraPosition.value
        if (currCameraPosition == null) {
            listener?.onComplete(false)
            return
        }
        val newPosition =
            currCameraPosition.copy(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMs, listener)
    }

    override fun moveCameraTo(
        position: MapCameraPosition,
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
            controller!!.moveCamera(position, listener)
        } else {
            controller!!.animateCamera(position, durationMs.toLong(), listener)
        }
    }

    internal fun OnCameraChange(cameraState: MapCamera.State) {
        this.cameraPosition.value = cameraState
    }
}
class HereMapViewSaver : BaseMapViewSaver<HereMapViewState>() {

    override fun extractCameraPosition(state: HereMapViewState): MapCameraPosition? {
        return state.mapCameraPosition.value
    }

    override fun saveMapDesign(state: HereMapViewState, bundle: Bundle) {
        bundle.putInt("id", state.mapDesignType.getValue().value)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition
    ): HereMapViewState {
        return HereMapViewState(
            id = stateId,
            mapDesignType = HereMapDesign.CreateById(
                id = mapDesignBundle?.getInt("id") ?: HereMapDesign.NormalDay.id.value,
            ),
            initCameraPosition = cameraPosition,
        )
    }

    override fun getCameraPaddings(): MapPaddings? = MapPaddingsImpl.Zeros

    override fun getStateId(state: HereMapViewState): String = state.id
}
@Composable
fun rememberHereMapViewState(
    mapDesign: HereMapDesign = HereMapDesign.NormalDay,
    cameraPosition: IMapCameraPosition = MapCameraPosition.Default,
): HereMapViewState {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = HereMapViewSaver().createSaver(),
        ) {
            mutableStateOf(
                HereMapViewState(
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
