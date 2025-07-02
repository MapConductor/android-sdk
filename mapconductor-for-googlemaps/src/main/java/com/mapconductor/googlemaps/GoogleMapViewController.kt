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
import com.google.android.gms.maps.model.PolylineOptions
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayManagerImpl
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.WebMercator
import android.graphics.Color
import android.graphics.Point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface IGoogleMapViewController : MapViewController<Marker> {
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
    override val hexCell: HexGeocell =
        HexGeocell(
            projection = WebMercator,
            baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
        ),
) : BaseMapViewController<CameraPosition, Marker>(),
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
            markerManager = MarkerManager(hexCell),
            onRemove = { removes ->
                coroutine.launch {
                    removes.forEach { params -> params.marker.remove() }
                }
            },
            onAdd = { newMarkers ->
                withContext(coroutine.coroutineContext) {
                    newMarkers.map { params ->
                        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.second.bitmap)
                        val options =
                            MarkerOptions()
                                .position(GeoPoint.from(params.first.position).toLatLng())
                                .anchor(
                                    params.second.anchor.x
                                        .toFloat(),
                                    params.second.anchor.y
                                        .toFloat(),
                                ).icon(bitmapDescriptor)
                                .draggable(params.first.draggable)
                        val marker =
                            holder.map.addMarker(options)?.also {
                                it.tag = params.first.id
                            }
                        return@map marker
                    }
                }
            },
            onChange = { changes ->
                changes.map { params ->
                    if (params.entity.state.icon != params.prevEntity.state.icon) {
                        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(params.bitmapIcon.bitmap)
                        params.entity.marker.setIcon(bitmapDescriptor)
                    }
                    if (params.entity.state.position != params.prevEntity.state.position) {
                        params.entity.marker.position =
                            params.entity.state.position
                                .toLatLng()
                    }

                    // Google Mapsはマーカーを再作成しなくてよいので、同じマーカーのインスタンスを返す
                    params.entity.marker
                }
            },
            onPostProcess = {
                // Do nothing here
            },
            onAnimate = {
                when (it.state.animation) {
                    MarkerAnimation.Drop -> this.animateMarkerDrop(it)
                    MarkerAnimation.Bounce -> this.animateMarkerBounce(it)
                    else -> throw IllegalArgumentException("No animation is available: ${it.state.animation}")
                }
            },
        )

    init {
        setupListeners()
    }

/*
    private fun markerDropAnimation(params: MarkerModifyParams<Marker>) {
        val markerLatLng = params.marker.position.toGeoPoint()
        val interpolator = LinearInterpolator()
        val markerPoint = this.toScreenOffset(markerLatLng) ?: return
        val startPoint = Offset(markerPoint.x, 0f)
        val duration = Settings.Default.markerDropAnimateDuration

        markerAnimateStartListener?.let { it(params.state) }

        flow{
            val startTime = SystemClock.uptimeMillis()
            while (true){
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t = min(1f, elapsed.toFloat() / duration)
                emit(interpolator.getInterpolation(t))
                if (t >= 1f) break
                delay(16)
            }
        }.onEach { t ->
            val startLatLng = this.fromScreenOffset(startPoint) ?: return@onEach
            val lng = t * markerLatLng.longitude + (1 - t) * startLatLng.longitude
            val lat = t * markerLatLng.latitude + (1 - t) * startLatLng.latitude
            params.marker.position = LatLng(lat, lng)
        }.onCompletion {
            params.marker.position = markerLatLng.toLatLng()
            params.state.animation = null
            markerAnimateEndListener?.let { it(params.state) }
        }.launchIn(coroutine)
    }

    private fun markerBounceAnimation(params: MarkerModifyParams<Marker>) {
        val startTime = SystemClock.uptimeMillis()
        val duration = Settings.Default.markerBounceAnimateDuration
        val interpolator: Interpolator = BounceInterpolator()
        val markerLatLng = params.marker.position.toGeoPoint()
        val startPoint = Offset(0f , -200f)

        markerAnimateStartListener?.let { it(params.state) }

        flow {
            while (true) {
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t = interpolator.getInterpolation(min(1f, elapsed.toFloat() / duration))
                emit(t)
                if (t >= 1f) break
                delay(16L)
            }
        }.onEach { t ->
            val startLatLng = this.fromScreenOffset(startPoint) ?: return@onEach
            val lng = markerLatLng.longitude
            val lat = t * markerLatLng.latitude + (1 - t) * startLatLng.latitude
            params.marker.position = LatLng(lat, lng)
        }.onCompletion {
            params.marker.position = markerLatLng.toLatLng()
            params.state.animation = null

            markerAnimateEndListener?.let { it(params.state) }
        }.launchIn(coroutine)
    }
*/
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

    override fun setMarkerPosition(
        markerEntity: MarkerEntity<Marker>,
        position: GeoPoint,
    ) {
        markerEntity.marker.position = position.toLatLng()
    }

    override fun clearPolyline() {
        holder.map.clear()
    }

    override fun drawPolyline(geoPoints: List<IGeoPoint>) {
        val options =
            PolylineOptions().also {
                it.color(Color.RED)
                it.width(2f)
            }
        geoPoints.forEach {
            options.add(GeoPoint.from(it).toLatLng())
        }
        val polyline = holder.map.addPolyline(options)
    }
}
