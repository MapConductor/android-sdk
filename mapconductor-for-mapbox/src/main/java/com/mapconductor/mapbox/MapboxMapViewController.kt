package com.mapconductor.mapbox

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import com.google.gson.JsonObject
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
import com.mapbox.maps.extension.style.layers.properties.generated.IconTranslateAnchor
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
import com.mapconductor.core.icons.Default
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayManagerImpl
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.settings.Settings
import kotlin.coroutines.suspendCoroutine
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
    override val hexCell: HexGeocell =
        HexGeocell(
            projection = WebMercator,
            baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
        ),
) : BaseMapViewController<CameraState, Feature>(),
    IMapboxMapViewController,
    CameraChangedCallback,
    OnMapClickListener,
    OnMapLongClickListener,
    OnMoveListener {
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

    private val loadedIconHash: MutableMap<String, Int> = mutableMapOf()
    private lateinit var defaultIcon: BitmapIcon

    private object Prop {
        const val MARKER_ID = "id"
        const val ICON_ID = "icon_id"
        const val DEFAULT_MARKER_ID = "default"
        const val OFFSET_X = "offset_x"
        const val OFFSET_Y = "offset_y"
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
            defaultIcon = markerOverlayManager.markerManager.createBitmapIcon(MarkerIcon.Default())
            style.addImage(Prop.DEFAULT_MARKER_ID, defaultIcon.bitmap)
            style.addSource(markerLayer.source)
            style.addLayer(markerLayer.layer)
            style.addSource(dragLayer.source)
            style.addLayer(dragLayer.layer)

//            style.addSource(lineSource)
//            style.addLayer(lineLayer)
        }

        setupListeners()
    }

    private fun setupSymbolLayer(layer: SymbolLayer) {
        layer.apply {
            iconSize(1.0)
            val iconId = Expression.get(Prop.ICON_ID)
            iconImage(iconId)
            iconAllowOverlap(true)
            iconIgnorePlacement(true)
            iconId.literalValue?.let {
                iconAnchor(IconAnchor.CENTER)
                iconTranslate(
                    listOf(
                        Expression.get(Prop.OFFSET_X).literalValue as? Double ?: 0.0,
                        Expression.get(Prop.OFFSET_Y).literalValue as? Double ?: 0.0,
                    ),
                )
                iconTranslateAnchor(IconTranslateAnchor.MAP)
            }
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

    override val markerOverlayManager =
        MarkerOverlayManagerImpl<Feature>(
            markerManager = MarkerManager(hexCell),
            onRemove = { removes ->
                val style =
                    suspendCoroutine { it ->
                        holder.map.getStyle { style ->
                            it.resumeWith(Result.success(style))
                        }
                    }
                removes.forEach { removeEntity ->
                    removeEntity.state.icon?.let {
                        val iconKey = it.hashCode().toString()
                        val cnt = loadedIconHash.getOrDefault(iconKey, 1) - 1
                        if (cnt == 0) {
                            loadedIconHash.remove(iconKey)
                            style.removeStyleImage(iconKey)
                        } else {
                            loadedIconHash.put(iconKey, cnt)
                        }
                    }
                }
            },
            onAdd = { newMarkers ->

                val style =
                    suspendCoroutine { it ->
                        holder.map.getStyle { style ->
                            it.resumeWith(Result.success(style))
                        }
                    }
                newMarkers.forEach { params ->
                    params.first.icon?.let {
                        val iconKey = it.hashCode().toString()
                        if (!loadedIconHash.contains(iconKey)) {
                            style.addImage(iconKey, params.second.bitmap)
                        }
                    }
                }

                newMarkers.map { params ->
                    Feature.fromGeometry(
                        params.first.position.toPoint(),
                        JsonObject().apply {
                            addProperty(Prop.MARKER_ID, params.first.id)
                            if (params.first.icon != null) {
                                params.first.icon?.let {
                                    val iconKey = it.hashCode().toString()
                                    loadedIconHash.put(iconKey, loadedIconHash.getOrDefault(iconKey, 0) + 1)
                                    addProperty(Prop.ICON_ID, iconKey)
                                    val offsetX = (it.anchor.x - 0.5) * it.size.width
                                    val offsetY = (it.anchor.y - 0.5) * it.size.height
                                    addProperty(Prop.OFFSET_X, offsetX)
                                    addProperty(Prop.OFFSET_Y, offsetY)
                                }
                            } else {
                                addProperty(Prop.ICON_ID, Prop.DEFAULT_MARKER_ID)
                                addProperty(Prop.OFFSET_X, 0.0)
                                addProperty(Prop.OFFSET_Y, defaultIcon.size.height.toDouble())
                            }
                        },
                    )
                }
            },
            onChange = { changes ->
                val style =
                    suspendCoroutine { it ->
                        holder.map.getStyle { style ->
                            it.resumeWith(Result.success(style))
                        }
                    }
                changes.map { params ->
                    val prevProperties = params.prevEntity.marker.properties()
                    val properties =
                        JsonObject().apply {
                            addProperty(Prop.MARKER_ID, params.entity.state.id)

                            if (params.entity.state.icon != params.prevEntity.state.icon) {
                                // Decrement reference counter for the previous icon
                                val iconKey =
                                    params.prevEntity.state.icon
                                        .hashCode()
                                        .toString()
                                val cnt = loadedIconHash.getOrDefault(iconKey, 1) - 1
                                if (cnt == 0) {
                                    loadedIconHash.remove(iconKey)
                                    style.removeStyleImage(iconKey)
                                } else {
                                    loadedIconHash.put(iconKey, cnt)
                                }

                                // Decrement reference counter for new icon
                                if (params.entity.state.icon != null) {
                                    params.entity.state.icon?.let {
                                        val iconKey = it.hashCode().toString()
                                        loadedIconHash.put(iconKey, loadedIconHash.getOrDefault(iconKey, 0) + 1)
                                        addProperty(Prop.ICON_ID, iconKey)
                                        val offsetX = (it.anchor.x - 0.5) * it.size.width
                                        val offsetY = (it.anchor.y - 0.5) * it.size.height
                                        addProperty(Prop.OFFSET_X, offsetX)
                                        addProperty(Prop.OFFSET_Y, offsetY)
                                    }
                                } else {
                                    addProperty(Prop.ICON_ID, Prop.DEFAULT_MARKER_ID)
                                    addProperty(Prop.OFFSET_X, 0.0)
                                    addProperty(Prop.OFFSET_Y, defaultIcon.size.height.toDouble())
                                }
                            } else {
                                addProperty(
                                    Prop.ICON_ID,
                                    prevProperties?.get(Prop.ICON_ID)?.asString ?: Prop.DEFAULT_MARKER_ID,
                                )
                                addProperty(Prop.OFFSET_X, 0.0)
                                addProperty(
                                    Prop.OFFSET_Y,
                                    prevProperties?.get(Prop.OFFSET_Y)?.asDouble ?: defaultIcon.size.height.toDouble(),
                                )
                            }
                        }
                    Feature.fromGeometry(
                        params.entity.state.position
                            .toPoint(),
                        properties,
                    )
                }
            },
            onPostProcess = {
                drawMarkerLayer()
            },
            onAnimate = {
                when (it.state.animation) {
                    MarkerAnimation.Drop -> this.animateMarkerDrop(it)
                    MarkerAnimation.Bounce -> this.animateMarkerBounce(it)
                    else -> throw IllegalArgumentException("No animation is available: ${it.state.animation}")
                }
            },
        )

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
