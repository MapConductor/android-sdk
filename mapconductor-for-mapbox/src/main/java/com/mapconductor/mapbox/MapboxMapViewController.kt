package com.mapconductor.mapbox

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.JsonObject
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraChanged
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
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
import com.mapconductor.core.ResourceProvider.dpToPx
import com.mapconductor.core.circle.CircleEntityImpl
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.circle.CircleState
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
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineOverlayManagerImpl
import com.mapconductor.core.polyline.PolylineRenderer
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.mapbox.circle.CircleLayerWrapper
import com.mapconductor.mapbox.marker.DefaultMapboxMarkerRenderer
import com.mapconductor.mapbox.marker.MapboxMarkerRenderer
import com.mapconductor.mapbox.marker.MarkerDragLayer
import com.mapconductor.mapbox.marker.MarkerLayer
import com.mapconductor.mapbox.polyline.MapboxPolylineRenderer
import com.mapconductor.settings.Settings
import kotlin.math.cos
import kotlin.math.pow
import android.animation.Animator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import com.mapconductor.mapbox.polyline.DefaultMapboxPolylineRenderer

interface IMapboxMapViewController : MapViewController<Feature, Feature, Feature> {
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
    private val markerRendererFactory: MarkerRendererFactory<Feature> = DefaultMapboxMarkerRenderer(),
    private val polylineRendererFactory: PolylineRendererFactory<Feature> = DefaultMapboxPolylineRenderer(),
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
    private val circleLayerWrapper: CircleLayerWrapper =
        CircleLayerWrapper(
            sourceId = "circle-source",
            layerId = "circle-layer",
        ),
    override val circleManager: CircleManager<Feature> = CircleManager(),
) : BaseMapViewController<CameraState, Feature, Feature, Feature>(),
    IMapboxMapViewController,
    CameraChangedCallback,
    OnMapClickListener,
    OnMapLongClickListener,
    OnMoveListener {
    val semaphore = Semaphore(1)
    override val markerRenderer: MapboxMarkerRenderer =
        MapboxMarkerRenderer(
            holder = holder,
            coroutine = coroutine,
            markerLayer = markerLayer,
            dragLayer = dragLayer,
        )

    override fun createMarkerOverlayManager(): MarkerOverlayManager<Feature> =
        markerRendererFactory.create(
            hexGeocell = hexGeocell,
            onIconAdd = markerRenderer::addIcons,
            onIconRemove = markerRenderer::removeIcons,
            onIconChange = markerRenderer::changeIcons,
            onPostProcess = markerRenderer::drawMarkerLayer,
            onAnimate = markerRenderer::animate,
        )

    override fun createPolylineOverlayManager(): PolylineOverlayManager<Feature> =
        polylineRendererFactory.create(
            onAdd = polylineRenderer::addLines,
            onChange = polylineRenderer::changeLine,
            onRemove = polylineRenderer::removeLines,
        )

    private val lineLayer: LineLayer
    private val lineSourceId = "lines-source"
    private val lineLayerId = "lines-layer"
    private val lineSource: GeoJsonSource =
        geoJsonSource(lineSourceId) {
            geometry(LineString.fromLngLats(emptyList()))
        }

    override fun onMarkerOverlayManagerInitialized(overlayManager: MarkerOverlayManager<Feature>) {
        holder.map.getStyle { style ->
            style.addSource(circleLayerWrapper.source)
            style.addLayer(circleLayerWrapper.layer)

            style.addSource(lineSource)
            style.addLayer(lineLayer)

            style.addSource(markerLayer.source)
            style.addLayer(markerLayer.layer)
            style.addSource(dragLayer.source)
            style.addLayer(dragLayer.layer)
        }
    }

    override val polylineRenderer: PolylineRenderer<Feature> =
        MapboxPolylineRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    init {
        lineLayer =
            lineLayer(lineLayerId, lineSourceId) {
                lineColor(Color.Red.toArgb())
                lineWidth(4.0)
                lineJoin(LineJoin.ROUND)
                lineCap(LineCap.ROUND)
            }

        setupListeners()
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

    override suspend fun clearOverlays() {
        markerOverlayManager.clearOverlays()
        polylineOverlayManager.clearOverlays()
    }

    override suspend fun addMarkers(markerList: List<MarkerState>) = markerOverlayManager.addMarkers(markerList)
    override suspend fun updateMarker(state: MarkerState) = markerOverlayManager.updateMarker(state)

    override suspend fun addPolylines(data: List<PolylineState>) {
        TODO("Not yet implemented")
    }

    override suspend fun updatePolyline(state: PolylineState) {
        TODO("Not yet implemented")
    }

    private fun createCircleFeature(state: CircleState): Feature {
        val feature = Feature.fromGeometry(
            GeoPoint.from(state.center).toPoint(),
            JsonObject().apply {
                addProperty(
                    CircleLayerWrapper.Prop.RADIUS,
                    meterToPixel(
                        meter = state.radius,
                        latitude = state.center.latitude,
                        zoom = holder.map.cameraState.zoom,
                    ))
                addProperty(CircleLayerWrapper.Prop.FILL_COLOR, state.fillColor.toMapboxColorString())
                addProperty(CircleLayerWrapper.Prop.STROKE_COLOR, state.strokeColor.toMapboxColorString())
                addProperty(CircleLayerWrapper.Prop.STROKE_WIDTH, dpToPx(state.strokeWidth))
            }
        )
        return feature
    }

    override suspend fun addCircles(data: List<CircleState>) {
        semaphore.acquire()
        data.forEach { state ->
            val feature = createCircleFeature(state)
            val entity = CircleEntityImpl(
                circle = feature,
                state = state,
            )

            circleManager.registerEntity(entity)
        }

        coroutine.launch {
            val entities = circleManager.allEntities()
            circleLayerWrapper.draw(entities)
        }

        semaphore.release()
    }

    fun meterToPixel(meter: Double, latitude: Double, zoom: Double): Double {
        val earthCircumference = 2 * Math.PI * 6378137
        val tileSize = 512.0 // Mapbox v10+はデフォルト512
        val metersPerPixel = cos(Math.toRadians(latitude)) * earthCircumference / (tileSize * 2.0.pow(zoom))
        return meter / metersPerPixel
    }

    override suspend fun updateCircle(state: CircleState) {
        val prevEntity = circleManager.getEntity(state.id) ?: return
        val currentFinger = state.fingerPrint()
        val prevFinger = prevEntity.fingerPrint
        if (currentFinger == prevFinger) {
            return
        }

        semaphore.acquire()
        val feature = createCircleFeature(state)
        val entity = CircleEntityImpl(
            circle = feature,
            state = state,
        )

        circleManager.updateEntity(entity)

        val entities = circleManager.allEntities()

        circleLayerWrapper.draw(entities)
        semaphore.release()
    }

    suspend fun redrawCircles() {
        semaphore.acquire()
        val entities = circleManager.allEntities()
        val updatedEntities = entities.map { entity ->
            val feature = createCircleFeature(entity.state)
            val updatedEntity = CircleEntityImpl(
                circle = feature,
                state = entity.state,
            )

            circleManager.updateEntity(updatedEntity)
            updatedEntity
        }

        circleLayerWrapper.draw(updatedEntities)
        semaphore.release()
    }

    override fun run(cameraChanged: CameraChanged) {
        coroutine.launch {
            redrawCircles()
        }
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
        val targetCamera = dstPosition.toCameraOptions()
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
                tolerance =
                    Settings.Default.tapTolerance.value
                        .toDouble() * ResourceProvider.getDensity(),
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
                tolerance =
                    Settings.Default.tapTolerance.value
                        .toDouble() * ResourceProvider.getDensity(),
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
