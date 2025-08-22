package com.mapconductor.arcgis

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
import com.mapconductor.core.map.MapViewStateImpl
import java.util.UUID
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ArcGISMapViewState : MapViewState<String>

class ArcGISMapViewStateImpl(
    override val id: String,
    override val initCameraPosition: MapCameraPosition,
    override val mapDesignType: ArcGISDesign,
) : MapViewStateImpl<String>(),
    ArcGISMapViewState {
    // Map padding
    private val _padding = MutableStateFlow(MapPaddingsImpl.Zeros)
    val padding: StateFlow<MapPaddings> = _padding.asStateFlow()

    internal var controller: ArcGISMapViewController? = null

    // Camera position
    private val _cameraPosition = MutableStateFlow<MapCameraPosition>(initCameraPosition)
    override val cameraPosition: StateFlow<MapCameraPosition> = _cameraPosition.asStateFlow()

    override fun moveCameraTo(
        cameraPosition: MapCameraPosition,
        durationMs: Long,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        if (this.isInitialized.value != InitState.Initialized) {
            this.warningLog("moveCameraTo() called before map is initialized.")
            listener?.onComplete(false)
            return
        }

        val dstCameraPosition = MapCameraPosition.from(cameraPosition)
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

    internal fun onCameraChange(cameraPosition: MapCameraPosition) {
        this._cameraPosition.value = cameraPosition
    }
}

class ArcGISMapViewSaver : BaseMapViewSaver<ArcGISMapViewStateImpl>() {
    override fun extractCameraPosition(state: ArcGISMapViewStateImpl): MapCameraPosition? = state.cameraPosition.value

    override fun saveMapDesign(
        state: ArcGISMapViewStateImpl,
        bundle: Bundle,
    ) {
        bundle.putString("id", state.mapDesignType.id)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition,
    ): ArcGISMapViewStateImpl =
        ArcGISMapViewStateImpl(
            id = stateId,
            mapDesignType =
                ArcGISDesign.Create(
                    id = mapDesignBundle?.getString("id") ?: ArcGISDesign.Streets.id,
                ),
            initCameraPosition = cameraPosition,
        )

    override fun getStateId(state: ArcGISMapViewStateImpl): String = state.id
}

@Composable
fun rememberArcGISMapViewState(
    mapDesign: ArcGISDesign = ArcGISDesign.Streets,
    cameraPosition: IMapCameraPosition = MapCameraPosition.Default,
): ArcGISMapViewStateImpl {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = ArcGISMapViewSaver().createSaver(),
        ) {
            mutableStateOf(
                ArcGISMapViewStateImpl(
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
