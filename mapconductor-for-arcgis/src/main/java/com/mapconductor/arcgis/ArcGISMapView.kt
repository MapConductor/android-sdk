package com.mapconductor.arcgis.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.Ref
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
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayController
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayRenderer
import com.mapconductor.arcgis.from
import com.mapconductor.arcgis.groundimage.ArcGISGroundImageController
import com.mapconductor.arcgis.groundimage.ArcGISGroundImageOverlayRenderer
import com.mapconductor.arcgis.marker.ArcGISMarkerController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayRenderer
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayController
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayRenderer
import com.mapconductor.arcgis.raster.ArcGISRasterLayerController
import com.mapconductor.arcgis.raster.ArcGISRasterLayerOverlayRenderer
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.MutableMapServiceRegistry
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.marker.MarkerEventControllerInterface
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerRenderingStrategyInterface
import com.mapconductor.core.marker.MarkerRenderingSupport
import com.mapconductor.core.marker.MarkerRenderingSupportKey
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler
import com.mapconductor.core.tileserver.TileServerRegistry
import java.util.concurrent.atomic.AtomicLong
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
    state: ArcGISMapViewState,
    modifier: Modifier = Modifier,
    markerTiling: MarkerTilingOptions? = null,
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
        markerTiling = markerTiling,
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
    state: ArcGISMapViewState,
    modifier: Modifier = Modifier,
    markerTiling: MarkerTilingOptions? = null,
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
    val serviceRegistry = remember { MutableMapServiceRegistry() }
    val owner = LocalLifecycleOwner.current
    val basemapStyle = remember { ArcGISDesign.toBasemapStyle(state.mapDesignType) }
    val cameraState = remember { mutableStateOf<MapCameraPositionInterface?>(state.cameraPosition) }
    val controllerRef = remember { Ref<ArcGISMapViewController>() }
    val controllerGeneration = remember { AtomicLong(0L) }

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
        serviceRegistry = serviceRegistry,
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

            suspendCancellableCoroutine<ArcGISMapViewHolder> { cont ->
                coroutine.launch {
                    scene.loadStatus.collect {
                        when (it) {
                            is LoadStatus.Loaded -> {
                                wrapView.sceneView.scene = scene
                                val holder =
                                    ArcGISMapViewHolder(
                                        mapView = wrapView,
                                        map = wrapView.sceneView,
                                    )
                                cont.resume(holder, onCancellation = {})
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
                    markerTiling = markerTiling ?: MarkerTilingOptions.Default,
                )
            val polylineController = getPolylineController(holder)
            val rasterLayerController = getRasterLayerController(holder)
            val polygonController = getPolygonController(holder, rasterLayerController)
            val circleController = getCircleController(holder)
            val groundImageController = getGroundImageController(holder)

            // Defer initial camera update until controller is created and view is laid out

            ArcGISMapViewController(
                holder = holder,
                markerController = markerController,
                polylineController = polylineController,
                polygonController = polygonController,
                circleController = circleController,
                groundImageController = groundImageController,
                rasterLayerController = rasterLayerController,
            ).also { mapController ->
                serviceRegistry.clear()
                serviceRegistry.put(
                    MarkerRenderingSupportKey,
                    object : MarkerRenderingSupport<ArcGISActualMarker> {
                        override fun createMarkerRenderer(
                            strategy: MarkerRenderingStrategyInterface<ArcGISActualMarker>,
                        ): MarkerOverlayRendererInterface<ArcGISActualMarker> =
                            mapController.createMarkerRenderer(strategy)

                        override fun createMarkerEventController(
                            controller: StrategyMarkerController<ArcGISActualMarker>,
                            renderer: MarkerOverlayRendererInterface<ArcGISActualMarker>,
                        ): MarkerEventControllerInterface<ArcGISActualMarker> =
                            mapController.createMarkerEventController(controller, renderer)

                        override fun registerMarkerEventController(
                            controller: MarkerEventControllerInterface<ArcGISActualMarker>,
                        ) {
                            mapController.registerMarkerEventController(controller)
                        }

                        override fun onMarkerRenderingReady() {
                            mapController.sendInitialCameraUpdate()
                        }
                    },
                )

                controllerRef.value = mapController
                mapController.setMapClickListener(onMapClick)
                mapController.setOnCircleClickListener(onCircleClick)
                mapController.setOnPolylineClickListener(onPolylineClick)
                mapController.setOnPolygonClickListener(onPolygonClick)
                mapController.setOnMarkerClickListener(onMarkerClick)
                mapController.setOnMarkerDragStart(onMarkerDragStart)
                mapController.setOnMarkerDrag(onMarkerDrag)
                mapController.setOnMarkerDragEnd(onMarkerDragEnd)
                mapController.setOnMarkerAnimateStart(onMarkerAnimateStart)
                mapController.setOnMarkerAnimateEnd(onMarkerAnimateEnd)
                mapController.setMapDesignTypeChangeListener(state::onMapDesignTypeChange)
                state.setController(mapController)

                // Set camera listeners immediately so they are ready to receive
                // camera updates from external sources (e.g. camera sync scenarios).
                mapController.setCameraMoveStartListener {
                    cameraState.value = it
                    state.updateCameraPosition(it)
                    onCameraMoveStart?.invoke(it)
                }
                mapController.setCameraMoveListener {
                    cameraState.value = it
                    state.updateCameraPosition(it)
                    onCameraMove?.invoke(it)
                }
                mapController.setCameraMoveEndListener {
                    cameraState.value = it
                    state.updateCameraPosition(it)
                    onCameraMoveEnd?.invoke(it)
                }

                // Avoid early ArcGIS viewpoint updates overwriting the desired initial camera.
                // Apply the initial camera after layout.
                val initialCameraPosition = state.cameraPosition
                val generation = controllerGeneration.incrementAndGet()
                holder.mapView.post {
                    if (controllerGeneration.get() != generation) return@post
                    mapController.moveCamera(MapCameraPosition.from(initialCameraPosition))
                    mapController.sendInitialCameraUpdate()
                }
            }
        },
        sdkInitialize = {
            sdkInitialize?.invoke(context) ?: defaultArcGISInitialize(context)
        },
        onMapLoaded = onMapLoaded,
        customDisposableEffect = { _, holderRef ->
            DisposableEffect(state.id) {
                onDispose {
                    // Invalidate any pending `post { ... }` work that captured older controllers.
                    controllerGeneration.incrementAndGet()
                    // Detach callbacks so a disposed controller cannot keep overwriting state.cameraPosition
                    // during rapid ArcGIS<->other provider switching.
                    controllerRef.value?.apply {
                        setCameraMoveStartListener(null)
                        setCameraMoveListener(null)
                        setCameraMoveEndListener(null)
                        setMapClickListener(null)
                        setMapLongClickListener(null)
                    }
                    controllerRef.value = null
                    state.clearController()
                    holderRef.value?.mapView?.apply {
                        onPause(owner)
                        onStop(owner)
                        onDestroy(owner)
                    }
                }
            }
        },
        content = content,
    )
}

private fun getCircleController(holder: ArcGISMapViewHolder): ArcGISCircleOverlayController {
    val circleLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.DrapedFlat
        }
    holder.map.graphicsOverlays.add(circleLayer)

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
    holder.map.graphicsOverlays.add(polylineLayer)

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

private fun getPolygonController(
    holder: ArcGISMapViewHolder,
    rasterLayerController: ArcGISRasterLayerController,
): ArcGISPolygonOverlayController {
    val polygonLayer: GraphicsOverlay =
        GraphicsOverlay().apply {
            sceneProperties.surfacePlacement = SurfacePlacement.DrapedBillboarded
        }
    holder.map.graphicsOverlays.add(polygonLayer)

    val renderer =
        ArcGISPolygonOverlayRenderer(
            polygonLayer = polygonLayer,
            holder = holder,
            rasterLayerController = rasterLayerController,
        )

    val controller =
        ArcGISPolygonOverlayController(
            renderer = renderer,
        )
    return controller
}

private fun getMarkerController(
    holder: ArcGISMapViewHolder,
    markerTiling: MarkerTilingOptions,
) = ArcGISMarkerController.create(
    holder = holder,
    markerTiling = markerTiling,
)

private fun getRasterLayerController(holder: ArcGISMapViewHolder): ArcGISRasterLayerController {
    val renderer =
        ArcGISRasterLayerOverlayRenderer(
            holder = holder,
        )
    return ArcGISRasterLayerController(
        renderer = renderer,
    )
}

private fun getGroundImageController(holder: ArcGISMapViewHolder): ArcGISGroundImageController {
    val tileServer = TileServerRegistry.get()
    val renderer =
        ArcGISGroundImageOverlayRenderer(
            holder = holder,
            tileServer = tileServer,
        )
    return ArcGISGroundImageController(renderer = renderer)
}

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
