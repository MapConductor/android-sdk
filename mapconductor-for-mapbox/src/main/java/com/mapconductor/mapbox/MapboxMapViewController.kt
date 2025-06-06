package com.mapconductor.mapbox

import androidx.compose.ui.geometry.Offset
import com.google.gson.JsonObject
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraChanged
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.CameraState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.Annotation
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapconductor.core.MarkerManager
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.controller.MarkerOverlayManager
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapClickHandler
import com.mapconductor.core.marker.MarkerEntry
import com.mapconductor.core.projection.WebMercator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.animation.Animator

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
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val onCameraMove: (OnCameraMoveHandler<CameraState>)? = null,
    val onMapClick: OnMapClickHandler? = null,
) : IMapboxMapViewController,
    CameraChangedCallback,
    OnPointAnnotationClickListener,
    OnMapClickListener,
    OnPointAnnotationDragListener {
    private lateinit var pointAnnotationManager: PointAnnotationManager

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
    }

    private val markerOverlayManager =
        MarkerOverlayManager<PointAnnotation>(
            markerManager = MarkerManager(HexGeocell(WebMercator)),
            onRemove = { removes ->
                synchronized(pointAnnotationManager) {
                    val annotations: List<PointAnnotation> = removes.map { params -> params.marker }
                    pointAnnotationManager.delete(annotations)
                }
            },
            onAdd = { newMarkers ->
                synchronized(pointAnnotationManager) {
                    val options =
                        newMarkers.map { params ->
                            return@map params.icon
                                .toPointAnnotationOptions()
                                .withPoint(
                                    GeoPoint.from(params.entry.state.position).toPoint(),
                                ).withData(
                                    JsonObject().apply {
                                        addProperty("id", params.entry.id)
                                    },
                                ).withDraggable(true)
                        }

                    return@MarkerOverlayManager pointAnnotationManager.create(options)
                }
            },
            onChange = { changes ->
                synchronized(pointAnnotationManager) {
                    changes.forEach { params ->
                        // TODO: アイコンに変更があったかどうかを比較
                        val option =
                            params.icon
                                .toPointAnnotationOptions()
                                .withPoint(
                                    GeoPoint.from(params.entry.state.position).toPoint(),
                                )
                        params.marker.point = GeoPoint.from(params.entry.state.position).toPoint()
                        params.marker.iconSize = option.iconSize
                        params.marker.iconImage = option.iconImage
                        params.marker.iconAnchor = option.iconAnchor
                        params.marker.iconOffset = option.iconOffset
                    }
                }
            },
        )

    override suspend fun addMarkers(markerList: List<MarkerEntry>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun clearOverlays() = markerOverlayManager.clearOverlays()

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

    override fun run(cameraChanged: CameraChanged) {
        onCameraMove?.let {
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
        val stateWithHandler = markerOverlayManager.getMarkerEntry(key) ?: return false
        val handlers = stateWithHandler.handlers
        handlers.onClick?.let {
            coroutine.launch {
                it(stateWithHandler.state)
            }
        }
        return true
    }

    override fun onMapClick(point: Point): Boolean {
        onMapClick?.let {
            coroutine.let {
                it(point.toGeoPoint())
            }
        }
        return true
    }

    override fun onAnnotationDrag(annotation: Annotation<*>) {
        val tag = annotation.getData() ?: return
        val id = tag.asJsonObject.get("id")?.asString ?: return
        when {
            annotation is PointAnnotation -> onPointAnnotationDrag(id, annotation)
            else -> {
                // Do nothing here
            }
        }
    }

    fun onPointAnnotationDrag(
        id: String,
        annotation: PointAnnotation,
    ) {
        val entry = markerOverlayManager.getMarkerEntry(id) ?: return
        entry.state.position = annotation.point.toGeoPoint()
    }

    override fun onAnnotationDragFinished(annotation: Annotation<*>) {
        TODO("Not yet implemented")
    }

    override fun onAnnotationDragStarted(annotation: Annotation<*>) {
        TODO("Not yet implemented")
    }
}
