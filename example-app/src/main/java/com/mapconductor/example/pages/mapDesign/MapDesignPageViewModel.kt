package com.mapconductor.example.pages.mapDesign

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapDesignType
import com.mapconductor.core.map.MapViewState
import com.mapconductor.example.toast.ToastMessage
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapDesign
import com.mapconductor.here.HereMapViewState
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.MapboxMapViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapDesignOptions(
    val label: String,
    val design: MapDesignType<*>
)

interface MapDesignPageViewModel {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewState<*>?>
    val messages: StateFlow<List<ToastMessage>>

    val options: StateFlow<List<MapDesignOptions>>
    fun onMapViewChanged(state: MapViewState<*>)

    fun onMapClick(clicked: GeoPoint)

    fun showToast(text: String)

    fun removeToast(toastMessage: ToastMessage)
}

class MapDesignPageViewModelImpl():
    ViewModel(),
    MapDesignPageViewModel {

    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = 21.382314,
                    longitude = -157.933097,
                ),
            zoom = 12.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    private val _messages: MutableStateFlow<List<ToastMessage>> = MutableStateFlow(emptyList())
    override val messages: StateFlow<List<ToastMessage>> = _messages.asStateFlow()

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    private val _options: MutableStateFlow<List<MapDesignOptions>> = MutableStateFlow(emptyList())
    override val options: StateFlow<List<MapDesignOptions>> = _options.asStateFlow()

    override fun onMapViewChanged(state: MapViewState<*>) {
        this._mapViewState.value = state
        when(state) {
            is GoogleMapViewState -> {
                _options.value = googleMapDesigns
            }
            is HereMapViewState -> {
                _options.value = hereMapDesigns
            }
            is MapboxMapViewState -> {
                _options.value = mapboxMapDesigns
            }
            is ArcGISMapViewState -> {
                _options.value = arcGISMapDesigns
            }
        }
    }

    override fun onMapClick(clicked: GeoPoint) {
        showToast("Map clicked at: ${clicked.toUrlValue()}")
    }

    override fun showToast(text: String) {
        this._messages.value = this._messages.value + ToastMessage(text = text)
    }

    override fun removeToast(toastMessage: ToastMessage) {
        this._messages.value = this._messages.value.filter { it != toastMessage }
    }

    private val googleMapDesigns = listOf(
        MapDesignOptions(label = "Normal", design = GoogleMapDesign.Normal),
        MapDesignOptions(label = "Satellite", design = GoogleMapDesign.Satellite),
        MapDesignOptions(label = "Hybrid", design = GoogleMapDesign.Hybrid),
        MapDesignOptions(label = "Terrain", design = GoogleMapDesign.Terrain),
        MapDesignOptions(label = "None", design = GoogleMapDesign.None),
    )

    private val hereMapDesigns = listOf(
        MapDesignOptions(label = "NormalDay", design = HereMapDesign.NormalDay),
        MapDesignOptions(label = "NormalNigh", design = HereMapDesign.NormalNigh),
        MapDesignOptions(label = "Satellite", design = HereMapDesign.Satellite),
        MapDesignOptions(label = "HybridDay", design = HereMapDesign.HybridDay),
        MapDesignOptions(label = "HybridNight", design = HereMapDesign.HybridNight),
        MapDesignOptions(label = "LiteDay", design = HereMapDesign.LiteDay),
        MapDesignOptions(label = "LiteNight", design = HereMapDesign.LiteNight),
        MapDesignOptions(label = "LiteHybridDay", design = HereMapDesign.LiteHybridDay),
        MapDesignOptions(label = "LiteHybridNight", design = HereMapDesign.LiteHybridNight),
        MapDesignOptions(label = "LogisticsDay", design = HereMapDesign.LogisticsDay),
        MapDesignOptions(label = "LogisticsNight", design = HereMapDesign.LogisticsNight),
        MapDesignOptions(label = "LogisticsHybridDay", design = HereMapDesign.LogisticsHybridDay),
        MapDesignOptions(label = "RoadNetworkDay", design = HereMapDesign.RoadNetworkDay),
        MapDesignOptions(label = "RoadNetworkNight", design = HereMapDesign.RoadNetworkNight),
    )

    private val mapboxMapDesigns = listOf(
        MapDesignOptions(label = "Standard", design = MapboxMapDesign.Standard),
        MapDesignOptions(label = "StandardSatellite", design = MapboxMapDesign.StandardSatellite),
        MapDesignOptions(label = "Streets", design = MapboxMapDesign.Streets),
        MapDesignOptions(label = "Outdoors", design = MapboxMapDesign.Outdoors),
        MapDesignOptions(label = "Light", design = MapboxMapDesign.Light),
        MapDesignOptions(label = "Dark", design = MapboxMapDesign.Dark),
        MapDesignOptions(label = "Satellite", design = MapboxMapDesign.Satellite),
        MapDesignOptions(label = "SatelliteStreets", design = MapboxMapDesign.SatelliteStreets),
        MapDesignOptions(label = "NavigationDay", design = MapboxMapDesign.NavigationDay),
        MapDesignOptions(label = "NavigationNight", design = MapboxMapDesign.NavigationNight),
    )

    private val arcGISMapDesigns  = listOf(
        MapDesignOptions(label = "Streets", design = ArcGISDesign.Companion.Streets),
        MapDesignOptions(label = "Imagery", design = ArcGISDesign.Companion.Imagery),
        MapDesignOptions(label = "ImageryStandard", design = ArcGISDesign.Companion.ImageryStandard),
        MapDesignOptions(label = "ImageryLabels", design = ArcGISDesign.Companion.ImageryLabels),
        MapDesignOptions(label = "LightGray", design = ArcGISDesign.Companion.LightGray),
        MapDesignOptions(label = "LightGrayBase", design = ArcGISDesign.Companion.LightGrayBase),
        MapDesignOptions(label = "LightGrayLabels", design = ArcGISDesign.Companion.LightGrayLabels),
        MapDesignOptions(label = "DarkGray", design = ArcGISDesign.Companion.DarkGray),
        MapDesignOptions(label = "DarkGrayBase", design = ArcGISDesign.Companion.DarkGrayBase),
        MapDesignOptions(label = "DarkGrayLabels", design = ArcGISDesign.Companion.DarkGrayLabels),
        MapDesignOptions(label = "Navigation", design = ArcGISDesign.Companion.Navigation),
        MapDesignOptions(label = "NavigationNight", design = ArcGISDesign.Companion.NavigationNight),
        MapDesignOptions(label = "StreetsNight", design = ArcGISDesign.Companion.StreetsNight),
        MapDesignOptions(label = "StreetsRelief", design = ArcGISDesign.Companion.StreetsRelief),
        MapDesignOptions(label = "Topographic", design = ArcGISDesign.Companion.Topographic),
        MapDesignOptions(label = "Oceans", design = ArcGISDesign.Companion.Oceans),
        MapDesignOptions(label = "OceansBase", design = ArcGISDesign.Companion.OceansBase),
        MapDesignOptions(label = "OceansLabels", design = ArcGISDesign.Companion.OceansLabels),
        MapDesignOptions(label = "Terrain", design = ArcGISDesign.Companion.Terrain),
        MapDesignOptions(label = "TerrainBase", design = ArcGISDesign.Companion.TerrainBase),
        MapDesignOptions(label = "TerrainDetail", design = ArcGISDesign.Companion.TerrainDetail),
        MapDesignOptions(label = "Community", design = ArcGISDesign.Companion.Community),
        MapDesignOptions(label = "ChartedTerritory", design = ArcGISDesign.Companion.ChartedTerritory),
        MapDesignOptions(label = "ColoredPencil", design = ArcGISDesign.Companion.ColoredPencil),
        MapDesignOptions(label = "Nova", design = ArcGISDesign.Companion.Nova),
        MapDesignOptions(label = "ModernAntique", design = ArcGISDesign.Companion.ModernAntique),
        MapDesignOptions(label = "Midcentury", design = ArcGISDesign.Companion.Midcentury),
        MapDesignOptions(label = "Newspaper", design = ArcGISDesign.Companion.Newspaper),
        MapDesignOptions(label = "HillshadeLight", design = ArcGISDesign.Companion.HillshadeLight),
        MapDesignOptions(label = "HillshadeDark", design = ArcGISDesign.Companion.HillshadeDark),
        MapDesignOptions(label = "StreetsReliefBase", design = ArcGISDesign.Companion.StreetsReliefBase),
        MapDesignOptions(label = "TopographicBase", design = ArcGISDesign.Companion.TopographicBase),
        MapDesignOptions(label = "ChartedTerritoryBase", design = ArcGISDesign.Companion.ChartedTerritoryBase),
        MapDesignOptions(label = "ModernAntiqueBase", design = ArcGISDesign.Companion.ModernAntiqueBase),
        MapDesignOptions(label = "HumanGeography", design = ArcGISDesign.Companion.HumanGeography),
        MapDesignOptions(label = "HumanGeographyBase", design = ArcGISDesign.Companion.HumanGeographyBase),
        MapDesignOptions(label = "HumanGeographyDetail", design = ArcGISDesign.Companion.HumanGeographyDetail),
        MapDesignOptions(label = "HumanGeographyLabels", design = ArcGISDesign.Companion.HumanGeographyLabels),
        MapDesignOptions(label = "HumanGeographyDark", design = ArcGISDesign.Companion.HumanGeographyDark),
        MapDesignOptions(label = "HumanGeographyDarkBase", design = ArcGISDesign.Companion.HumanGeographyDarkBase),
        MapDesignOptions(label = "HumanGeographyDarkDetail", design = ArcGISDesign.Companion.HumanGeographyDarkDetail),
        MapDesignOptions(label = "HumanGeographyDarkLabels", design = ArcGISDesign.Companion.HumanGeographyDarkLabels),
        MapDesignOptions(label = "Outdoor", design = ArcGISDesign.Companion.Outdoor),
        MapDesignOptions(label = "OsmStandard", design = ArcGISDesign.Companion.OsmStandard),
        MapDesignOptions(label = "OsmStandardRelief", design = ArcGISDesign.Companion.OsmStandardRelief),
        MapDesignOptions(label = "OsmStandardReliefBase", design = ArcGISDesign.Companion.OsmStandardReliefBase),
        MapDesignOptions(label = "OsmStreets", design = ArcGISDesign.Companion.OsmStreets),
        MapDesignOptions(label = "OsmStreetsRelief", design = ArcGISDesign.Companion.OsmStreetsRelief),
        MapDesignOptions(label = "OsmLightGray", design = ArcGISDesign.Companion.OsmLightGray),
        MapDesignOptions(label = "OsmLightGrayBase", design = ArcGISDesign.Companion.OsmLightGrayBase),
        MapDesignOptions(label = "OsmLightGrayLabels", design = ArcGISDesign.Companion.OsmLightGrayLabels),
        MapDesignOptions(label = "OsmDarkGray", design = ArcGISDesign.Companion.OsmDarkGray),
        MapDesignOptions(label = "OsmDarkGrayBase", design = ArcGISDesign.Companion.OsmDarkGrayBase),
        MapDesignOptions(label = "OsmDarkGrayLabels", design = ArcGISDesign.Companion.OsmDarkGrayLabels),
        MapDesignOptions(label = "OsmStreetsReliefBase", design = ArcGISDesign.Companion.OsmStreetsReliefBase),
        MapDesignOptions(label = "OsmBlueprint", design = ArcGISDesign.Companion.OsmBlueprint),
        MapDesignOptions(label = "OsmHybrid", design = ArcGISDesign.Companion.OsmHybrid),
        MapDesignOptions(label = "OsmHybridDetail", design = ArcGISDesign.Companion.OsmHybridDetail),
        MapDesignOptions(label = "OsmNavigation", design = ArcGISDesign.Companion.OsmNavigation),
        MapDesignOptions(label = "OsmNavigationDark", design = ArcGISDesign.Companion.OsmNavigationDark),
    )
}
