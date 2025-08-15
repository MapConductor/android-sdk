package com.mapconductor.mapbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import com.mapbox.maps.MapInitOptions
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

@Composable
fun MapboxMapView(
    state: IMapboxMapViewState,
    modifier: Modifier = Modifier,
    onMapClick: OnMapEventHandler? = {},
    onMarkerClick: OnMarkerEventHandler? = {},
    onMarkerDragStart: OnMarkerEventHandler? = {},
    onMarkerDrag: OnMarkerEventHandler? = {},
    onMarkerDragEnd: OnMarkerEventHandler? = {},
    onMarkerAnimateStart: OnMarkerEventHandler? = {},
    onMarkerAnimateEnd: OnMarkerEventHandler? = {},
    onCircleClick: OnCircleEventHandler? = {},
    onPolylineClick: OnPolylineEventHandler? = {},
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
        viewProvider = { this.mapView },
        scope = scope,
        registry = registry,
        onInitialize = {
            MapboxInitSDK(context)

            val cameraOptions =
                state.mapCameraPosition.value?.toCameraOptions()

            val styleUri = state.mapDesignType.getValue()
            val mapInitOptions =
                MapInitOptions(
                    context = context,
                    textureView = true,
                    styleUri = styleUri,
                    cameraOptions = cameraOptions,
                )

            val holder = MapboxMapViewHolderImpl.create(context, mapInitOptions)

            val controller =
                MapboxMapViewController(
                    holder = holder,
                )
            (state as? MapboxMapViewState)?.let { mapViewState ->
                mapViewState.controller = controller
                controller.setCameraMoveListener(mapViewState::OnCameraChange)
            }
            controller.setMapClickListener(onMapClick)
            controller.setMarkerClickListener(onMarkerClick)
            controller.setMarkerDragStartListener(onMarkerDragStart)
            controller.setMarkerDragListener(onMarkerDrag)
            controller.setMarkerDragEndListener(onMarkerDragEnd)
            controller.setCircleClickListener(onCircleClick)
            controller.setPolylineClickListener(onPolylineClick)
            controller.setOnMarkerAnimationStart(onMarkerAnimateStart)
            controller.setOnMarkerAnimationEnd(onMarkerAnimateEnd)
            controller.setOnMarkerAnimationStart(onMarkerAnimateStart)
            controller.setOnMarkerAnimationEnd(onMarkerAnimateEnd)

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
