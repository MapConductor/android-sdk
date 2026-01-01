package com.mapconductor.arcgis.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.LoadStatus
import com.arcgismaps.mapping.ArcGISScene
import com.arcgismaps.mapping.ArcGISTiledElevationSource
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.SceneView
import com.arcgismaps.mapping.view.SurfacePlacement
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayController
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayRenderer
import com.mapconductor.arcgis.marker.ArcGISMarkerController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayRenderer
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayController
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayRenderer
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler
import android.util.Log
import android.widget.FrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ArcGISMapView(
    state: ArcGISMapViewStateImpl,
    modifier: Modifier = Modifier,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    content: (@Composable ArcGISMapViewScope.() -> Unit)? = null,
) {
    @Suppress("DEPRECATION")
    ArcGISMapView(
        state = state,
        modifier = modifier,
        sdkInitialize = sdkInitialize,
        onMapLoaded = onMapLoaded,
        onCameraMoveStart = onCameraMoveStart,
        onCameraMove = onCameraMove,
        onCameraMoveEnd = onCameraMoveEnd,
        onMapClick = onMapClick,
        onMarkerClick = null,
        onMarkerDragStart = null,
        onMarkerDrag = null,
        onMarkerDragEnd = null,
        onMarkerAnimateStart = null,
        onMarkerAnimateEnd = null,
        onCircleClick = null,
        onPolylineClick = null,
        onPolygonClick = null,
        content = content,
    )
}

@Deprecated("Use CircleState/PolylineState/PolygonState onClick instead.")
@Composable
fun ArcGISMapView(
    state: ArcGISMapViewStateImpl,
    modifier: Modifier = Modifier,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler?,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable ArcGISMapViewScope.() -> Unit)? = null,
) {
    val scope = remember { ArcGISMapViewScope() } // Use specific scope
    val context = LocalContext.current // Context will be available from MapViewBase too if needed
    val registry = remember { scope.buildRegistry() }
    val owner = LocalLifecycleOwner.current
    owner.lifecycle
    val basemapStyle = remember { ArcGISDesign.toBasemapStyle(state.mapDesignType) }
    val cameraState = remember { mutableStateOf<MapCameraPosition?>(state.cameraPosition) }

    MapViewBase(
        state = state,
        cameraState = cameraState,
        modifier = modifier,
        viewProvider = {
            val sceneView = SceneView(context)
            val wrapView =
                WrapSceneView(context).apply {
                    addView(sceneView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                }
            wrapView.sceneView = sceneView
            // Ensure lifecycle owner is set before the view is attached/drawn
            // to avoid GeoView.lifeCycleOwner UninitializedPropertyAccessException
            sceneView.onCreate(owner)
            sceneView.onResume(owner)
            wrapView
        },
        scope = scope,
        registry = registry,
        holderProvider = { wrapView ->
            val options =
                ArcGISMapViewInitOptions(
                    basemapStyle = basemapStyle,
                    elevationSources = state.mapDesignType.elevationSources,
                )

            val scene = ArcGISScene(options.basemapStyle)

            options.elevationSources.forEach {
                val source = ArcGISTiledElevationSource(it)
                scene.baseSurface.elevationSources.add(source)
            }

            wrapView.sceneView.scene = scene

            val coroutine = CoroutineScope(Dispatchers.Default)

            suspendCancellableCoroutine<ArcGISMapViewHolderImpl> { cont ->
                coroutine.launch {
                    scene.loadStatus.collect {
                        when (it) {
                            is LoadStatus.Loaded -> {
                                wrapView.sceneView.scene = scene
                                val holder =
                                    ArcGISMapViewHolderImpl(
                                        mapView = wrapView,
                                        map = wrapView.sceneView,
                                    )
                                cont.resume(holder) {}
                            }
                            is LoadStatus.FailedToLoad -> {
                                cont.cancel(it.error)
                            }
                            else -> {
                                // Do nothing here
                            }
                        }
                    }
                }
            }
        },
        controllerProvider = { holder ->

            val markerController =
                getMarkerController(
                    holder = holder,
                )
            val polylineController = getPolylineController(holder)
            val polygonController = getPolygonController(holder)
            val circleController = getCircleController(holder)

            // Defer initial camera update until controller is created and view is laid out

            ArcGISMapViewControllerImpl(
                holder = holder,
                markerController = markerController,
                polylineController = polylineController,
                polygonController = polygonController,
                circleController = circleController,
            ).also { controller ->
                controller.setCameraMoveStartListener {
                    cameraState.value = it
                    state.updateCameraPosition(it)
                    onCameraMoveStart?.invoke(it)
                }
                controller.setCameraMoveListener {
                    cameraState.value = it
                    state.updateCameraPosition(it)
                    onCameraMove?.invoke(it)
                }
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

                controller.setCameraMoveEndListener {
                    // Post an initial camera update after layout to compute visibleRegion correctly
                    holder.mapView.post {
                        val restoreCameraPosition = state.cameraPosition
                        controller.moveCamera(restoreCameraPosition)
                        controller.sendInitialCameraUpdate()

                        controller.setCameraMoveEndListener {
                            cameraState.value = it
                            state.updateCameraPosition(it)
                            onCameraMoveEnd?.invoke(it)
                        }
                    }
                }
            }
        },
        sdkInitialize = {
            sdkInitialize?.invoke(context) ?: defaultArcGISInitialize(context)
        },
        onMapLoaded = onMapLoaded,
        content = content,
    )
}

private fun getCircleController(holder: ArcGISMapViewHolder): ArcGISCircleOverlayController {
    val circleLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.DrapedFlat
        }

    val renderer =
        ArcGISCircleOverlayRenderer(
            circleLayer = circleLayer,
            holder = holder,
        )

    val controller =
        ArcGISCircleOverlayController(
            renderer = renderer,
        )
    return controller
}

private fun getPolylineController(holder: ArcGISMapViewHolder): ArcGISPolylineOverlayController {
    val polylineLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.DrapedBillboarded
        }

    val renderer =
        ArcGISPolylineOverlayRenderer(
            polylineLayer = polylineLayer,
            holder = holder,
        )

    val controller =
        ArcGISPolylineOverlayController(
            renderer = renderer,
        )
    return controller
}

private fun getPolygonController(holder: ArcGISMapViewHolder): ArcGISPolygonOverlayController {
    val polygonLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.DrapedBillboarded
        }

    val renderer =
        ArcGISPolygonOverlayRenderer(
            polygonLayer = polygonLayer,
            holder = holder,
        )

    val controller =
        ArcGISPolygonOverlayController(
            renderer = renderer,
        )
    return controller
}

private fun getMarkerController(holder: ArcGISMapViewHolder) =
    ArcGISMarkerController.create(
        holder = holder,
    )

/**
 * Default ArcGIS SDK initialization using API Key authentication.
 *
 * This function is used when no custom sdkInitialize parameter is provided to ArcGISMapView.
 * It reads the API Key from AndroidManifest.xml metadata and configures ArcGISEnvironment.
 *
 * @param context Application context
 * @return true if initialization succeeded, false otherwise
 */
private suspend fun defaultArcGISInitialize(context: android.content.Context): Boolean {
    if (ArcGISEnvironment.authenticationManager.arcGISCredentialStore
            .getCredentials()
            .isEmpty()
    ) {
        val apiKey = context.applicationContext.getArcGisApiKey()
        if (apiKey == null) {
            Log.e("ArcGISMapView", "<meta-data android:name=\"ARCGIS_API_KEY\" /> is required")
            return false
        }
        ArcGISEnvironment.apiKey = ApiKey.create(apiKey)
    }
    return true
}
