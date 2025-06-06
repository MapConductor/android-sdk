package com.mapconductor.mapbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapbox.maps.CameraState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.InitState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import com.mapconductor.mapbox.MapboxMapDesign.Standard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import android.os.Bundle

interface IMapboxMapViewState : MapViewState<String>

class MapboxMapViewState(
    override val id: String,
    override val mapDesignType: MapboxDesignType,
    override val initCameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewStateImpl<String>(),
    IMapboxMapViewState {
    internal var controller: IMapboxMapViewController? = null

    // Camera center position
    private val cameraState = MutableStateFlow<CameraState>(initCameraPosition.toCameraState())
    override val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraState.map { it.toMapCameraPosition() }.stateIn(
            scope = this.mainCoroutine,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    override fun moveCameraTo(
        position: GeoPoint,
        durationMs: Long,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        if (this.isInitialized.value != InitState.Initialized) {
            this.warningLog("moveCameraTo() called before map is initialized.")
            listener?.onComplete(false)
            return
        }
        val currentPosition = this.mapCameraPosition.value
        if (currentPosition == null) {
            listener?.onComplete(false)
            return
        }
        val newPosition =
            currentPosition.copy(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMs, listener)
    }

    override fun moveCameraTo(
        position: MapCameraPosition,
        durationMs: Long,
        listener: MapViewState.MoveCameraCallback?,
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

    internal fun OnCameraChange(state: CameraState) {
        cameraState.value = state
    }
}

val MapboxMapViewStateSaver =
    Saver<MapboxMapViewState, Bundle>(
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
                    putString("id", state.mapDesignType.id)
                }

            Bundle().apply {
                putString("stateId", state.id)
                putBundle("mapDesign", mapDesignBundle)
                putBundle("camera", cameraStateBundle)
            }
        },
        restore = { storedData ->
            val cameraBundle = storedData.getBundle("camera")
            val mapDesignBundle = storedData.getBundle("mapDesign")

            MapboxMapViewState(
                id = storedData.getString("stateId")!!,
                mapDesignType =
                    MapboxMapDesign.Create(
                        layerId = mapDesignBundle?.getString("id") ?: Standard.id,
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
                        paddings = null,
                    ),
            )
        },
    )

@Composable
fun rememberMapboxMapViewState(
    mapDesign: MapboxDesignType = Standard,
    cameraPosition: IMapCameraPosition = MapCameraPosition.Default,
): MapboxMapViewState {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(stateSaver = MapboxMapViewStateSaver) {
            mutableStateOf(
                MapboxMapViewState(
                    id = stateId,
                    mapDesignType = mapDesign,
                    initCameraPosition = MapCameraPosition.from(cameraPosition),
                ),
            )
        }

    return state.value
}
