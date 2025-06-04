package com.mapconductor.mapbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnMapClickHandler
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

@Composable
fun MapboxMapView(
    state: IMapboxMapViewState,
    modifier: Modifier = Modifier,
    onMapClick: OnMapClickHandler = {},
    content: (@Composable MapboxMapViewScope.() -> Unit)? = null,
) {
    val holderRef = remember { Ref<MapboxMapViewHolder>() }
    val context = LocalContext.current
    val controllerRef = remember { Ref<MapboxMapViewController>() }
    val scope = remember { MapboxMapViewScope() }
    val registry = remember { scope.buildRegistry() }

    MapViewBase(
        state = state,
        modifier = modifier,
        holderRef = holderRef,
        controllerRef = controllerRef,
        mapProvider = { this.map },
        viewProvider = { this.mapView },
        scope = scope,
        registry = registry,
        onInitialize = {
            val cameraOptions =
                state.mapCameraPosition.value?.let { it ->
                    CameraOptions
                        .Builder()
                        .apply {
                            center(GeoPoint.from(it.position).toPoint())
                            zoom(it.zoom)
                            pitch(it.tilt)
                            bearing(it.bearing)
                        }.build()
                }

            val styleUri = state.mapDesignType.getValue()
            val mapInitOptions =
                MapInitOptions(
                    context = context,
                    textureView = true,
                    styleUri = styleUri,
                    cameraOptions = cameraOptions,
                )

            val holder = MapboxMapViewHolderImpl.create(context, mapInitOptions)

            val onCameraMove =
                (state as? MapboxMapViewState)?.let {
                    it::OnCameraChange
                }
            val controller =
                MapboxMapViewController(
                    holder = holder,
                    onCameraMove = onCameraMove,
                    onMapClick = onMapClick,
                )

            holderRef.value = holder
            controllerRef.value = controller
            true
        },
        // Pass content if it needs to be rendered within the overlay providers in MapViewBase,
        // or handle it here if it's specific to GoogleMapsView structure before calling MapViewBase.
        // For now, assuming content relates to overlay definitions.
        content = content, // This might need adjustment based on how overlays are handled
    )
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
