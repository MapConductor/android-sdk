package com.mapconductor.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.arcgis.ArcGISMapView
import com.mapconductor.arcgis.ArcGISMapView2D
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.compose.MapViewScope
import com.mapconductor.core.OnCameraMoveHandler
import com.mapconductor.core.OnMapEventHandler
import com.mapconductor.core.OnMapLoadedHandler
import com.mapconductor.core.map.CameraRestriction
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.example.ui.LocalSelectedProviderKey
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapView
import com.mapconductor.here.HereViewState
import com.mapconductor.longdo.LongdoMapView
import com.mapconductor.longdo.LongdoViewState
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.MapboxViewState
import com.mapconductor.maplibre.MapLibreMapView
import com.mapconductor.maplibre.MapLibreViewState
import com.mapconductor.maptiler.MapTilerMapView
import com.mapconductor.maptiler.MapTilerViewState
import com.mapconductor.tomtom.TomTomMapView
import com.mapconductor.tomtom.TomTomMapViewState

@Composable
@Suppress("DEPRECATION")
fun MapViewContainer(
    modifier: Modifier = Modifier,
    state: MapViewStateInterface<*>? = null,
    // ArcGIS の 2D / 3D はどちらも ArcGISMapViewState を使うため、状態の型では描き分けられない。
    // 既定では画面が選択中のプロバイダキー（[LocalSelectedProviderKey]）を見るが、
    // Camera Sync のようにペインごとに別のプロバイダを選ぶ画面では CompositionLocal では
    // 表現できないので、呼び出し側から明示的に渡せるようにしている。
    providerKey: String = LocalSelectedProviderKey.current,
    markerTiling: MarkerTilingOptions? = null,
    cameraRestriction: CameraRestriction? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMapLongClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    content: (@Composable MapViewScope.() -> Unit)? = null,
) {
    @Suppress("UNCHECKED_CAST")
    when (state) {
        is GoogleMapViewState ->
            GoogleMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                cameraRestriction = cameraRestriction,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is HereViewState ->
            HereMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                cameraRestriction = cameraRestriction,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is MapboxViewState ->
            MapboxMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                cameraRestriction = cameraRestriction,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is ArcGISMapViewState ->
            if (providerKey == "arcgis2d") {
                ArcGISMapView2D(
                    modifier = modifier,
                    state = state,
                    markerTiling = markerTiling,
                    cameraRestriction = cameraRestriction,
                    onMapLoaded = onMapLoaded,
                    onMapClick = onMapClick,
                    onMapLongClick = onMapLongClick,
                    onCameraMoveStart = onCameraMoveStart,
                    onCameraMove = onCameraMove,
                    onCameraMoveEnd = onCameraMoveEnd,
                    content = content,
                )
            } else {
                ArcGISMapView(
                    modifier = modifier,
                    state = state,
                    markerTiling = markerTiling,
                    cameraRestriction = cameraRestriction,
                    onMapLoaded = onMapLoaded,
                    onMapClick = onMapClick,
                    onMapLongClick = onMapLongClick,
                    onCameraMoveStart = onCameraMoveStart,
                    onCameraMove = onCameraMove,
                    onCameraMoveEnd = onCameraMoveEnd,
                    content = content,
                )
            }

        is MapLibreViewState ->
            MapLibreMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                cameraRestriction = cameraRestriction,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is TomTomMapViewState ->
            TomTomMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                cameraRestriction = cameraRestriction,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is MapTilerViewState ->
            MapTilerMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                cameraRestriction = cameraRestriction,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        is LongdoViewState ->
            LongdoMapView(
                modifier = modifier,
                state = state,
                markerTiling = markerTiling,
                cameraRestriction = cameraRestriction,
                onMapLoaded = onMapLoaded,
                onMapClick = onMapClick,
                onMapLongClick = onMapLongClick,
                onCameraMoveStart = onCameraMoveStart,
                onCameraMove = onCameraMove,
                onCameraMoveEnd = onCameraMoveEnd,
                content = content,
            )

        else -> throw IllegalStateException("unknown state")
    }
}
