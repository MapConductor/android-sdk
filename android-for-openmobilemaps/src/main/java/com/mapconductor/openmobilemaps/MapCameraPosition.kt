package com.mapconductor.openmobilemaps

import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface

/**
 * [MapCameraPositionInterface] を具象の [MapCameraPosition] にする。
 *
 * コアには置いていない（全プロバイダがローカル拡張として持っている）。
 * android-for-maplibre の同名拡張と同じ実装。
 */
fun MapCameraPosition.Companion.from(cameraPosition: MapCameraPositionInterface) =
    when (cameraPosition) {
        is MapCameraPosition -> cameraPosition
        else ->
            MapCameraPosition(
                position = cameraPosition.position,
                zoom = cameraPosition.zoom,
                bearing = cameraPosition.bearing,
                tilt = cameraPosition.tilt,
                visibleRegion = cameraPosition.visibleRegion,
            )
    }
