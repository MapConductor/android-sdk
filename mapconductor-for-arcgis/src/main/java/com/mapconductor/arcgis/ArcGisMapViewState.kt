package com.mapconductor.arcgis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.arcgismaps.mapping.view.Camera
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.InitState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapPaddings
import com.mapconductor.core.map.MapPaddingsImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle

interface IArcGISMapViewState : MapViewState<String>

class ArcGISMapViewState(
    override val id: String,
    override val initCameraPosition: MapCameraPosition,
    override val mapDesignType: ArcGISDesign,
) : MapViewStateImpl<String>(),
    IArcGISMapViewState {
    // Map padding
    private val _padding = MutableStateFlow(MapPaddingsImpl.Zeros)
    val padding: StateFlow<MapPaddings> = _padding.asStateFlow()

    internal var controller: IArcGISMapViewController? = null

    // Camera position
    private val cameraPosition = MutableStateFlow<Camera?>(null)
    override val mapCameraPosition: StateFlow<MapCameraPosition?> =
        cameraPosition.map { it?.toMapCameraPosition() }.stateIn(
            scope = mainCoroutine,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

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
        controller?.let {
            if (durationMs == 0L) {
                it.moveCamera(dstCameraPosition, listener)
            } else {
                it.animateCamera(dstCameraPosition, durationMs.toInt(), listener)
            }
        } ?: listener?.onComplete(false)
    }

    override fun moveCameraTo(
        position: GeoPoint,
        durationMs: Long,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        // Do nothing here
    }

    internal fun OnCameraChange(cameraPosition: Camera) {
        this.cameraPosition.value = cameraPosition
    }
}

val ArcGISMapViewStateSaver =
    Saver<ArcGISMapViewState, Bundle>(
        save = { state ->
            val cameraStateBundle =
                state.mapCameraPosition.value.let { cameraState ->
                    Bundle().apply {
                        putDouble("zoom", cameraState?.zoom ?: MapCameraPosition.Default.zoom)
                        putDouble("tilt", cameraState?.tilt ?: MapCameraPosition.Default.tilt)
                        putDouble("bearing", cameraState?.bearing ?: MapCameraPosition.Default.bearing)
                        putDouble(
                            "latitude",
                            cameraState?.position?.latitude
                                ?: MapCameraPosition.Default.position.latitude,
                        )
                        putDouble(
                            "longitude",
                            cameraState?.position?.longitude
                                ?: MapCameraPosition.Default.position.longitude,
                        )
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

            ArcGISMapViewState(
                id = storedData.getString("stateId")!!,
                mapDesignType =
                    ArcGISDesign.Create(
                        id = mapDesignBundle?.getString("id") ?: ArcGISDesign.Streets.id,
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
fun rememberArcGISMapViewState(
    mapDesign: ArcGISDesign = ArcGISDesign.Streets,
    cameraPosition: IMapCameraPosition = MapCameraPosition.Default,
): ArcGISMapViewState {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = ArcGISMapViewStateSaver,
        ) {
            mutableStateOf(
                ArcGISMapViewState(
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
