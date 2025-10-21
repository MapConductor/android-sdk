package com.mapconductor.maplibre

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.map.OnMapViewInitializedHandler
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polyline.PolylineManagerImpl
import com.mapconductor.maplibre.marker.MapLibreMarkerController
import com.mapconductor.maplibre.marker.MapLibreMarkerOverlayRenderer
import com.mapconductor.maplibre.marker.MarkerDragLayer
import com.mapconductor.maplibre.marker.MarkerLayer
import com.mapconductor.maplibre.polyline.MapLibrePolylineController
import com.mapconductor.maplibre.polyline.MapLibrePolylineLayer
import com.mapconductor.maplibre.polyline.MapLibrePolylineOverlayRenderer
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun MapLibreMapView(
    state: MapLibreViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<MapLibreActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
//    onCircleClick: OnCircleEventHandler? = null,
//    onPolylineClick: OnPolylineEventHandler? = null,
//    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapLibreMapViewScope.() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = remember { MapLibreMapViewScope() }
    val registry = remember { scope.buildRegistry() }

    MapViewBase(
        state = state,
        modifier = modifier,
        viewProvider = {
            val cameraPosition =
                state.cameraPosition.value.toCameraPosition()
            val mapInitOptions = MapLibreMapOptions.createFromAttributes(context)
                .camera(cameraPosition)
                .textureMode(true)
                // Don't set style here - it will be set in holderProvider

            MapView(context, mapInitOptions)
        },
        scope = scope,
        registry = registry,
        onMapViewInitialized = onMapViewInitialized,
        holderProvider = { mapView ->
            suspendCancellableCoroutine { continuation ->
                mapView.getMapAsync { map ->
                    // Set style and wait for it to load completely
                    map.setStyle(state.mapDesignType.styleJsonURL) { loadedStyle ->
                        // Resume only after style is fully loaded
                        continuation.resume(MapLibreMapViewHolderImpl(mapView, map)) {}
                    }
                }
            }
        },
        controllerProvider = { holder ->
            val markerController = getMarkerController(
                holder = holder,
                renderingStrategy = markerRenderingStrategy,
            )
            val polylineController = getPolylineController(
                holder = holder,
            )
            MapLibreViewControllerImpl(
                holder = holder,
                markerController = markerController,
                polylineController = polylineController,
            ).also { controller ->
                // Store controller reference in holder
                (holder as? MapLibreMapViewHolderImpl)?.setController(controller)
                controller.setCameraMoveListener(state::onCameraChange)
                controller.setMapClickListener(onMapClick)
                controller.setMapDesignTypeChangeListener(state::onMapDesignTypeChange)
                state.setController(controller)
                controller.setMapLoadedListener {
                    onMapLoaded?.invoke(state)
                }
            }
        },
        sdkInitialize = {
            MapLibre.getInstance(context)
            true
        },

        // Pass content if it needs to be rendered within the overlay providers in MapViewBase,
        // or handle it here if it's specific to GoogleMapsView structure before calling MapViewBase.
        // For now, assuming content relates to overlay definitions.
        content = content, // This might need adjustment based on how overlays are handled
    )
}

internal fun getMarkerController(
    holder: MapLibreMapViewHolder,
    renderingStrategy: MarkerRenderingStrategy<MapLibreActualMarker>? = null,
): MapLibreMarkerController {
    val manager = renderingStrategy?.markerManager ?: MarkerManager.defaultManager()
    val markerLayer: MarkerLayer =
        MarkerLayer(
            sourceId = "markers-source",
            layerId = "markers-layer",
        )
    val dragLayer: MarkerDragLayer =
        MarkerDragLayer(
            sourceId = "marker-drag-source",
            layerId = "marker-drag-layer",
        )
    val renderer =
        MapLibreMarkerOverlayRenderer(
            holder = holder,
            markerLayer = markerLayer,
            dragLayer = dragLayer,
            markerManager = manager,
        )

    val controller =
        MapLibreMarkerController(
            renderer = renderer,
            renderingStrategy = renderingStrategy,
        )
    return controller
}


internal fun getPolylineController(holder: MapLibreMapViewHolder): MapLibrePolylineController {
    val polylineLayer: MapLibrePolylineLayer =
        MapLibrePolylineLayer(
            sourceId = "polyline-source",
            layerId = "polyline-layer",
        )
    val polylineManager = PolylineManagerImpl<MapLibreActualPolyline>()

    val renderer =
        MapLibrePolylineOverlayRenderer(
            layer = polylineLayer,
            polylineManager = polylineManager,
            holder = holder,
        )

    val controller =
        MapLibrePolylineController(
            renderer = renderer,
        )
    return controller
}


internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
