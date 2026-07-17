package com.mapconductor.example.pages.rasterlayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.AttributionRule
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface RasterLayerPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    var opacity: Float

    var selectedLayer: GsiLayer

    val rasterLayerState: RasterLayerState

    fun onMapViewChanged(state: MapViewStateInterface<*>)
}

enum class GsiLayer {
    RELIEF,
    STANDARD,
}

class RasterLayerPageViewModel :
    ViewModel(),
    RasterLayerPageViewModelInterface {
    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 35.6812,
                    longitude = 139.7671,
                ),
            zoom = 5.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    override var opacity by mutableStateOf(0.75f)

    override var selectedLayer by mutableStateOf(GsiLayer.RELIEF)

    override val rasterLayerState: RasterLayerState
        get() =
            RasterLayerState(
                id = "rasterLayer",
                source =
                    if (selectedLayer == GsiLayer.RELIEF) {
                        RasterLayerSource.UrlTemplate(
                            template = "https://cyberjapandata.gsi.go.jp/xyz/relief/{z}/{x}/{y}.png",
                            tileSize = 256,
                            minZoom = 5,
                            maxZoom = 15,
                            attributionRules =
                                listOf(
                                    AttributionRule(
                                        attribution = "<a href=\"https://maps.gsi.go.jp/development/ichiran.html\">地理院タイル</a>",
                                    ),
                                    AttributionRule(attribution = "海域部は海上保安庁海洋情報部の資料を使用して作成"),
                                ),
                        )
                    } else {
                        RasterLayerSource.UrlTemplate(
                            template = "https://cyberjapandata.gsi.go.jp/xyz/std/{z}/{x}/{y}.png",
                            tileSize = 256,
                            minZoom = 5,
                            maxZoom = 18,
                            attributionRules =
                                listOf(
                                    AttributionRule(attribution = "<a href=\"https://maps.gsi.go.jp/development/ichiran.html\">地理院タイル</a>"),
                                    AttributionRule(
                                        attribution = "The bathymetric contours are derived from those contained within the GEBCO Digital Atlas, published by the BODC on behalf of IOC and IHO (2003) (<a href=\"https://www.gebco.net\">https://www.gebco.net</a>)",
                                        minZoom = 5,
                                        maxZoom = 8,
                                    ),
                                    AttributionRule(attribution = "海上保安庁許可第292502号（水路業務法第25条に基づく類似刊行物）", minZoom = 5, maxZoom = 8),
                                    AttributionRule(
                                        attribution = "Shoreline data is derived from: United States. National Imagery and Mapping Agency. &quot;Vector Map Level 0 (VMAP0).&quot; Bethesda, MD: Denver, CO: The Agency; USGS Information Services, 1997.",
                                        minZoom = 5,
                                        maxZoom = 8,
                                    ),
                                ),
                        )
                    },
                opacity = opacity,
            )

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        mapViewState.value?.cameraPosition?.let {
            state.moveCameraTo(it)
        }
        this._mapViewState.value = state
    }

    override fun onCleared() {
        super.onCleared()
    }
}
