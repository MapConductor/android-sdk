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
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.controller.MarkerModifyParams
import com.mapconductor.core.controller.MarkerOverlayManagerImpl
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.WebMercator
import android.graphics.Point
import android.os.SystemClock
import android.view.animation.LinearInterpolator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : BaseMapViewController<CameraPosition>(),
    IGoogleMapViewController,
    OnCameraMoveStartedListener,
    OnCameraMoveCanceledListener,
    OnCameraMoveListener,
    OnCameraIdleListener,
    OnMarkerClickListener,
    OnMapClickListener,
    OnMarkerDragListener {
    override val markerOverlayManager =
        MarkerOverlayManagerImpl<Marker>(
            coroutine = coroutine,
            markerManager = MarkerManager(HexGeocell(WebMercator)),
            onRemove = { removes ->
                removes.forEach { params -> params.marker.remove() }
            },
            onAdd = { newMarkers ->
                newMarkers.map { params ->
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.icon.bitmap)

                    val options =
                        MarkerOptions()
                            .position(GeoPoint.from(params.state.position).toLatLng())
                            .anchor(
                                params.icon.anchor.x
                                    .toFloat(),
                                params.icon.anchor.y
                                    .toFloat(),
                            ).icon(bitmapDescriptor)
                            .draggable(params.state.draggable)
                    val marker =
                        holder.map.addMarker(options)?.also {
                            it.tag = params.state.id
                        }
                    return@map marker
                }
            },
            onChange = { changes ->
                changes.map { params ->
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.icon.bitmap)
                    params.marker.position = GeoPoint.from(params.state.position).toLatLng()
                    params.marker.setIcon(bitmapDescriptor)

                    // Google Mapsはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
                    params.marker
                }
            },
            onIconChange = { marker, icon ->
                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(icon.bitmap)
                marker.setIcon(bitmapDescriptor)
            },
            onAnimation = {
                when (it.state.animation) {
                    MarkerAnimation.Drop -> this.markerDropAnimation(it)
                    MarkerAnimation.Bounce -> {

                    }
                    else -> { /* Do nothing here */ }
                }
            },
        )

    init {
        setupListeners()
    }

    private fun markerDropAnimation(params: MarkerModifyParams<Marker>) {
        val markerLatLng = params.marker.position.toGeoPoint()
        val interpolator = LinearInterpolator()
        val markerPoint = this.toScreenOffset(markerLatLng)

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

    override suspend fun addMarkers(markerList: List<MarkerState>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun clearOverlays() = markerOverlayManager.clearOverlays()

    override fun toScreenOffset(position: IGeoPoint): Offset? {
        val point =
            holder.map.projection.toScreenLocation(
                GeoPoint.from(position).toLatLng(),
            )
        return Offset(
            x = point.x.toFloat(),
            y = point.y.toFloat(),
        )
    }

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? =
        holder.map.projection
            .fromScreenLocation(
                Point(
                    offset.x.toInt(),
                    offset.y.toInt(),
                ),
            ).toGeoPoint()

    override suspend fun updateMarker(state: MarkerState) = markerOverlayManager.updateMarker(state)

    override fun onCameraMove() {
        cameraMoveListener?.let {
            coroutine.launch { it(holder.map.cameraPosition) }
        }
    }

    override fun onCameraIdle() {
        cameraMoveListener?.let {
            coroutine.launch { it(holder.map.cameraPosition) }
        }
    }

    override fun onCameraMoveStarted(p0: Int) {
        cameraMoveListener?.let {
            coroutine.launch { it(holder.map.cameraPosition) }
        }
    }

    override fun onCameraMoveCanceled() {
        cameraMoveListener?.let {
            coroutine.launch { it(holder.map.cameraPosition) }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val key = marker.tag?.toString() ?: return true
        val state = markerOverlayManager.getMarkerState(key) ?: return true
        markerClickListener?.let {
            coroutine.launch {
                it(state)
            }
        }
        return true
    }

    override fun onMapClick(position: LatLng) {
        mapClickListener?.let {
            coroutine.launch { it(position.toGeoPoint()) }
        }
    }

    private fun getMarkerStateFrom(marker: Marker): MarkerState? {
        val markerId = marker.tag as? String ?: return null
        return markerOverlayManager.getMarkerState(markerId)
    }

    override fun onMarkerDrag(marker: Marker) {
        this.getMarkerStateFrom(marker)?.also { state ->

            // Suppress the recomposition for the position property
            setDraggingState(state, true)

            state.position = marker.position.toGeoPoint()
            markerDragListener?.invoke(state)
        }
    }

    override fun onMarkerDragEnd(marker: Marker) {
        this.getMarkerStateFrom(marker)?.also { state ->
            state.position = marker.position.toGeoPoint()
            markerDragEndListener?.invoke(state)
        }
    }

    override fun onMarkerDragStart(marker: Marker) {
        this.getMarkerStateFrom(marker)?.also { state ->
            state.position = marker.position.toGeoPoint()

            // Restore the recomposition for the position property
            setDraggingState(state, false)

            markerDragStartListener?.invoke(state)
        }
    }
}
