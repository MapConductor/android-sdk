package com.mapconductor.openmobilemaps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapconductor.core.map.MapViewBase
import io.openmobilemaps.mapscore.MapsCore
import io.openmobilemaps.mapscore.map.layers.TiledRasterLayer
import io.openmobilemaps.mapscore.shared.map.coordinates.Coord
import io.openmobilemaps.mapscore.shared.map.coordinates.CoordinateSystemIdentifiers
import android.util.Log

@Composable
fun OpenMobileMapView(
    state: IOpenMobileMapViewState,
    modifier: Modifier = Modifier,
    content: (@Composable OpenMobileMapViewScope.() -> Unit)? = null,
) {
    val holderRef = remember { Ref<OpenMobileMapViewHolder>() }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val controllerRef = remember { Ref<OpenMobileMapViewController>() }
    val scope = remember { OpenMobileMapViewScope() }
    val registry = remember { scope.buildRegistry() }

    key(state) {
        MapViewBase(
            state = state,
            modifier = modifier,
            holderRef = holderRef,
            controllerRef = controllerRef,
            viewProvider = { this.mapView },
            scope = scope,
            registry = registry,
            onInitialize = {
                Log.d("openMobile", "------>onInitalize")
                MapsCore.initialize()

                val holder = OpenMobileMapViewHolderImpl.create(context, lifecycle)
                holderRef.value = holder

                val controller = OpenMobileMapViewController(
                    holder = holder,
                )
                controllerRef.value = controller



                // OpenStreetMapタイルソースを作成
                val tileSource = TiledRasterLayer(context,"https://tile.openstreetmap.org/{z}/{x}/{y}.png", "osm")


                // レイヤーをマップに追加
                holder.map.addLayer(tileSource.layerInterface())

                holder.map.getCamera().moveToCenterPositionZoom(
                    Coord(CoordinateSystemIdentifiers.EPSG4326(), 139.7671, 35.6812, 0.0),
                    10000000.0, false)
                true
            },
            content = content,
        )
    }
}
