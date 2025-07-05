package com.mapconductor.mapbox

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
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
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerRenderer
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
    private val markerRenderer: MarkerRenderer<Feature> = MapboxMarkerRenderer(holder),
) : BaseMapViewController<CameraState, Feature>(),
    IMapboxMapViewController,
    CameraChangedCallback,
    OnMapClickListener,
    OnMapLongClickListener,
    OnMoveListener {
    private var isMarkerExpressionApplied = false // ★★★ このフラグを追加 ★★★

    private val lineLayer: LineLayer
    private val lineSourceId = "lines-source"
    private val lineLayerId = "lines-layer"
    private val lineSource: GeoJsonSource =
        geoJsonSource(lineSourceId) {
            geometry(LineString.fromLngLats(emptyList()))
        }
    private val markerLayer =
        MarkerLayer(
            sourceId = "markers-source",
            layerId = "markers-layer",
        )
    private val dragLayer =
        MarkerDragLayer(
            sourceId = "marker-drag-source",
            layerId = "marker-drag-layer",
        )

    override val markerOverlayManager: MarkerOverlayManager<Feature> by lazy {
        return@lazy overlayManagerFactory.create(
            hexGeocell = hexGeocell,
            onIconAdd = markerRenderer::addIcons,
            onIconRemove = markerRenderer::removeIcons,
            onIconChange = markerRenderer::changeIcons,
            onPostProcess = this::drawMarkerLayer,
            onAnimate = { entity ->
                when (entity.state.animation) {
                    MarkerAnimation.Drop -> animateMarkerDrop(entity)
                    MarkerAnimation.Bounce -> animateMarkerBounce(entity)
                    else -> throw IllegalArgumentException("No animation is available: ${entity.state.animation}")
                }
            }
        )
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
        holder.map.getStyle { style ->
            markerRenderer.init(markerOverlayManager.markerManager)
            style.addSource(markerLayer.source)
            style.addLayer(markerLayer.layer)
            style.addSource(dragLayer.source)
            style.addLayer(dragLayer.layer)

            style.addSource(lineSource)
            style.addLayer(lineLayer)
        }


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

    private fun setupListeners() {
        holder.map.subscribeCameraChanged(this)
        holder.map.removeOnMapClickListener(this)
        holder.map.addOnMapClickListener(this)

        holder.map.removeOnMapLongClickListener(this)
        holder.map.addOnMapLongClickListener(this)

        holder.map.removeOnMoveListener(this)
        holder.map.addOnMoveListener(this)
    }

    private fun drawMarkerLayer() {
        val entities = markerOverlayManager.markerManager.allEntities()
        coroutine.launch {
            markerLayer.draw(entities)
        }
    }

    private fun drawDragLayer() {
        coroutine.launch {
            dragLayer.draw()
        }
    }

    override suspend fun addMarkers(markerList: List<MarkerState>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun clearOverlays() = markerOverlayManager.clearOverlays()

    override suspend fun updateMarker(state: MarkerState) = markerOverlayManager.updateMarker(state)

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        val pixel =
            holder.map.pixelForCoordinate(
                coordinate = GeoPoint.from(position).toPoint(),
            )
        return Offset(
            x = pixel.x.toFloat(),
            y = pixel.y.toFloat(),
        )
    }

    fun fromScreenOffset(coordinate: ScreenCoordinate): GeoPoint? =
        holder.map.coordinateForPixel(coordinate).toGeoPoint()

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? =
        fromScreenOffset(
            ScreenCoordinate(
                offset.x.toDouble(),
                offset.y.toDouble(),
            ),
        )

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

    override fun onMapClick(point: Point): Boolean {
        val geoPoint = point.toGeoPoint()
        val entity =
            this.findNearestMarker(
                position = geoPoint,
                tolerance = Settings.Default.tapTolerance,
            )
        if (entity != null) {
            markerClickListener?.invoke(entity.state)
            return true
        }

        mapClickListener?.invoke(geoPoint)
        return true
    }

    private fun findNearestMarker(
        position: IGeoPoint,
        tolerance: Dp,
    ): MarkerEntity<Feature>? {
        val zoom = holder.map.cameraState.zoom
        val acceptDPI = tolerance.value * holder.mapView.context.resources.displayMetrics.density

//        clearPolyline()
//
//        // 検索範囲の詳細分析
//        val searchAnalysis = analyzeSearchRange(position, zoom, acceptDPI.toDouble())
//
//        // 可視化レイヤーを選択
//        drawSearchOutline(searchAnalysis)

        return findMarkerFromPoint(
            position = position,
            zoom = zoom,
            tolerance = acceptDPI.toDouble(),
        )
    }

    override fun setMarkerPosition(
        markerEntity: MarkerEntity<Feature>,
        position: GeoPoint,
    ) {
        val entities = markerOverlayManager.markerManager.allEntities()
        val feature =
            Feature.fromGeometry(
                position.toPoint(),
                markerEntity.marker.properties(),
            )
        markerEntity.marker = feature
        val features =
            entities.map {
                if (it.state.id == markerEntity.state.id) {
                    feature
                } else {
                    it.marker
                }
            }
        coroutine.launch {
            markerLayer.source.featureCollection(
                FeatureCollection.fromFeatures(features),
            )
        }
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

    override fun onMapLongClick(point: Point): Boolean {
        val geoPoint = point.toGeoPoint()
        val entity =
            this.findNearestMarker(
                position = geoPoint,
                tolerance = Settings.Default.tapTolerance,
            )
        if (entity != null) {
            setDraggingState(entity.state, true) // Suppress the recomposition for the position property
            markerOverlayManager.markerManager.removeEntity(entity.state.id)
            dragLayer.selected = entity
            dragLayer.updatePosition(geoPoint)
            drawMarkerLayer()
            drawDragLayer()

            markerDragStartListener?.invoke(entity.state)
            return true
        }

        mapClickListener?.invoke(geoPoint)
        return true
    }

    override fun onMove(detector: MoveGestureDetector): Boolean {
        dragLayer.selected?.let { entity ->
            val screenCoordinate =
                ScreenCoordinate(
                    detector.focalPoint.x.toDouble(),
                    detector.focalPoint.y.toDouble(),
                )
            fromScreenOffset(screenCoordinate)?.let {
                entity.state.position = it
                dragLayer.updatePosition(it)
                drawDragLayer()
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
            drawDragLayer()
            setDraggingState(entity.state, false) // Restore the recomposition for the position property
            markerOverlayManager.markerManager.registerEntity(entity)
            drawMarkerLayer()
            markerDragEndListener?.invoke(entity.state)
        }
    }
}
