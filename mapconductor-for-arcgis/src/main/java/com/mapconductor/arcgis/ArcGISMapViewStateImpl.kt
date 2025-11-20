package com.mapconductor.arcgis

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ArcGISMapViewState : MapViewState<ArcGISDesignType>

class ArcGISMapViewStateImpl(
    override val id: String,
    mapDesignType: ArcGISDesignType,
    cameraPosition: MapCameraPositionImpl = MapCameraPositionImpl.Default,
) : MapViewStateImpl<ArcGISDesignType>(),
    ArcGISMapViewState {
    private var _cameraPosition: MapCameraPositionImpl = cameraPosition
    override val cameraPosition: MapCameraPositionImpl
        get() = _cameraPosition

    // Map padding
    private val _padding = MutableStateFlow(MapPaddingsImpl.Zeros)
    val padding: StateFlow<MapPaddings> = _padding.asStateFlow()

    private var controller: ArcGISMapViewController? = null
    private var _mapDesignType: ArcGISDesignType = mapDesignType

    override var mapDesignType: ArcGISDesignType
        set(value) {
            _mapDesignType = value
            this.controller?.setMapDesignType(value)
        }
        get() = _mapDesignType

    internal fun setController(controller: ArcGISMapViewController) {
        this.controller = controller
        controller.setMapDesignType(_mapDesignType)
        controller.moveCamera(cameraPosition)
    }

    internal fun onMapDesignTypeChange(value: ArcGISDesignType) {
        _mapDesignType = value
    }

    override fun moveCameraTo(
        cameraPosition: MapCameraPositionImpl,
        durationMs: Long?,
    ) {
        controller?.let { ctrl ->
            val dstCameraPosition = MapCameraPositionImpl.from(cameraPosition)
            if (durationMs == null || durationMs == 0L) {
                ctrl.moveCamera(dstCameraPosition)
            } else {
                ctrl.animateCamera(dstCameraPosition, durationMs)
            }
            return@let
        }
        this._cameraPosition = cameraPosition
    }

    override fun moveCameraTo(
        position: GeoPointImpl,
        durationMs: Long?,
    ) {
        val currentPosition = this.cameraPosition
        val newPosition =
            currentPosition.copy(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMs)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getMapViewHolder(): ArcGISMapViewHolder? = controller?.holder as? ArcGISMapViewHolder

    internal fun updateCameraPosition(cameraPosition: MapCameraPositionImpl) {
        this._cameraPosition = cameraPosition
    }
}

class ArcGISMapViewSaver : BaseMapViewSaver<ArcGISMapViewStateImpl>() {
    override fun saveMapDesign(
        state: ArcGISMapViewStateImpl,
        bundle: Bundle,
    ) {
        bundle.putString("id", state.mapDesignType.id)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPositionImpl,
    ): ArcGISMapViewStateImpl =
        ArcGISMapViewStateImpl(
            id = stateId,
            mapDesignType =
                ArcGISDesign.Create(
                    id = mapDesignBundle?.getString("id") ?: ArcGISDesign.Streets.id,
                ),
            cameraPosition = cameraPosition,
        )

    override fun getStateId(state: ArcGISMapViewStateImpl): String = state.id
}

@Composable
fun rememberArcGISMapViewState(
    mapDesign: ArcGISDesign = ArcGISDesign.Streets,
    cameraPosition: MapCameraPosition = MapCameraPositionImpl.Default,
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
