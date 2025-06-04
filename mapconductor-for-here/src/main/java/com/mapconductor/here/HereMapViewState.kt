package com.mapconductor.here

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapScheme
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.InitState
import com.mapconductor.core.map.MapCameraPositionBase
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
    override val stateId: String,
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
        position: IGeoPoint,
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
        position: IMapCameraPosition,
        durationMs: Long,
        listener: MoveCameraCallback?,
    ) {
        if (this.isInitialized.value != InitState.Initialized) {
            this.warningLog("moveCameraTo() called before map is initialized.")
            listener?.onComplete(false)
            return
        }

        val dstCameraPosition = MapCameraPosition.from(position)
        if (controller == null) {
            listener?.onComplete(false)
            return
        }

        if (durationMs == 0L) {
            controller!!.moveCamera(dstCameraPosition, listener)
        } else {
            controller!!.animateCamera(dstCameraPosition, durationMs.toLong(), listener)
        }
    }

    internal fun OnCameraChange(cameraState: MapCamera.State) {
        this.cameraPosition.value = cameraState
    }
}

val HereMapViewStateSaver =
    Saver<HereMapViewState, Bundle>(
        save = { state ->
            val cameraStateBundle =
                state.mapCameraPosition.value?.let { cameraState ->
                    Bundle().apply {
                        putDouble("zoom", cameraState.zoom)
                        putDouble("tilt", cameraState.tilt)
                        putDouble("bearing", cameraState.bearing)
                        putDouble("latitude", cameraState.position.latitude)
                        putDouble("longitude", cameraState.position.longitude)
                    }
                }

            val mapDesignBundle =
                Bundle().apply {
                    putInt("id", state.mapDesignType.getValue().value)
                }

            Bundle().apply {
                putString("stateId", state.stateId)
                putBundle("mapDesign", mapDesignBundle)
                putBundle("camera", cameraStateBundle)
            }
        },
        restore = { storedData ->
            val cameraBundle = storedData.getBundle("camera")
            val mapDesignBundle = storedData.getBundle("mapDesign")

            HereMapViewState(
                stateId = storedData.getString("stateId")!!,
                mapDesignType =
                    HereMapDesign.CreateById(
                        id = mapDesignBundle?.getInt("id") ?: HereMapDesign.NormalDay.id.value,
                    ),
                initCameraPosition =
                    MapCameraPosition(
                        position =
                            GeoPoint.fromLatLong(
                                latitude = cameraBundle?.getDouble("latitude") ?: 0.0,
                                longitude = cameraBundle?.getDouble("longitude") ?: 0.0,
                            ),
                        zoom = cameraBundle?.getDouble("zoom") ?: 0.0,
                        bearing = cameraBundle?.getDouble("bearing") ?: 0.0,
                        tilt = cameraBundle?.getDouble("tilt") ?: 0.0,
                        paddings = MapPaddingsImpl.Zeros,
                    ),
            )
        },
    )

@Composable
fun rememberHereMapViewState(
    mapDesign: HereMapDesign = HereMapDesign.NormalDay,
    cameraPosition: IMapCameraPosition = MapCameraPositionBase.Default,
): HereMapViewState {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = HereMapViewStateSaver,
        ) {
            mutableStateOf(
                HereMapViewState(
                    stateId = stateId,
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
