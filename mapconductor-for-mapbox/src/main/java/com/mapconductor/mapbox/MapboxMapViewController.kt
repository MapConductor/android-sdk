package com.mapconductor.mapbox

import androidx.compose.ui.geometry.Offset
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraChanged
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMoveListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.removeOnMoveListener
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerRendererFactory
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.mapbox.marker.DefaultMapboxMarkerRenderer
import com.mapconductor.mapbox.marker.MapboxMarkerRenderer
import com.mapconductor.settings.Settings
import android.animation.Animator
import android.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// interface IMapboxMapInitOptions {
//    val mapOptions: MapOptions?
//    val plugins: List<Plugin>?
//    val cameraOptions: CameraOptions?
//    val textureView: Boolean?
//    val styleUri: String?
//    val attrs: AttributeSet?
//    val antialiasingSampleCount: Int?
// }
// data class MapboxMapInitOptions(
//    override val mapOptions: MapOptions? = null,
//    override val plugins: List<Plugin>? = null,
//    override val cameraOptions: CameraOptions? = null,
//    override val textureView: Boolean? = null,
//    override val styleUri: String? = null,
//    override val attrs: AttributeSet? = null,
//    override val antialiasingSampleCount: Int? = null,
// ) : IMapboxMapInitOptions

interface IMapboxMapViewController : MapViewController<Feature> {
    fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback? = null,
    )

    fun animateCamera(
        dstPosition: MapCameraPosition,
        duration: Long,
        listener: MapViewState.MoveCameraCallback? = null,
    )
}

internal class MapboxMapViewController(
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    // Web Mercator投影法に適したbaseHexSideLengthを使用
    override val hexGeocell: HexGeocell =
        HexGeocell(
            projection = WebMercator,
            baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
        ),
    private val overlayManagerFactory: MarkerRendererFactory<Feature> = DefaultMapboxMarkerRenderer(),

    private val markerLayer: MarkerLayer =
        MarkerLayer(
            sourceId = "markers-source",
            layerId = "markers-layer",
        ),
    private val dragLayer: MarkerDragLayer =
        MarkerDragLayer(
            sourceId = "marker-drag-source",
            layerId = "marker-drag-layer",
        ),
) : BaseMapViewController<CameraState, Feature>(),
    IMapboxMapViewController,
    CameraChangedCallback,
    OnMapClickListener,
    OnMapLongClickListener,
    OnMoveListener {

    override val markerRenderer: MapboxMarkerRenderer = MapboxMarkerRenderer(
        holder = holder,
        coroutine = coroutine,
        markerLayer = markerLayer,
        dragLayer = dragLayer,
    )

    override fun createMarkerOverlayManager(): MarkerOverlayManager<Feature> {
        return overlayManagerFactory.create(
            hexGeocell = hexGeocell,
            onIconAdd = markerRenderer::addIcons,
            onIconRemove = markerRenderer::removeIcons,
            onIconChange = markerRenderer::changeIcons,
            onPostProcess = markerRenderer::drawMarkerLayer,
            onAnimate = markerRenderer::animate,
        )
    }
    private val lineLayer: LineLayer
    private val lineSourceId = "lines-source"
    private val lineLayerId = "lines-layer"
    private val lineSource: GeoJsonSource =
        geoJsonSource(lineSourceId) {
            geometry(LineString.fromLngLats(emptyList()))
        }

    override fun onMarkerOverlayManagerInitialized(overlayManager: MarkerOverlayManager<Feature>) {
        holder.map.getStyle { style ->
            style.addSource(markerLayer.source)
            style.addLayer(markerLayer.layer)
            style.addSource(dragLayer.source)
            style.addLayer(dragLayer.layer)

            style.addSource(lineSource)
            style.addLayer(lineLayer)
        }
    }

    init {
        lineLayer =
            lineLayer(lineLayerId, lineSourceId) {
                lineColor(Color.RED)
                lineWidth(4.0)
                lineJoin(LineJoin.ROUND)
                lineCap(LineCap.ROUND)
            }

        setupSymbolLayer(markerLayer.layer)
        setupSymbolLayer(dragLayer.layer)

        setupListeners()
    }

    private fun setupSymbolLayer(layer: SymbolLayer) {
        layer.apply {
            iconSize(1.0)
            iconImage(Expression.get(MapboxMarkerRenderer.Prop.ICON_ID))
            iconAllowOverlap(true)
            iconIgnorePlacement(true)
            iconAnchor(IconAnchor.BOTTOM)
        }
    }

    override fun setupListeners() {
        holder.map.subscribeCameraChanged(this)
        holder.map.removeOnMapClickListener(this)
        holder.map.addOnMapClickListener(this)

        holder.map.removeOnMapLongClickListener(this)
        holder.map.addOnMapLongClickListener(this)

        holder.map.removeOnMoveListener(this)
        holder.map.addOnMoveListener(this)
    }


    override suspend fun addMarkers(markerList: List<MarkerState>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun clearOverlays() = markerOverlayManager.clearOverlays()

    override suspend fun updateMarker(state: MarkerState) = markerOverlayManager.updateMarker(state)

    override fun run(cameraChanged: CameraChanged) {
        cameraMoveListener?.invoke(
            CameraState(
                cameraChanged.cameraState.center,
                cameraChanged.cameraState.padding,
                cameraChanged.cameraState.zoom + 1,
                cameraChanged.cameraState.bearing,
                cameraChanged.cameraState.pitch,
            ),
        )
    }

    override fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val cameraOptions =
            CameraOptions
                .Builder()
                .center(dstPosition.position.toPoint())
                .zoom(dstPosition.zoom - 1)
                .pitch(dstPosition.tilt)
                .bearing(dstPosition.bearing)
                .build()

        coroutine.launch {
            holder.map.setCamera(cameraOptions)
        }
        listener?.onComplete(true)
    }

    override fun animateCamera(
        dstPosition: MapCameraPosition,
        durationMs: Long,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val targetCamera =
            CameraOptions
                .Builder()
                .center(dstPosition.position.toPoint())
                .zoom(dstPosition.zoom - 1)
                .pitch(dstPosition.tilt)
                .bearing(dstPosition.bearing)
                .build()

        val animationOptions =
            MapAnimationOptions
                .Builder()
                .duration(durationMs)
                .build()

        val animatorListener =
            object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    // Do nothing here
                }

                override fun onAnimationEnd(animation: Animator) {
                    listener?.onComplete(true)
                }

                override fun onAnimationCancel(animation: Animator) {
                    listener?.onComplete(false)
                }

                override fun onAnimationRepeat(animation: Animator) {
                    // Do nothing here
                }
            }

        coroutine.launch {
            holder.map.flyTo(
                cameraOptions = targetCamera,
                animationOptions = animationOptions,
                animatorListener = animatorListener,
            )
        }
    }

    override fun onMapLongClick(point: Point): Boolean {
        val geoPoint = point.toGeoPoint()
        val entity =
            this.markerRenderer.findNearestMarker(
                position = geoPoint,
                tolerance = Settings.Default.tapTolerance.value.toDouble() * ResourceProvider.density,
                zoom = holder.map.cameraState.zoom,
            )
        if (entity != null) {
            markerRenderer.setDraggingState(entity.state, true) // Suppress the recomposition for the position property
            markerOverlayManager.markerManager.removeEntity(entity.state.id)
            dragLayer.selected = entity
            dragLayer.updatePosition(geoPoint)
            markerRenderer.drawMarkerLayer()
            markerRenderer.drawDragLayer()

            markerDragStartListener?.invoke(entity.state)
            return true
        }

        mapLongClickListener?.invoke(geoPoint)
        return true
    }

    override fun onMapClick(point: Point): Boolean {
        val geoPoint = point.toGeoPoint()
        val entity =
            this.markerRenderer.findNearestMarker(
                position = geoPoint,
                tolerance = Settings.Default.tapTolerance.value.toDouble() * ResourceProvider.density,
                zoom = holder.map.cameraState.zoom,
            )
        if (entity != null) {
            markerClickListener?.invoke(entity.state)
            return true
        }

        mapClickListener?.invoke(geoPoint)
        return true
    }


    override fun clearPolyline() {
        lineSource.geometry(LineString.fromLngLats(emptyList()))
    }

    override fun drawPolyline(geoPoints: List<IGeoPoint>) {
        val points =
            geoPoints.map {
                GeoPoint.from(it).toPoint()
            }
        lineSource.geometry(LineString.fromLngLats(points))
    }

    override fun onMove(detector: MoveGestureDetector): Boolean {
        dragLayer.selected?.let { entity ->

            val screenCoordinate =
                Offset(
                    detector.focalPoint.x,
                    detector.focalPoint.y,
                )

            holder.fromScreenOffsetSync(screenCoordinate)?.let {
                entity.state.position = it
                dragLayer.updatePosition(it)
                markerRenderer.drawDragLayer()
            }

            markerDragListener?.invoke(entity.state)
            return true
        }
        return false
    }

    override fun onMoveBegin(detector: MoveGestureDetector) {
        // Do nothing here
    }

    override fun onMoveEnd(detector: MoveGestureDetector) {
        dragLayer.selected?.let { entity ->
            val screenCoordinate =
                ScreenCoordinate(
                    detector.focalPoint.x.toDouble(),
                    detector.focalPoint.y.toDouble(),
                )
            val point = holder.map.coordinateForPixel(screenCoordinate)
            dragLayer.updatePosition(point.toGeoPoint())
            dragLayer.selected = null
            markerRenderer.drawDragLayer()
            markerRenderer.setDraggingState(entity.state, false) // Restore the recomposition for the position property
            markerOverlayManager.markerManager.registerEntity(entity)
            markerRenderer.drawMarkerLayer()
            markerDragEndListener?.invoke(entity.state)
        }
    }
}
