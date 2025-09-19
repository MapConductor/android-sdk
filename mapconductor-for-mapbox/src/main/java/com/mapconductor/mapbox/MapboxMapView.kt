package com.mapconductor.mapbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalContext
import com.mapbox.maps.MapInitOptions
import com.mapconductor.core.circle.CircleManagerImpl
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.map.OnMapViewInitializedHandler
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.PolygonManagerImpl
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.polyline.PolylineManagerImpl
import com.mapconductor.mapbox.circle.MapboxCircleController
import com.mapconductor.mapbox.circle.MapboxCircleLayer
import com.mapconductor.mapbox.circle.MapboxCircleOverlayRenderer
import com.mapconductor.mapbox.marker.MapboxMarkerController
import com.mapconductor.mapbox.marker.MapboxMarkerOverlayRenderer
import com.mapconductor.mapbox.marker.MarkerDragLayer
import com.mapconductor.mapbox.marker.MarkerLayer
import com.mapconductor.mapbox.polygon.MapboxPolygonConductor
import com.mapconductor.mapbox.polygon.MapboxPolygonLayer
import com.mapconductor.mapbox.polygon.MapboxPolygonOverlayRenderer
import com.mapconductor.mapbox.polyline.MapboxPolylineController
import com.mapconductor.mapbox.polyline.MapboxPolylineLayer
import com.mapconductor.mapbox.polyline.MapboxPolylineOverlayRenderer
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

@Composable
fun MapboxMapView(
    state: MapboxViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<MapboxActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapboxMapViewScope.() -> Unit)? = null,
) {
    val holderRef = remember { Ref<MapboxMapViewHolder>() }
    val context = LocalContext.current
    val controllerRef = remember { Ref<MapboxMapViewControllerImpl>() }
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
                state.cameraPosition.value.toCameraOptions()

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
                MapboxMapViewControllerImpl(
                    holder = holder,
                    markerController =
                        getMarkerController(
                            holder = holder,
                            renderingStrategy = markerRenderingStrategy,
                        ),
                    polylineController = getPolylineController(holder),
                    polygonController = getPolygonController(holder),
                    circleController = getCircleController(holder),
                )
            controller.setCameraMoveListener(state::onCameraChange)
            controller.setMapClickListener(onMapClick)
            controller.setOnCircleClickListener(onCircleClick)
            controller.setOnPolylineClickListener(onPolylineClick)
            controller.setOnPolygonClickListener(onPolygonClick)
            controller.setOnMarkerClickListener(onMarkerClick)
            controller.setOnMarkerDragStart(onMarkerDragStart)
            controller.setOnMarkerDrag(onMarkerDrag)
            controller.setOnMarkerDragEnd(onMarkerDragEnd)
            controller.setOnMarkerAnimateStart(onMarkerAnimateStart)
            controller.setOnMarkerAnimateEnd(onMarkerAnimateEnd)
            controller.setMapDesignTypeChangeListener(state::onMapDesignTypeChange)
            state.setController(controller)
            controller.setMapLoadedListener {
                onMapLoaded?.invoke(state)
            }

            holderRef.value = holder
            controllerRef.value = controller
            true
        },
        onMapViewInitialized = onMapViewInitialized,
        // Pass content if it needs to be rendered within the overlay providers in MapViewBase,
        // or handle it here if it's specific to GoogleMapsView structure before calling MapViewBase.
        // For now, assuming content relates to overlay definitions.
        content = content, // This might need adjustment based on how overlays are handled
    )
}

internal fun getPolygonController(holder: MapboxMapViewHolder): MapboxPolygonConductor {
    val polylineLayer: MapboxPolylineLayer =
        MapboxPolylineLayer(
            sourceId = "polygon-outline-source",
            layerId = "polygon-outline-layer",
        )
    val polylineManager = PolylineManagerImpl<MapboxActualPolyline>()
    val polylineOverlayRenderer =
        MapboxPolylineOverlayRenderer(
            layer = polylineLayer,
            polylineManager = polylineManager,
            holder = holder,
        )

    val polygonManager = PolygonManagerImpl<MapboxActualPolygon>()
    val polygonLayer: MapboxPolygonLayer =
        MapboxPolygonLayer(
            sourceId = "polygon-fill-source",
            layerId = "polygon-fill-layer",
        )
    val polygonOverlayRenderer =
        MapboxPolygonOverlayRenderer(
            layer = polygonLayer,
            polygonManager = polygonManager,
            holder = holder,
        )

    val conductor =
        MapboxPolygonConductor(
            polygonOverlay = polygonOverlayRenderer,
            polylineOverlay = polylineOverlayRenderer,
        )
    return conductor
}

internal fun getCircleController(holder: MapboxMapViewHolder): MapboxCircleController {
    val circleLayer: MapboxCircleLayer =
        MapboxCircleLayer(
            sourceId = "circle-source",
            layerId = "circle-layer",
        )
    val circleManager = CircleManagerImpl<MapboxActualCircle>()

    val renderer =
        MapboxCircleOverlayRenderer(
            layer = circleLayer,
            circleManager = circleManager,
            holder = holder,
        )

    val controller =
        MapboxCircleController(
            renderer = renderer,
        )
    return controller
}

internal fun getPolylineController(holder: MapboxMapViewHolder): MapboxPolylineController {
    val polylineLayer: MapboxPolylineLayer =
        MapboxPolylineLayer(
            sourceId = "polyline-source",
            layerId = "polyline-layer",
        )
    val polylineManager = PolylineManagerImpl<MapboxActualPolyline>()

    val renderer =
        MapboxPolylineOverlayRenderer(
            layer = polylineLayer,
            polylineManager = polylineManager,
            holder = holder,
        )

    val controller =
        MapboxPolylineController(
            renderer = renderer,
        )
    return controller
}

internal fun getMarkerController(
    holder: MapboxMapViewHolder,
    renderingStrategy: MarkerRenderingStrategy<MapboxActualMarker>? = null,
): MapboxMarkerController {
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
        MapboxMarkerOverlayRenderer(
            holder = holder,
            markerLayer = markerLayer,
            dragLayer = dragLayer,
            markerManager = manager,
        )

    val controller =
        MapboxMarkerController(
            renderer = renderer,
            renderingStrategy = renderingStrategy,
        )
    return controller
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
