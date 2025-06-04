package com.mapconductor.googlemaps

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
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

interface IGoogleMapViewController : MapViewController {
    fun moveCamera(
        dstPosition: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback? = null,
    )

    fun animateCamera(
        dstPosition: MapCameraPosition,
        duration: Int,
        listener: MapViewState.MoveCameraCallback? = null,
    )
}

class GoogleMapViewController(
    override val holder: GoogleMapViewHolder,
    eventHandler: IGoogleMapEventHandler?,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : IGoogleMapViewController,
    OnCameraMoveStartedListener,
    OnCameraMoveCanceledListener,
    OnCameraMoveListener,
    OnCameraIdleListener,
    OnMarkerClickListener {
    private val baseMapViewController =
        BaseMapViewController<Marker>(
            coroutine = coroutine,
            onMarkerRemove = { id, marker ->
                marker.remove()
                coroutine.launch {
                    eventHandlerRef.get()?.onMarkerRemove(id)
                }
            },
            onMarkerAdd = { newMarkers ->
                newMarkers.map { params ->
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.icon.bitmap)

                    val options =
                        MarkerOptions()
                            .position(GeoPoint.from(params.entry.state.position).toLatLng())
                            .anchor(
                                params.icon.anchor.x
                                    .toFloat(),
                                params.icon.anchor.y
                                    .toFloat(),
                            ).icon(bitmapDescriptor)
                            .draggable(true)
                    val marker =
                        holder.map.addMarker(options)?.also {
                            it.tag = params.entry.state.id
                        }
                    return@map marker
                }
            },
            onMarkerChanged = { changes ->
                changes.forEach { params ->
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.icon.bitmap)
                    params.marker.position = GeoPoint.from(params.entry.state.position).toLatLng()
                    params.marker.setIcon(bitmapDescriptor)
                }
            },
        )

    private val eventHandlerRef = WeakReference(eventHandler)

    init {
        setupListeners()
    }

    private fun setupListeners() {
        holder.map.setOnCameraMoveStartedListener(this)
        holder.map.setOnCameraMoveCanceledListener(this)
        holder.map.setOnCameraMoveListener(this)
        holder.map.setOnCameraIdleListener(this)
        holder.map.setOnMarkerClickListener(this)
    }

    override fun moveCamera(
        position: MapCameraPosition,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        coroutine.launch {
            val dstCameraPosition = position.toCameraPosition()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(dstCameraPosition)
            holder.map.moveCamera(cameraUpdate)
            listener?.onComplete(true)
        }
    }

    override fun animateCamera(
        position: MapCameraPosition,
        duration: Int,
        listener: MapViewState.MoveCameraCallback?,
    ) {
        val dstCameraPosition = position.toCameraPosition()
        coroutine.launch {
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(dstCameraPosition)
            holder.map.animateCamera(
                cameraUpdate,
                duration,
                object : CancelableCallback {
                    override fun onCancel() {
                        listener?.onComplete(false)
                    }

                    override fun onFinish() {
                        listener?.onComplete(true)
                    }
                },
            )
        }
    }

    override suspend fun addMarkers(markerList: List<MarkerEntry>) = baseMapViewController.addMarkers(markerList)

    override suspend fun clearOverlays() = baseMapViewController.clearOverlays()

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        val point =
            holder.map.projection.toScreenLocation(
                GeoPoint.from(position).toLatLng(),
            )
        return Offset(
            x = point.x.toDouble(),
            y = point.y.toDouble(),
        )
    }

    override fun onCameraMove() {
        coroutine.launch {
            eventHandlerRef.get()?.onCameraMove(holder.map.cameraPosition)
        }
    }

    override fun onCameraIdle() {
        coroutine.launch {
            eventHandlerRef.get()?.onCameraMove(holder.map.cameraPosition)
        }
    }

    override fun onCameraMoveStarted(p0: Int) {
        coroutine.launch {
            eventHandlerRef.get()?.onCameraMove(holder.map.cameraPosition)
        }
    }

    override fun onCameraMoveCanceled() {
        coroutine.launch {
            eventHandlerRef.get()?.onCameraMove(holder.map.cameraPosition)
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val key = marker.tag?.toString() ?: return true
        val entry = baseMapViewController.getMarkerEntry(key) ?: return true
        entry.handlers.onClick?.let {
            coroutine.launch {
                it(entry.state)
            }
        }
        return true
    }
}
