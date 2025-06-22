package com.mapconductor.mapbox

import androidx.compose.ui.geometry.Offset
import com.google.gson.JsonObject
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraChanged
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.CameraState
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.Annotation
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationLongClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapconductor.core.MarkerManager
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.controller.MarkerOverlayManagerImpl
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.WebMercator
import android.animation.Animator
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    OnPointAnnotationClickListener,
    OnMapClickListener,
    OnPointAnnotationDragListener,
    OnPointAnnotationLongClickListener {
    private val pointAnnotationManager: PointAnnotationManager

    private var selectedMarker: MarkerState? = null

    init {
        val annotationApi = holder.mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        setupListeners()
    }

    private fun setupListeners() {
        holder.map.subscribeCameraChanged(this)
        holder.map.removeOnMapClickListener(this)
        holder.map.addOnMapClickListener(this)
        pointAnnotationManager.addClickListener(this@MapboxMapViewController)
        pointAnnotationManager.dragListeners.remove(this)
        pointAnnotationManager.dragListeners.add(this)
        pointAnnotationManager.longClickListeners.remove(this)
        pointAnnotationManager.longClickListeners.add(this)
    }

    override val markerOverlayManager =
        MarkerOverlayManagerImpl<PointAnnotation>(
            markerManager = MarkerManager(HexGeocell(WebMercator)),
            onRemove = { removes ->
                withContext(coroutine.coroutineContext) {
                    synchronized(pointAnnotationManager) {
                        val annotations: List<PointAnnotation> = removes.map { params -> params.marker }
                        pointAnnotationManager.delete(annotations)
                    }
                }
            },
            onAdd = { newMarkers ->
                withContext(coroutine.coroutineContext) {
                    synchronized(pointAnnotationManager) {
                        val options = newMarkers.map { params ->
                            return@map params.icon
                                .toPointAnnotationOptions()
                                .withPoint(
                                    GeoPoint.from(params.state.position).toPoint(),
                                ).withData(
                                    JsonObject().apply {
                                        addProperty("id", params.state.id)
                                    },
                                ) // .withDraggable(true)
                            }

                        pointAnnotationManager.create(options)
                    }
                }
            },
            onChange = { changes ->
                withContext(coroutine.coroutineContext) {
                    // Mapboxはマーカーの画像が変更された場合、作り直す必要がある
                    synchronized(pointAnnotationManager) {
                        // 古いマーカーを削除
                        val oldMarkers = changes.map { params -> params.marker }
                        pointAnnotationManager.delete(oldMarkers)

                        val newMarkerOptions = changes.map { params ->
                            params.icon
                                .toPointAnnotationOptions()
                                .withPoint(
                                    GeoPoint.from(params.state.position).toPoint(),
                                ).withData(
                                    JsonObject().apply {
                                        addProperty("id", params.state.id)
                                    },
                                )
                        }
                        // 新しいマーカーのインスタンスを返す
                        pointAnnotationManager.create(newMarkerOptions)
                    }
                }
            },
            onIconChange = { marker, icon ->
                withContext(coroutine.coroutineContext) {
                    synchronized(pointAnnotationManager) {
                        val option = icon.toPointAnnotationOptions()
                        marker.iconImageBitmap = icon.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                        marker.iconSize = option.iconSize
                        marker.iconImage = option.iconImage
                        marker.iconAnchor = option.iconAnchor
                        marker.iconOffset = option.iconOffset
                        pointAnnotationManager.update(marker)
                    }
                }
            },
            onAnimation = { param ->

            },
        )

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
            coroutine.let {
                it(cameraChanged.cameraState)
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

    override fun onAnnotationClick(annotation: PointAnnotation): Boolean {
        val tag = annotation.getData() ?: return false
        val key = tag.asJsonObject.get("id")?.asString ?: return false
        val state = markerOverlayManager.getMarkerState(key) ?: return false
        markerClickListener?.let {
            coroutine.launch {
                it(state)
            }
        }
        return true
    }

    override fun onMapClick(point: Point): Boolean {
        mapClickListener?.let {
            coroutine.let {
                it(point.toGeoPoint())
            }
        }
        return true
    }

    private fun annotationToMarkerState(annotation: Annotation<*>): MarkerState? {
        val tag = annotation.getData() ?: return null
        val id = tag.asJsonObject.get("id")?.asString ?: return null
        return when {
            annotation is PointAnnotation -> markerOverlayManager.getMarkerState(id)
            else -> {
                // Do nothing here
                null
            }
        }
    }

    override fun onAnnotationDrag(annotation: Annotation<*>) {
        (annotation as PointAnnotation).also { point ->
            this.annotationToMarkerState(annotation)?.also { state ->
                state.position = point.geometry.toGeoPoint()
                markerDragListener?.also {
                    coroutine.launch { it.invoke(state) }
                }
            }
        }
    }

    override fun onAnnotationDragFinished(annotation: Annotation<*>) {
        (annotation as PointAnnotation).also { point ->
            this.annotationToMarkerState(annotation)?.also { state ->
                state.position = point.geometry.toGeoPoint()

                // Restore the recomposition for the position property
                setDraggingState(state, false)
                point.isDraggable = false

                markerDragEndListener?.also {
                    coroutine.launch { it.invoke(state) }
                }
            }
        }
    }

    override fun onAnnotationDragStarted(annotation: Annotation<*>) {
        (annotation as PointAnnotation).also { point ->
            this.annotationToMarkerState(annotation)?.also { state ->
                // Suppress the recomposition for the position property
                setDraggingState(state, true)

                state.position = point.geometry.toGeoPoint()
                markerDragStartListener?.also {
                    coroutine.launch { it.invoke(state) }
                }
            }
        }
    }

    override fun onAnnotationLongClick(annotation: PointAnnotation): Boolean {
        selectedMarker = this.annotationToMarkerState(annotation)
        if (selectedMarker == null) return false

        annotation.isDraggable = true
        return true
    }
}
