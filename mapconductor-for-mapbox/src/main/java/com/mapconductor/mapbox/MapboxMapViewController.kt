package com.mapconductor.mapbox

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraChanged
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.CameraState
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.extension.style.expressions.dsl.generated.zoom
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
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
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.icons.Default
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayManagerImpl
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.settings.Settings
import kotlin.Result
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

interface IMapboxMapViewController : MapViewController {
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
) : BaseMapViewController<CameraState>(),
    IMapboxMapViewController,
    CameraChangedCallback,
    OnMapClickListener {
    private val symbolLayer: SymbolLayer
    private val lineLayer: LineLayer
    private val lineSourceId = "lines-source"
    private val lineLayerId = "lines-layer"
    private val lineSource: GeoJsonSource = geoJsonSource(lineSourceId) {
        geometry(LineString.fromLngLats(emptyList()))
    }
    private val markerSourceId = "markers-source"
    private val markerLayerId = "markers-layer"
    private val markerSource: GeoJsonSource = geoJsonSource(markerSourceId) {
        featureCollection(FeatureCollection.fromFeatures(emptyList()))
    }
    private var selectedMarker: MarkerState? = null
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
        lineLayer = lineLayer(lineLayerId, lineSourceId) {
            lineColor(Color.RED)
            lineWidth(4.0)
            lineJoin(LineJoin.ROUND)
            lineCap(LineCap.ROUND)
        }

        symbolLayer = symbolLayer(markerLayerId, markerSourceId) {
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
                        Expression.get(Prop.OFFSET_Y).literalValue as? Double ?: 0.0
                    )
                )
                iconTranslateAnchor(IconTranslateAnchor.MAP)
            }
        }

        holder.map.getStyle { style ->
            defaultIcon = markerOverlayManager.markerManager.createBitmapIcon(MarkerIcon.Default())
            style.addImage(Prop.DEFAULT_MARKER_ID, defaultIcon.bitmap)
            style.addSource(markerSource)
            style.addLayer(symbolLayer)

            style.addSource(lineSource)
            style.addLayer(lineLayer)
        }

        setupListeners()
    }

    private fun setupListeners() {
        holder.map.subscribeCameraChanged(this)
        holder.map.removeOnMapClickListener(this)
        holder.map.addOnMapClickListener(this)
    }

    // Web Mercator投影法に適したbaseHexSideLengthを使用
    private val hexCell = HexGeocell(
        projection = WebMercator,
        baseHexSideLength = 100000  // 100km - 中ズームレベルに適した値
    )

    override val markerOverlayManager =
        MarkerOverlayManagerImpl<Feature>(
            markerManager = MarkerManager(hexCell),
            onRemove = { removes ->
                val style = suspendCoroutine { it->
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

                val style = suspendCoroutine { it->
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

                val features = newMarkers.map { params ->
                    Feature.fromGeometry(
                        Point.fromLngLat(params.first.position.longitude, params.first.position.latitude),
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
                        }
                    )
                }
                coroutine.launch { updateMarkerLayer(features) }
                features
            },
            onChange = { TODO() },
        )

    private fun updateMarkerLayer(features: List<Feature>) {
        markerSource.featureCollection(
            FeatureCollection.fromFeatures(features)
        )
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

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? =
        holder.map
            .coordinateForPixel(
                ScreenCoordinate(
                    offset.x.toDouble(),
                    offset.y.toDouble(),
                ),
            ).toGeoPoint()

    override fun run(cameraChanged: CameraChanged) {
        cameraMoveListener?.let {
            coroutine.launch {
                it(CameraState(
                    cameraChanged.cameraState.center,
                    cameraChanged.cameraState.padding,
                    cameraChanged.cameraState.zoom + 1,
                    cameraChanged.cameraState.bearing,
                    cameraChanged.cameraState.pitch
                ))
            }
        }
    }

    override fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        coroutine.launch {
            holder.map.setCamera(dstPosition.toCameraOptions())
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


    override fun onMapClick(point: Point): Boolean {
        val geoPoint = point.toGeoPoint()
        val state = this.findNearestMarker(
            position = geoPoint,
            tolerance = Settings.Default.tapTolerance,
        )
        if (state != null) {
            markerClickListener?.let {
                coroutine.launch {
                    it(state)
                }
            }
            return true
        }

        mapClickListener?.let {
            coroutine.launch {
                it(geoPoint)
            }
        }
        return true
    }

    private fun findNearestMarker(
        position: IGeoPoint,
        tolerance: Dp,
    ): MarkerState? {
        val zoom = holder.map.cameraState.zoom
        val acceptDPI = tolerance.value.toFloat() * 2 * holder.mapView.context.resources.displayMetrics.density

        // 現在のzoomレベルに適したHexGeocellを使用
        val currentHexCell = createHexGeocell(zoom)

        clearPolyline()
        markerOverlayManager.markerManager.findNearestCell(position)?.let { cell ->
            val points = currentHexCell.hexToPolygonLatLng(
                coord = cell.coord,
                latHint = position.latitude,  // 修正済み: 正しい緯度を使用
                zoom = zoom,
            )
            drawPolyline(points)
        }

        return findMarkerFromPoint(
            markerOverlayManager = markerOverlayManager,
            position = position,
            zoom = zoom,
            tolerance = acceptDPI.toDouble(),
        )
    }

    // zoom レベルに応じて適切なbaseHexSideLengthを選択
    private fun createHexGeocell(zoom: Double): HexGeocell {
        val baseHexSideLength = when {
            zoom <= 8 -> 500000    // 500km - 低ズーム用
            zoom <= 14 -> 100000   // 100km - 中ズーム用
            else -> 20000          // 20km - 高ズーム用
        }
        return HexGeocell(WebMercator, baseHexSideLength)
    }

    private fun clearPolyline() {
        lineSource.geometry(LineString.fromLngLats(emptyList()))
    }

    private fun drawPolyline(geoPoints: List<IGeoPoint>) {
        val points = geoPoints.map {
            GeoPoint.from(it).toPoint()
        }
        lineSource.geometry(LineString.fromLngLats(points))
    }

//    private fun annotationToMarkerState(annotation: Annotation<*>): MarkerState? {
//        val tag = annotation.getData() ?: return null
//        val id = tag.asJsonObject.get("id")?.asString ?: return null
//        return when {
//            annotation is PointAnnotation -> markerOverlayManager.getMarkerState(id)
//            else -> {
//                // Do nothing here
//                null
//            }
//        }
//    }

//    override fun onAnnotationDrag(annotation: Annotation<*>) {
//        (annotation as PointAnnotation).also { point ->
//            this.annotationToMarkerState(annotation)?.also { state ->
//                state.position = point.geometry.toGeoPoint()
//                markerDragListener?.also {
//                    coroutine.launch { it.invoke(state) }
//                }
//            }
//        }
//    }
//
//    override fun onAnnotationDragFinished(annotation: Annotation<*>) {
//        (annotation as PointAnnotation).also { point ->
//            this.annotationToMarkerState(annotation)?.also { state ->
//                state.position = point.geometry.toGeoPoint()
//
//                // Restore the recomposition for the position property
//                setDraggingState(state, false)
//                point.isDraggable = false
//
//                markerDragEndListener?.also {
//                    coroutine.launch { it.invoke(state) }
//                }
//            }
//        }
//    }
//
//    override fun onAnnotationDragStarted(annotation: Annotation<*>) {
//        (annotation as PointAnnotation).also { point ->
//            this.annotationToMarkerState(annotation)?.also { state ->
//                // Suppress the recomposition for the position property
//                setDraggingState(state, true)
//
//                state.position = point.geometry.toGeoPoint()
//                markerDragStartListener?.also {
//                    coroutine.launch { it.invoke(state) }
//                }
//            }
//        }
//    }
//
//    override fun onAnnotationLongClick(annotation: PointAnnotation): Boolean {
//        selectedMarker = this.annotationToMarkerState(annotation)
//        if (selectedMarker == null) return false
//
//        annotation.isDraggable = true
//        return true
//    }
}
