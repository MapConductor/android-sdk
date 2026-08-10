package com.mapconductor.openmobilemaps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapconductor.compose.map.BaseMapViewSaver
import com.mapconductor.compose.map.MapViewBase
import com.mapconductor.core.OnCameraMoveHandler
import com.mapconductor.core.OnMapEventHandler
import com.mapconductor.core.OnMapLoadedHandler
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.map.CameraRestriction
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.MutableMapServiceRegistry
import com.mapconductor.core.marker.MarkerEventControllerInterface
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerRenderingStrategyInterface
import com.mapconductor.core.marker.MarkerRenderingSupport
import com.mapconductor.core.marker.MarkerRenderingSupportKey
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.openmobilemaps.marker.OpenMobileMapsMarkerController
import io.openmobilemaps.mapscore.MapsCore
import io.openmobilemaps.mapscore.map.loader.DataLoader
import io.openmobilemaps.mapscore.map.view.MapView
import io.openmobilemaps.mapscore.map.view.MapViewState as OmmMapViewState
import io.openmobilemaps.mapscore.shared.map.MapConfig
import io.openmobilemaps.mapscore.shared.map.coordinates.CoordinateSystemFactory
import io.openmobilemaps.mapscore.shared.map.loader.LoaderInterface
import java.util.UUID
import android.content.Context
import android.os.Bundle
import android.widget.FrameLayout
import kotlinx.coroutines.flow.first

interface OpenMobileMapsViewStateInterface : MapViewStateInterface<OpenMobileMapsMapDesignTypeInterface>

/**
 * アプリ側の状態。
 *
 * カメラの保持・[MapViewStateInterface.moveCameraTo]・`fitBounds` はコアの
 * [MapViewState] が持つ。ドライバーが書くのは `id` / `mapDesignType` と、
 * 共変の [getMapViewHolder]（**1 行だが消さないこと**。消すとアプリ側の
 * `state.getMapViewHolder()?.map` が静的型を失いソース非互換になる）だけ。
 */
class OpenMobileMapsViewState(
    mapDesignType: OpenMobileMapsMapDesignTypeInterface,
    override val id: String,
    cameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewState<OpenMobileMapsMapDesignTypeInterface>(cameraPosition),
    OpenMobileMapsViewStateInterface {
    private var controller: OpenMobileMapsMapViewController? = null
    private var current: OpenMobileMapsMapDesignTypeInterface = mapDesignType

    override var mapDesignType: OpenMobileMapsMapDesignTypeInterface
        get() = current
        set(value) {
            current = value
            controller?.setMapDesignType(value)
        }

    internal fun setController(controller: OpenMobileMapsMapViewController) {
        this.controller = controller
        attachController(controller)
    }

    internal fun onMapDesignTypeChange(value: OpenMobileMapsMapDesignTypeInterface) {
        current = value
    }

    /** 戻り型をこのプロバイダのホルダーへ絞る（アプリが `?.map` を取れる形を保つため）。 */
    override fun getMapViewHolder(): OpenMobileMapsMapViewHolder? =
        super.getMapViewHolder() as? OpenMobileMapsMapViewHolder

    internal fun updateCameraPosition(cameraPosition: MapCameraPosition) {
        setCameraPositionInternal(cameraPosition)
    }
}

class OpenMobileMapsMapViewSaver : BaseMapViewSaver<OpenMobileMapsViewState>() {
    override fun saveMapDesign(
        state: OpenMobileMapsViewState,
        bundle: Bundle,
    ) {
        bundle.putString("id", state.mapDesignType.id)
        bundle.putString("tileUrlTemplate", state.mapDesignType.tileUrlTemplate)
        bundle.putInt("tileSize", state.mapDesignType.tileSize)
    }

    override fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition,
    ): OpenMobileMapsViewState =
        OpenMobileMapsViewState(
            id = stateId,
            mapDesignType =
                OpenMobileMapsDesign(
                    id = mapDesignBundle?.getString("id") ?: OpenMobileMapsDesign.OpenStreetMap.id,
                    tileUrlTemplate =
                        mapDesignBundle?.getString("tileUrlTemplate")
                            ?: OpenMobileMapsDesign.OpenStreetMap.tileUrlTemplate,
                    tileSize = mapDesignBundle?.getInt("tileSize")?.takeIf { it > 0 } ?: 256,
                ),
            cameraPosition = cameraPosition,
        )

    override fun getStateId(state: OpenMobileMapsViewState): String = state.id
}

@Composable
fun rememberOpenMobileMapsMapViewState(
    mapDesign: OpenMobileMapsMapDesignTypeInterface = OpenMobileMapsDesign.OpenStreetMap,
    cameraPosition: MapCameraPositionInterface = MapCameraPosition.Default,
): OpenMobileMapsViewState {
    val stateId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    val state =
        rememberSaveable(stateSaver = OpenMobileMapsMapViewSaver().createSaver()) {
            mutableStateOf(
                OpenMobileMapsViewState(
                    id = stateId,
                    mapDesignType = mapDesign,
                    cameraPosition = MapCameraPosition.from(cameraPosition),
                ),
            )
        }
    return state.value
}

/**
 * 地図の Composable。
 *
 * ## SDK 固有の必須手順が 2 つある
 *
 * 1. [MapsCore.initialize] … ネイティブライブラリを読む。忘れると `UnsatisfiedLinkError`。
 * 2. [MapView.registerLifecycle] … レンダリングループを回す。**忘れると地図が真っ黒のまま**
 *    エラーも出ない。
 */
@Composable
fun OpenMobileMapsMapView(
    state: OpenMobileMapsViewState,
    modifier: Modifier = Modifier,
    markerTiling: MarkerTilingOptions? = null,
    cameraRestriction: CameraRestriction? = null,
    sdkInitialize: (suspend (Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMapLongClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    content: (@Composable OpenMobileMapsMapViewScope.() -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = remember { OpenMobileMapsMapViewScope() }
    val registry = remember { scope.buildRegistry() }
    val cameraState = remember { mutableStateOf<MapCameraPositionInterface?>(state.cameraPosition) }
    val loaders =
        remember { arrayListOf<LoaderInterface>(DataLoader(context, context.cacheDir, CACHE_BYTES, "", USER_AGENT)) }

    MapViewBase(
        state = state,
        cameraState = cameraState,
        modifier = modifier,
        viewProvider = {
            OpenMobileMapsMapSurface(context).apply {
                val inner = MapView(context)
                mapView = inner
                addView(
                    inner,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    ),
                )
                // 地図は EPSG:3857 で構成する（タイルがすべて 3857 で、ズームの縮尺の
                // 導出も「地図単位 = メルカトルメートル」を前提にしているため）。
                //
                // 密度は densityDpi を渡すこと。SDK のサンプルは xdpi（実測の物理 dpi）を
                // 渡すが、それだと端末ごとに縮尺が数 % ずれ、Google Maps と大きさが揃わない。
                inner.setupMap(
                    MapConfig(CoordinateSystemFactory.getEpsg3857System()),
                    context.resources.displayMetrics.densityDpi
                        .toFloat(),
                    false,
                    false,
                )
                inner.registerLifecycle(lifecycleOwner.lifecycle)
            }
        },
        scope = scope,
        registry = registry,
        onMapLoaded = onMapLoaded,
        holderProvider = { surface ->
            // setupMap は非同期に GL スレッドで完了する。INITIALIZED になる前に
            // レイヤを足すと落ちるので待つ。
            surface.mapView.mapViewState.first { it != OmmMapViewState.UNINITIALIZED }
            OpenMobileMapsMapViewHolder(surface, surface.mapView.requireMapInterface())
        },
        controllerProvider = { holder ->
            createOpenMobileMapsViewController(
                holder = holder,
                loaders = loaders,
                density = context.resources.displayMetrics.density,
                markerTiling = markerTiling ?: MarkerTilingOptions.Default,
                serviceRegistry = state.serviceRegistry,
            ).also { mapController ->
                mapController.setMapDesignType(state.mapDesignType)
                mapController.installListeners()
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
                mapController.setMapClickListener(onMapClick)
                mapController.setMapLongClickListener(onMapLongClick)
                mapController.setMapDesignTypeChangeListener(state::onMapDesignTypeChange)
                cameraRestriction?.let { mapController.setCameraRestriction(it) }
                mapController.moveCamera(MapCameraPosition.from(state.cameraPosition))
                state.setController(mapController)
                holder.mapView.post { mapController.sendInitialCameraUpdate() }
            }
        },
        sdkInitialize = {
            if (sdkInitialize != null) {
                sdkInitialize(context)
            } else {
                MapsCore.initialize()
                true
            }
        },
        content = content,
    )
}

/**
 * 命令的なコントローラ一式を組み立てる。Compose からも、React Native のような
 * 非 Compose ホストからも同じものを使えるようにここに置く。
 */
fun createOpenMobileMapsViewController(
    holder: OpenMobileMapsMapViewHolder,
    loaders: ArrayList<LoaderInterface>,
    density: Float,
    markerTiling: MarkerTilingOptions = MarkerTilingOptions.Default,
    serviceRegistry: MutableMapServiceRegistry? = null,
): OpenMobileMapsMapViewController {
    val layers = OpenMobileMapsLayers()
    layers.attachTo(holder.map)

    val markerRenderer =
        OpenMobileMapsMarkerOverlayRenderer(
            holder = holder,
            markerManager = MarkerManager.defaultManager(),
            iconLayer = layers.iconLayer,
        )

    val mapController =
        OpenMobileMapsMapViewController(
            holder = holder,
            layers = layers,
            markerController = OpenMobileMapsMarkerController(markerRenderer, markerTiling),
            polylineController =
                OpenMobileMapsPolylineController(
                    OpenMobileMapsPolylineOverlayRenderer(
                        holder = holder,
                        polylineManager = PolylineManager(),
                        lineLayer = layers.polylineLayer,
                    ),
                ),
            polygonController =
                OpenMobileMapsPolygonController(
                    OpenMobileMapsPolygonOverlayRenderer(
                        holder = holder,
                        polygonManager = PolygonManager(),
                        fillLayer = layers.polygonFillLayer,
                        outlineLayer = layers.polygonOutlineLayer,
                    ),
                ),
            circleController =
                OpenMobileMapsCircleController(
                    OpenMobileMapsCircleOverlayRenderer(
                        holder = holder,
                        circleManager = CircleManager(),
                        fillLayer = layers.circleFillLayer,
                        outlineLayer = layers.circleOutlineLayer,
                    ),
                ),
            groundImageController =
                OpenMobileMapsGroundImageController(
                    OpenMobileMapsGroundImageOverlayRenderer(holder = holder, layers = layers),
                ),
            rasterLayerController =
                OpenMobileMapsRasterLayerController(
                    OpenMobileMapsRasterLayerOverlayRenderer(
                        holder = holder,
                        layers = layers,
                        loaders = loaders,
                        density = density,
                    ),
                ),
            loaders = loaders,
            density = density,
        )

    serviceRegistry?.let { serviceRegistryTarget ->
        mapController.declareCapabilities(serviceRegistryTarget)
        serviceRegistryTarget.put(
            MarkerRenderingSupportKey,
            object : MarkerRenderingSupport<OpenMobileMapsActualMarker> {
                override fun createMarkerRenderer(
                    strategy: MarkerRenderingStrategyInterface<OpenMobileMapsActualMarker>,
                ): MarkerOverlayRendererInterface<OpenMobileMapsActualMarker> =
                    mapController.createMarkerRenderer(strategy)

                override fun createMarkerEventController(
                    controller: StrategyMarkerController<OpenMobileMapsActualMarker>,
                    renderer: MarkerOverlayRendererInterface<OpenMobileMapsActualMarker>,
                ): MarkerEventControllerInterface<OpenMobileMapsActualMarker> =
                    mapController.createMarkerEventController(controller, renderer)

                override fun registerMarkerEventController(
                    controller: MarkerEventControllerInterface<OpenMobileMapsActualMarker>,
                ) {
                    mapController.registerMarkerEventController(controller)
                }

                override fun onMarkerRenderingReady() {
                    mapController.sendInitialCameraUpdate()
                }
            },
        )
    }

    return mapController
}

/** タイルのディスクキャッシュ。他プロバイダの既定と揃えてある。 */
private const val CACHE_BYTES: Long = 50L * 1024 * 1024
private const val USER_AGENT = "MapConductor"
