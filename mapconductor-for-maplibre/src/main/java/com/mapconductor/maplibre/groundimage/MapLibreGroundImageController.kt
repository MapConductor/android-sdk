package com.mapconductor.maplibre.groundimage

import com.mapconductor.core.groundimage.GroundImageController
import com.mapconductor.core.groundimage.GroundImageEntity
import com.mapconductor.core.groundimage.GroundImageManager
import com.mapconductor.core.groundimage.GroundImageManagerInterface
import com.mapconductor.core.groundimage.GroundImageOverlayRendererInterface
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.maplibre.MapLibreActualGroundImage

class MapLibreGroundImageController(
    groundImageManager: GroundImageManagerInterface<MapLibreActualGroundImage> = GroundImageManager(),
    renderer: MapLibreGroundImageOverlayRenderer,
) : GroundImageController<MapLibreActualGroundImage>(groundImageManager, renderer) {
    suspend fun reapplyStyle() {
        val states = groundImageManager.allEntities().map { it.state }
        if (states.isEmpty()) return

        val addParams =
            states.map { state ->
                object : GroundImageOverlayRendererInterface.AddParamsInterface {
                    override val state: GroundImageState = state
                }
            }

        val handles = renderer.onAdd(addParams)
        handles.forEachIndexed { index, handle ->
            handle?.let {
                groundImageManager.registerEntity(
                    GroundImageEntity(
                        groundImage = it,
                        state = states[index],
                    ),
                )
            }
        }
        renderer.onPostProcess()
    }
}
