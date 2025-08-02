package com.mapconductor.googlemaps

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.mapconductor.core.circle.CircleClickEvent
import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleRenderer
import com.mapconductor.core.circle.CircleRendererFactory
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerRenderer
import com.mapconductor.core.marker.MarkerRendererFactory
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonRenderer
import com.mapconductor.core.polygon.PolygonRendererFactory
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineRenderer
import com.mapconductor.core.polyline.PolylineRendererFactory
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.projection.WebMercator
import com.mapconductor.googlemaps.circle.DefaultGoogleMapCircleRenderer
import com.mapconductor.googlemaps.circle.GoogleMapCircleRenderer
import com.mapconductor.googlemaps.marker.DefaultGoogleMapMarkerRenderer
import com.mapconductor.googlemaps.marker.GoogleMapMarkerRenderer
import com.mapconductor.googlemaps.polygon.DefaultGoogleMapPolygonRenderer
import com.mapconductor.googlemaps.polygon.GoogleMapPolygonRenderer
import com.mapconductor.googlemaps.polyline.DefaultGoogleMapPolylineRenderer
import com.mapconductor.googlemaps.polyline.GoogleMapPolylineRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IGoogleMapViewController : MapViewController<Marker, Circle, Polyline, Polygon> {
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
    override val hexGeocell: HexGeocell =
        HexGeocell(
            projection = WebMercator,
            baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
        ),
    private val markerRendererFactory: MarkerRendererFactory<Marker> = DefaultGoogleMapMarkerRenderer(),
    private val polylineRendererFactory: PolylineRendererFactory<Polyline> = DefaultGoogleMapPolylineRenderer(),
    private val polygonRendererFactory: PolygonRendererFactory<Polygon> = DefaultGoogleMapPolygonRenderer(),
    private val circleRendererFactory: CircleRendererFactory<Circle> = DefaultGoogleMapCircleRenderer(),
) : BaseMapViewController<CameraPosition, Marker, Circle, Polyline, Polygon>(),
    IGoogleMapViewController,
    OnCameraMoveStartedListener,
    OnCameraMoveCanceledListener,
    OnCameraMoveListener,
    OnCameraIdleListener,
    OnMarkerClickListener,
    OnMapClickListener,
    OnMarkerDragListener {
    override val markerRenderer: MarkerRenderer<Marker> =
        GoogleMapMarkerRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override fun createMarkerOverlayManager(): MarkerOverlayManager<Marker> =
        markerRendererFactory.create(
            hexGeocell = hexGeocell,
            onIconAdd = markerRenderer::addIcons,
            onIconRemove = markerRenderer::removeIcons,
            onIconChange = markerRenderer::changeIcons,
            onAnimate = markerRenderer::animate,
        )

    override val polylineRenderer: PolylineRenderer<Polyline> =
        GoogleMapPolylineRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override fun createPolylineOverlayManager(): PolylineOverlayManager<Polyline> =
        polylineRendererFactory.create(
            onAdd = polylineRenderer::addLines,
            onChange = polylineRenderer::changeLine,
            onRemove = polylineRenderer::removeLines,
        )

    override val polygonRenderer: PolygonRenderer<Polygon> =
        GoogleMapPolygonRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override fun createPolygonOverlayManager(): PolygonOverlayManager<Polygon> =
        polygonRendererFactory.create(
            onAdd = polygonRenderer::addPolygons,
            onChange = polygonRenderer::changePolygon,
            onRemove = polygonRenderer::removePolygons,
        )

    override fun onCircleOverlayManagerInitialized(overlayManager: CircleOverlayManager<Circle>) {
    }

    override fun onPolygonOverlayManagerInitialized(overlayManager: PolygonOverlayManager<Polygon>) {
    }

    override fun onPolylineOverlayManagerInitialized(overlayManager: PolylineOverlayManager<Polyline>) {
    }

    override fun onMarkerOverlayManagerInitialized(overlayManager: MarkerOverlayManager<Marker>) {
    }

    override val circleRenderer: CircleRenderer<Circle> =
        GoogleMapCircleRenderer(
            holder = holder,
            coroutine = coroutine,
        )

    override fun createCircleOverlayManager(): CircleOverlayManager<Circle> =
        circleRendererFactory.create(
            onAdd = circleRenderer::addCircles,
            onChange = circleRenderer::changeCircle,
            onRemove = circleRenderer::removeCircles,
        )

    init {
        setupListeners()
        markerRenderer.init(markerOverlayManager)
    }

    override fun setupListeners() {
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

    override suspend fun clearOverlays() {
        markerOverlayManager.clearOverlays()
        polylineOverlayManager.clearOverlays()
    }

    override suspend fun addMarkers(markerList: List<MarkerState>) = markerOverlayManager.addMarkers(markerList)

    override suspend fun updateMarker(state: MarkerState) = markerOverlayManager.updateMarker(state)

    override suspend fun addCircles(data: List<CircleState>) = circleOverlayManager.addCircles(data)

    override suspend fun updateCircle(state: CircleState) = circleOverlayManager.updateCircle(state)

    override suspend fun addPolylines(data: List<PolylineState>) = polylineOverlayManager.addPolylines(data)

    override suspend fun updatePolyline(state: PolylineState) = polylineOverlayManager.updatePolyline(state)

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
        val touchPosition = position.toGeoPoint()

        circleOverlayManager.find(touchPosition)?.let { entity ->
            val event =
                CircleClickEvent(
                    state = entity.state,
                    position = touchPosition,
                )
            circleClickListener?.invoke(event)
            return
        }

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
            markerRenderer.setDraggingState(state, true)

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
            markerRenderer.setDraggingState(state, false)

            markerDragStartListener?.invoke(state)
        }
    }
}
