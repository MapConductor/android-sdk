package com.mapconductor.maplibre

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateImpl
import java.util.UUID
import android.os.Bundle

interface MapLibreViewState : MapViewState<MapLibreMapDesignType>

class MapLibreViewStateImpl(
    mapDesignType: MapLibreMapDesignType,
    override val id: String,
    cameraPosition: MapCameraPositionImpl = MapCameraPositionImpl.Default,
) : MapViewStateImpl<MapLibreMapDesignType>(),
    MapLibreViewState {
    private var controller: MapLibreViewController? = null
    private var _mapDesignType: MapLibreMapDesignType = mapDesignType

    private var _cameraPosition: MapCameraPositionImpl = cameraPosition
    override val cameraPosition: MapCameraPositionImpl
        get() = _cameraPosition
    override var mapDesignType: MapLibreMapDesignType
        set(value) {
            value?.let {
                _mapDesignType = it
                this.controller?.setMapDesignType(it)
            }
        }
        get() = _mapDesignType

    internal fun setController(controller: MapLibreViewController) {
        this.controller = controller
        controller.moveCamera(this.cameraPosition)
    }

    internal fun onMapDesignTypeChange(value: MapLibreMapDesignType) {
        _mapDesignType = value
    }

    override fun moveCameraTo(
        position: GeoPointImpl,
        durationMills: Long?,
    ) {
        val newPosition =
            this.cameraPosition?.let { currentPosition ->
                MapCameraPositionImpl.Companion.from(currentPosition).copy(
                    position = position,
                )
            } ?: MapCameraPositionImpl(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMills)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getMapViewHolder(): MapLibreMapViewHolder? = controller?.holder as? MapLibreMapViewHolder

    override fun moveCameraTo(
        cameraPosition: MapCameraPositionImpl,
        durationMills: Long?,
    ) {
        controller?.let { ctrl ->
            val dstCameraPosition = MapCameraPositionImpl.from(cameraPosition)
            if (durationMills == null || durationMills == 0L) {
                ctrl.moveCamera(dstCameraPosition)
            } else {
                ctrl.animateCamera(dstCameraPosition, durationMills)
            }
            return@let
        }
        this._cameraPosition = cameraPosition
    }

    internal fun updateCameraPosition(cameraPosition: MapCameraPositionImpl) {
        this._cameraPosition = cameraPosition
    }
}

class MapLibreMapViewSaver : BaseMapViewSaver<MapLibreViewStateImpl>() {
    override fun saveMapDesign(
        state: MapLibreViewStateImpl,
        bundle: Bundle,
    ) {
        bundle.putString("styleJsonURL", state.mapDesignType.styleJsonURL)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPositionImpl,
    ): MapLibreViewStateImpl =
        MapLibreViewStateImpl(
            id = stateId,
            mapDesignType =
                MapLibreDesignType(
                    id =
                        mapDesignBundle?.getString("id")
                            ?: MapLibreDesignType.DemoTiles.id,
                    styleJsonURL =
                        mapDesignBundle?.getString("styleJsonURL")
                            ?: MapLibreDesignType.DemoTiles.styleJsonURL,
                ),
            cameraPosition = cameraPosition,
        )

    override fun getStateId(state: MapLibreViewStateImpl): String = state.id
}

@Composable
fun rememberMapLibreMapViewState(
    mapDesign: MapLibreMapDesignType = MapLibreDesignType.DemoTiles,
    cameraPosition: MapCameraPosition = MapCameraPositionImpl.Default,
): MapLibreViewStateImpl {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = MapLibreMapViewSaver().createSaver(),
        ) {
            mutableStateOf(
                MapLibreViewStateImpl(
                    id = stateId,
                    mapDesignType = mapDesign,
                    cameraPosition = MapCameraPositionImpl.Companion.from(cameraPosition),
                ),
            )
        }

    return state.value
}
