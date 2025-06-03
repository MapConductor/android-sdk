package com.mapconductor.googlemaps

import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mapconductor.core.MarkerManager
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.controller.MarkerOverlayManager
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapClickHandler
import com.mapconductor.core.map.OnMarkerDragHandler
import com.mapconductor.core.marker.MarkerEntry
import com.mapconductor.core.projection.WebMercator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IGoogleMapViewController: MapViewController
{
    fun moveCamera(dstPosition: MapCameraPosition, listener: MapViewState.MoveCameraCallback? = null)
    fun animateCamera(dstPosition: MapCameraPosition, duration: Int, listener: MapViewState.MoveCameraCallback? = null)
}

class GoogleMapViewController(
    override val holder: GoogleMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val onCameraMove: (OnCameraMoveHandler<CameraPosition>)? = null,
    val onMapTap: OnMapClickHandler? = null,
): IGoogleMapViewController,
    OnCameraMoveStartedListener,
    OnCameraMoveCanceledListener,
    OnCameraMoveListener,
    OnCameraIdleListener,
    OnMarkerClickListener,
    OnMapClickListener,
    OnMarkerDragListener
{
//    private val infoBubbles = InfoBubbleManager(
//        coroutine = coroutine,
//    )
    private val markerOverlayManager = MarkerOverlayManager<Marker>(
        coroutine = coroutine,
        markerManager = MarkerManager(HexGeocell(WebMercator)),
        onRemove = { removes ->
            removes.forEach { params -> params.marker.remove() }
        },
        onAdd = { newMarkers ->
            newMarkers.map { params ->
                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.icon.bitmap)

                val options = MarkerOptions()
                    .position(GeoPoint.from(params.entry.state.position).toLatLng())
                    .anchor(params.icon.anchor.x.toFloat(), params.icon.anchor.y.toFloat())
                    .icon(bitmapDescriptor)
                    .draggable(true)
                val marker = holder.map.addMarker(options)?.also {
                    it.tag = params.entry.state.id
                }
                return@map marker
            }
        },
        onChange = { changes ->
            changes.forEach { params ->
                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.icon.bitmap)
                params.marker.position = GeoPoint.from(params.entry.state.position).toLatLng()
                params.marker.setIcon(bitmapDescriptor)
            }
        },
    )

    init {
        setupListeners()
    }
    private fun setupListeners() {
        holder.map.setOnCameraMoveStartedListener(this)
        holder.map.setOnCameraMoveCanceledListener(this)
        holder.map.setOnCameraMoveListener(this)
        holder.map.setOnCameraIdleListener(this)
        holder.map.setOnMarkerClickListener(this)
        holder.map.setOnMapClickListener(this)
        holder.map.setOnMarkerDragListener(this)
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
            holder.map.animateCamera(cameraUpdate, duration, object : CancelableCallback {
                override fun onCancel() {
                    listener?.onComplete(false)
                }

                override fun onFinish() {
                    listener?.onComplete(true)
                }
            })
        }
    }

    override suspend fun addMarkers(markerList : List<MarkerEntry>) =
        markerOverlayManager.addMarkers(markerList)

    override suspend fun clearOverlays() = markerOverlayManager.clearOverlays()

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        val point = holder.map.projection.toScreenLocation(
            GeoPoint.from(position).toLatLng(),
        )
        return Offset(
            x = point.x.toFloat(),
            y = point.y.toFloat(),
        )
    }

    override fun onCameraMove() {
        onCameraMove?.let {
            coroutine.launch { it(holder.map.cameraPosition) }
        }
    }

    override fun onCameraIdle() {
        onCameraMove?.let {
            coroutine.launch { it(holder.map.cameraPosition) }
        }
    }

    override fun onCameraMoveStarted(p0: Int) {
        onCameraMove?.let {
            coroutine.launch { it(holder.map.cameraPosition) }
        }
    }

    override fun onCameraMoveCanceled() {
        onCameraMove?.let {
            coroutine.launch { it(holder.map.cameraPosition) }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val key = marker.tag?.toString() ?: return true
        val entry = markerOverlayManager.getMarkerEntry(key) ?: return true
        entry.handlers.onClick?.let {
            coroutine.launch {
                it(entry.state)
            }
        }
        return true
    }

    override fun onMapClick(position: LatLng) {
        onMapTap?.let {
            coroutine.launch { it(position.toGeoPoint()) }
        }
    }

    override fun onMarkerDrag(marker: Marker) {
        val markerId = marker.tag as? String ?: return
        val entry = markerOverlayManager.getMarkerEntry(markerId) ?: return
        entry.state.position = marker.position.toGeoPoint()
    }

    override fun onMarkerDragEnd(marker: Marker) {
        val markerId = marker.tag as? String ?: return
        val entry = markerOverlayManager.getMarkerEntry(markerId) ?: return
        entry.state.position = marker.position.toGeoPoint()
    }

    override fun onMarkerDragStart(marker: Marker) {
        val markerId = marker.tag as? String ?: return
        val entry = markerOverlayManager.getMarkerEntry(markerId) ?: return
        entry.state.position = marker.position.toGeoPoint()
    }
}

