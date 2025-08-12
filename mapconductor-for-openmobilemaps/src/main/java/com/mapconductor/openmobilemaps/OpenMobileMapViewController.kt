package com.mapconductor.openmobilemaps

import com.mapconductor.core.circle.CircleOverlayManager
import com.mapconductor.core.circle.CircleRenderer
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerRenderer
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonOverlayManager
import com.mapconductor.core.polygon.PolygonRenderer
import com.mapconductor.core.polyline.PolylineOverlayManager
import com.mapconductor.core.polyline.PolylineRenderer
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.projection.WebMercator
import io.openmobilemaps.mapscore.shared.map.MapCameraInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

interface IOpenMobileMapViewController :
    MapViewController<
        OpenMobileMapActualMarker,
        OpenMobileMapActualCircle,
        OpenMobileMapActualPolyline,
        OpenMobileMapActualPolygon,
    > {

}

internal class OpenMobileMapViewController(
    override val holder: OpenMobileMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    override val hexGeocell: HexGeocell =
        HexGeocell(
            projection = WebMercator,
            baseHexSideLength = 100000, // 100km - 中ズームレベルに適した値
        ),
): BaseMapViewController<
    MapCameraInterface,
    OpenMobileMapActualMarker,
    OpenMobileMapActualCircle,
    OpenMobileMapActualPolyline,
    OpenMobileMapActualPolygon,
    >(),
    IOpenMobileMapViewController {
    override val markerRenderer: MarkerRenderer<OpenMobileMapActualMarker>
        get() = TODO("Not yet implemented")
    override val polylineRenderer: PolylineRenderer<OpenMobileMapActualPolyline>
        get() = TODO("Not yet implemented")
    override val polygonRenderer: PolygonRenderer<OpenMobileMapActualPolygon>
        get() = TODO("Not yet implemented")
    override val circleRenderer: CircleRenderer<OpenMobileMapActualCircle>
        get() = TODO("Not yet implemented")

    override fun createMarkerOverlayManager(): MarkerOverlayManager<OpenMobileMapActualMarker> {
        TODO("Not yet implemented")
    }

    override fun createPolylineOverlayManager(): PolylineOverlayManager<OpenMobileMapActualPolyline> {
        TODO("Not yet implemented")
    }

    override fun createPolygonOverlayManager(): PolygonOverlayManager<OpenMobileMapActualPolygon> {
        TODO("Not yet implemented")
    }

    override fun createCircleOverlayManager(): CircleOverlayManager<OpenMobileMapActualCircle> {
        TODO("Not yet implemented")
    }

    override fun setupListeners() {
    }

    override fun onCircleOverlayManagerInitialized(overlayManager: CircleOverlayManager<OpenMobileMapActualCircle>) {
    }

    override fun onPolygonOverlayManagerInitialized(overlayManager: PolygonOverlayManager<OpenMobileMapActualPolygon>) {
    }

    override fun onPolylineOverlayManagerInitialized(overlayManager: PolylineOverlayManager<OpenMobileMapActualPolyline>) {
    }

    override fun onMarkerOverlayManagerInitialized(overlayManager: MarkerOverlayManager<OpenMobileMapActualMarker>) {
    }

    override suspend fun addMarkers(data: List<MarkerState>) {
    }

    override suspend fun updateMarker(state: MarkerState) {
    }

    override suspend fun addPolylines(data: List<PolylineState>) {
    }

    override suspend fun updatePolyline(state: PolylineState) {
    }

    override suspend fun addCircles(data: List<CircleState>) {
    }

    override suspend fun updateCircle(state: CircleState) {
    }

    override suspend fun clearOverlays() {
    }
}
