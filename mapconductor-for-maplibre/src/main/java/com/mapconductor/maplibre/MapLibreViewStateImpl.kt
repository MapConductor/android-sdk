package com.mapconductor.maplibre

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.BaseMapViewSaver
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateInterface
import java.util.UUID
import android.os.Bundle

interface MapLibreViewStateInterface : MapViewStateInterface<MapLibreMapDesignTypeInterface>

class MapLibreViewState(
    mapDesignType: MapLibreMapDesignTypeInterface,
    override val id: String,
    cameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewState<MapLibreMapDesignTypeInterface>(),
    MapLibreViewStateInterface {
    private var controller: MapLibreViewControllerInterface? = null
    private var _mapDesignType: MapLibreMapDesignTypeInterface = mapDesignType

    private var _cameraPosition: MapCameraPosition = cameraPosition
    override val cameraPosition: MapCameraPosition
        get() = _cameraPosition
    override var mapDesignType: MapLibreMapDesignTypeInterface
        set(value) {
            value?.let {
                _mapDesignType = it
                this.controller?.setMapDesignType(it)
            }
        }
        get() = _mapDesignType

    internal fun setController(controller: MapLibreViewControllerInterface) {
        this.controller = controller
        controller.moveCamera(this.cameraPosition)
    }

    internal fun onMapDesignTypeChange(value: MapLibreMapDesignTypeInterface) {
        _mapDesignType = value
    }

    override fun moveCameraTo(
        position: GeoPoint,
        durationMillis: Long?,
    ) {
        val newPosition =
            this.cameraPosition?.let { currentPosition ->
                MapCameraPosition.Companion.from(currentPosition).copy(
                    position = position,
                )
            } ?: MapCameraPosition(
                position = position,
            )
        this.moveCameraTo(newPosition, durationMillis)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getMapViewHolder(): MapLibreMapViewHolderInterface? =
        controller?.holder as? MapLibreMapViewHolderInterface

    override fun moveCameraTo(
        cameraPosition: MapCameraPosition,
        durationMillis: Long?,
    ) {
        controller?.let { ctrl ->
            val dstCameraPosition = MapCameraPosition.from(cameraPosition)
            if (durationMillis == null || durationMillis == 0L) {
                ctrl.moveCamera(dstCameraPosition)
            } else {
                ctrl.animateCamera(dstCameraPosition, durationMillis)
            }
            return@let
        }
        this._cameraPosition = cameraPosition
    }

    internal fun updateCameraPosition(cameraPosition: MapCameraPosition) {
        this._cameraPosition = cameraPosition
    }
}

class MapLibreMapViewSaver : BaseMapViewSaver<MapLibreViewState>() {
    override fun saveMapDesign(
        state: MapLibreViewState,
        bundle: Bundle,
    ) {
        bundle.putString("styleJsonURL", state.mapDesignType.styleJsonURL)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition,
    ): MapLibreViewState =
        MapLibreViewState(
            id = stateId,
            mapDesignType =
                MapLibreDesign(
                    id =
                        mapDesignBundle?.getString("id")
                            ?: MapLibreDesign.OsmBright.id,
                    styleJsonURL =
                        mapDesignBundle?.getString("styleJsonURL")
                            ?: MapLibreDesign.OsmBright.styleJsonURL,
                ),
            cameraPosition = cameraPosition,
        )

    override fun getStateId(state: MapLibreViewState): String = state.id
}

@Composable
fun rememberMapLibreMapViewState(
    mapDesign: MapLibreMapDesignTypeInterface = MapLibreDesign.DemoTiles,
    cameraPosition: MapCameraPositionInterface = MapCameraPosition.Default,
): MapLibreViewState {
    val stateId by rememberSaveable {
        val uuid = UUID.randomUUID().toString()
        mutableStateOf(uuid)
    }
    val state =
        rememberSaveable(
            stateSaver = MapLibreMapViewSaver().createSaver(),
        ) {
            mutableStateOf(
                MapLibreViewState(
                    id = stateId,
                    mapDesignType = mapDesign,
                    cameraPosition = MapCameraPosition.Companion.from(cameraPosition),
                ),
            )
        }

    return state.value
}
