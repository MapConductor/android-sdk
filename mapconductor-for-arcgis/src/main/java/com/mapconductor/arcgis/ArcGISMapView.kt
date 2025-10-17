package com.mapconductor.arcgis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.SceneView
import com.arcgismaps.mapping.view.SurfacePlacement
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayController
import com.mapconductor.arcgis.circle.ArcGISCircleOverlayRenderer
import com.mapconductor.arcgis.marker.ArcGISMarkerController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayController
import com.mapconductor.arcgis.polygon.ArcGISPolygonOverlayRenderer
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayController
import com.mapconductor.arcgis.polyline.ArcGISPolylineOverlayRenderer
import com.mapconductor.core.circle.OnCircleEventHandler
import com.mapconductor.core.map.MapViewBase
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.map.OnMapLoadedHandler
import com.mapconductor.core.map.OnMapViewInitializedHandler
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polyline.OnPolylineEventHandler
import android.util.Log
import android.widget.FrameLayout

@Composable
fun ArcGISMapView(
    state: ArcGISMapViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<ArcGISActualMarker>? = null,
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
    content: (@Composable ArcGISMapViewScope.() -> Unit)? = null,
) {
    val scope = remember { ArcGISMapViewScope() } // Use specific scope
    val context = LocalContext.current // Context will be available from MapViewBase too if needed
    val registry = remember { scope.buildRegistry() }
    val owner = LocalLifecycleOwner.current
    owner.lifecycle
    val basemapStyle = remember { ArcGISDesign.toBasemapStyle(state.mapDesignType) }

    MapViewBase(
        state = state,
        modifier = modifier,
        viewProvider = {
            val sceneView = SceneView(context)
            val wrapView =
                WrapSceneView(context).apply {
                    addView(sceneView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                }
            wrapView.sceneView = sceneView
            wrapView
        },
        scope = scope,
        registry = registry,
        holderProvider = { mapView ->
            val options =
                ArcGISMapViewInitOptions(
                    basemapStyle = basemapStyle,
                    elevationSources = state.mapDesignType.elevationSources,
                )
            ArcGISMapViewHolderImpl.create(
                context = context.applicationContext,
                options = options,
            )
        },
        controllerProvider = { holder ->
            ArcGISMapViewControllerImpl(
                holder = holder,
                markerController =
                    getMarkerController(
                        holder = holder,
                        renderingStrategy = markerRenderingStrategy,
                    ),
                polylineController = getPolylineController(holder),
                polygonController = getPolygonController(holder),
                circleController = getCircleController(holder),
            ).also { controller ->
                controller.holder.mapView.onCreate(owner)
                controller.holder.mapView.onResume(owner)
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
                controller.setMapLoadedListener {
                    onMapLoaded?.invoke(state)
                }
                state.setController(controller)

                val restoreCameraPosition = state.cameraPosition.value
                controller.moveCamera(restoreCameraPosition)
            }
        },
        sdkInitialize = {
            val apiKey = context.applicationContext.getArcGisApiKey()
            if (apiKey == null) {
                Log.e("ArcGISMapView", "<meta-data android:name=\"ARCGIS_API_KEY\" /> is required")
                return@MapViewBase false
            }
            ArcGISEnvironment.apiKey = ApiKey.create(apiKey)
            true
        },
        onMapViewInitialized = onMapViewInitialized,
        customDisposableEffect = { _state, _holderRef ->

            // ArcGIS specific DisposableEffect logic
//            DisposableEffect(lifecycle) {
//                val stateId = _stateId // from BaseMapViewState
//                val observer =
//                    object : DefaultLifecycleObserver {
//                        override fun onResume(owner: LifecycleOwner) {
//                            _holderRef.value?.mapView?.onResume(owner)
//                        }
//
//                        override fun onPause(owner: LifecycleOwner) {
//                            _holderRef.value?.mapView?.onPause(owner)
//                        }
//
//                        override fun onDestroy(owner: LifecycleOwner) {
//                            val currentHolder = _holderRef.value
//                            if (currentHolder != null) {
//                                val activity = context.findActivity()
//                                if (activity?.isChangingConfigurations == true) {
//                                    (currentHolder.mapView.parent as? ViewGroup)?.removeView(currentHolder.mapView)
//                                } else {
//                                    // Ensure these calls are safe if mapView might be null or already destroyed
//                                    currentHolder.mapView.onPause(owner)
//                                    currentHolder.mapView.onDestroy(owner)
//                                    _state.controller = null
//                                    ArcGISMapViewHolderStore.remove(stateId) // Clean up from your store
//                                }
//                            }
//                        }
//                    }
//                lifecycle.addObserver(observer)
//                onDispose {
//                    _state.resetInitState()
//                    lifecycle.removeObserver(observer)
//                }
//            }
        },
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

private fun getMarkerController(
    holder: ArcGISMapViewHolder,
    renderingStrategy: MarkerRenderingStrategy<ArcGISActualMarker>? = null,
) = ArcGISMarkerController.create(
    holder = holder,
    renderingStrategy = renderingStrategy,
)
