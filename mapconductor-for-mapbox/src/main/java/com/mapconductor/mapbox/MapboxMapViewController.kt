package com.mapconductor.mapbox

import android.animation.Animator
import android.util.AttributeSet
import com.google.gson.JsonObject
import com.mapbox.maps.CameraChanged
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapOptions
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapconductor.core.Offset
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

interface IMapboxMapInitOptions {
    val mapOptions: MapOptions?
    val plugins: List<Plugin>?
    val cameraOptions: CameraOptions?
    val textureView: Boolean?
    val styleUri: String?
    val attrs: AttributeSet?
    val antialiasingSampleCount: Int?
}
data class MapboxMapInitOptions(
    override val mapOptions: MapOptions? = null,
    override val plugins: List<Plugin>? = null,
    override val cameraOptions: CameraOptions? = null,
    override val textureView: Boolean? = null,
    override val styleUri: String? = null,
    override val attrs: AttributeSet? = null,
    override val antialiasingSampleCount: Int? = null,
) : IMapboxMapInitOptions

interface IMapboxMapViewController: MapViewController {
    fun moveCamera(dstPosition: MapCameraPosition, listener: MapViewState.MoveCameraCallback? = null)
    fun animateCamera(dstPosition: MapCameraPosition, duration: Long, listener: MapViewState.MoveCameraCallback? = null)
}

internal class MapboxMapViewController(
    override val holder: MapboxMapViewHolder,
    eventHandler: IMapboxMapEventHandler?,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
): IMapboxMapViewController,
    CameraChangedCallback,
    OnPointAnnotationClickListener
{

    private lateinit var pointAnnotationManager: PointAnnotationManager

    init {
        setupListeners()
        val annotationApi = holder.mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationManager.addClickListener(this@MapboxMapViewController)
    }

    private val eventHandlerRef = WeakReference(eventHandler)

    private fun setupListeners() {
        holder.map.subscribeCameraChanged(this)
    }

    private val baseMapViewController = BaseMapViewController<PointAnnotation>(
        coroutine = coroutine,
        onMarkerRemove = { id, marker ->
            synchronized(pointAnnotationManager) {
                pointAnnotationManager.delete(marker)
            }
        },
        onMarkerAdd = { newMarkers ->
            synchronized(pointAnnotationManager) {
                val options = newMarkers.map { params ->
                    return@map params.icon.toPointAnnotationOptions()
                        .withPoint(
                            GeoPoint.from(params.entry.state.position).toPoint(),
                        )
                        .withData(JsonObject().apply {
                            addProperty("id", params.entry.id)
                        })
                }
                return@BaseMapViewController pointAnnotationManager.create(options)
            }

        },
        onMarkerChanged = { changes ->
            changes.forEach { params ->
                // TODO: アイコンに変更があったかどうかを比較
                val option = params.icon.toPointAnnotationOptions()
                    .withPoint(
                        GeoPoint.from(params.entry.state.position).toPoint(),
                    )
                params.marker.point = GeoPoint.from(params.entry.state.position).toPoint()
                params.marker.iconSize = option.iconSize
                params.marker.iconImage = option.iconImage
                params.marker.iconAnchor = option.iconAnchor
                params.marker.iconOffset = option.iconOffset
            }
        },
    )

    override suspend fun addMarkers(markerList: List<MarkerEntry>) =
        baseMapViewController.addMarkers(markerList)

    override suspend fun clearOverlays() = baseMapViewController.clearOverlays()

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        val pixel = holder.map.pixelForCoordinate(
            coordinate = GeoPoint.from(position).toPoint(),
        )
        return Offset(
            x = pixel.x,
            y = pixel.y,
        )
    }

    override fun run(cameraChanged: CameraChanged) {
        eventHandlerRef.get()?.onCameraMove(cameraChanged.cameraState)
    }

    override fun moveCamera(dstPosition: MapCameraPosition, listener: MapViewState.MoveCameraCallback?) {
        coroutine.launch {
            holder.map.setCamera(dstPosition.toCameraOptions())
        }
        listener?.onComplete(true)
    }

    override fun animateCamera(
        dstPosition: MapCameraPosition,
        durationMs: Long,
        listener: MapViewState.MoveCameraCallback?
    ) {
        val targetCamera  = dstPosition.toCameraOptions()
        val animationOptions = MapAnimationOptions.Builder()
            .duration(durationMs)
            .build()

        val animatorListener = object : Animator.AnimatorListener {
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
                cameraOptions  = targetCamera,
                animationOptions = animationOptions,
                animatorListener = animatorListener,
            )
        }
    }

    override fun onAnnotationClick(annotation: PointAnnotation): Boolean {
        val tag = annotation.getData() ?: return false
        val key = tag.asJsonObject.get("id")?.asString ?: return false
        val stateWithHandler = baseMapViewController.getMarkerEntry(key) ?: return false
        val handlers = stateWithHandler.handlers
        handlers.onClick?.let {
            coroutine.launch {
                it(stateWithHandler.state)
            }
        }
        return true
    }

}