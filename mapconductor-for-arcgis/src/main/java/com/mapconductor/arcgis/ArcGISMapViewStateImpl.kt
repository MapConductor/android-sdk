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
    override val initCameraPosition: MapCameraPositionImpl,
) : MapViewStateImpl<ArcGISDesignType>(),
    ArcGISMapViewState {
    // Map padding
    private val _padding = MutableStateFlow(MapPaddingsImpl.Zeros)
    val padding: StateFlow<MapPaddings> = _padding.asStateFlow()

    // Camera position
    private val _cameraPosition = MutableStateFlow<MapCameraPositionImpl>(initCameraPosition)
    override val cameraPosition: StateFlow<MapCameraPositionImpl> = _cameraPosition.asStateFlow()

    private var controller: ArcGISMapViewController? = null
    private var _mapDesignType: ArcGISDesignType = mapDesignType

    override var mapDesignType: ArcGISDesignType
        set(value) {
            value?.let {
                _mapDesignType = value
                this.controller?.setMapDesignType(value)
            }
        }
        get() = _mapDesignType

    internal fun setController(controller: ArcGISMapViewController) {
        this.controller = controller
        _mapDesignType?.let {
            controller.setMapDesignType(it)
        }
        controller.moveCamera(_cameraPosition.value)
    }

    internal fun onMapDesignTypeChange(value: ArcGISDesignType) {
        _mapDesignType = value
    }

    override fun moveCameraTo(
        cameraPosition: MapCameraPositionImpl,
        durationMs: Long?,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        _cameraPosition.value = cameraPosition
        listener?.onComplete()
    }

    override fun moveCameraTo(
        position: GeoPointImpl,
        durationMs: Long?,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val currentPosition = this.cameraPosition.value
        val newPosition =
            currentPosition.copy(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMs, listener)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getMapViewHolder(): ArcGISMapViewHolder? = controller?.holder as? ArcGISMapViewHolder

    internal fun onCameraChange(cameraPosition: MapCameraPositionImpl) {
        this._cameraPosition.value = cameraPosition
    }
}

class ArcGISMapViewSaver : BaseMapViewSaver<ArcGISMapViewStateImpl>() {
    override fun extractCameraPosition(state: ArcGISMapViewStateImpl): MapCameraPositionImpl? =
        state.cameraPosition.value

    override fun saveMapDesign(
        state: ArcGISMapViewStateImpl,
        bundle: Bundle,
    ) {
        bundle.putString("id", state.mapDesignType?.id ?: ArcGISDesign.Streets.id)
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
            initCameraPosition = cameraPosition,
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
                    initCameraPosition = MapCameraPositionImpl.from(cameraPosition),
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
