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

    var design: Int

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

    override var design by mutableStateOf(1)

    override fun onMapViewChanged(state: MapViewState<*>) {
        this._mapViewState.value = state
        when(state) {
            is GoogleMapViewState -> {
                _options.value = listOf(
                    MapDesignOptions(
                        label = "Normal",
                        design = GoogleMapDesign.Normal,
                    ),
                    MapDesignOptions(
                        label = "Satellite",
                        design = GoogleMapDesign.Satellite,
                    ),
                    MapDesignOptions(
                        label = "Hybrid",
                        design = GoogleMapDesign.Hybrid,
                    ),
                    MapDesignOptions(
                        label = "Terrain",
                        design = GoogleMapDesign.Terrain,
                    ),
                    MapDesignOptions(
                        label = "None",
                        design = GoogleMapDesign.None,
                    ),
                )
            }
            is HereMapViewState -> {
                _options.value = listOf(
                    MapDesignOptions(
                        label = "NormalDay",
                        design = HereMapDesign.NormalDay,
                    ),
                    MapDesignOptions(
                        label = "NormalNigh",
                        design = HereMapDesign.NormalNigh,
                    ),
                    MapDesignOptions(
                        label = "Satellite",
                        design = HereMapDesign.Satellite,
                    ),
                    MapDesignOptions(
                        label = "HybridDay",
                        design = HereMapDesign.HybridDay,
                    ),
                    MapDesignOptions(
                        label = "HybridNight",
                        design = HereMapDesign.HybridNight,
                    ),
                    MapDesignOptions(
                        label = "LiteDay",
                        design = HereMapDesign.LiteDay,
                    ),
                    MapDesignOptions(
                        label = "LiteNight",
                        design = HereMapDesign.LiteNight,
                    ),
                    MapDesignOptions(
                        label = "LiteHybridDay",
                        design = HereMapDesign.LiteHybridDay,
                    ),
                    MapDesignOptions(
                        label = "LiteHybridNight",
                        design = HereMapDesign.LiteHybridNight,
                    ),
                    MapDesignOptions(
                        label = "LogisticsDay",
                        design = HereMapDesign.LogisticsDay,
                    ),
                    MapDesignOptions(
                        label = "LogisticsNight",
                        design = HereMapDesign.LogisticsNight,
                    ),
                    MapDesignOptions(
                        label = "LogisticsHybridDay",
                        design = HereMapDesign.LogisticsHybridDay,
                    ),
                    MapDesignOptions(
                        label = "RoadNetworkDay",
                        design = HereMapDesign.RoadNetworkDay,
                    ),
                    MapDesignOptions(
                        label = "RoadNetworkNight",
                        design = HereMapDesign.RoadNetworkNight,
                    ),
                )
            }
            is MapboxMapViewState -> {
                _options.value = listOf(
                    MapDesignOptions(
                        label = "Standard",
                        design = MapboxMapDesign.Standard,
                    ),
                    MapDesignOptions(
                        label = "StandardSatellite",
                        design = MapboxMapDesign.StandardSatellite,
                    ),
                    MapDesignOptions(
                        label = "Streets",
                        design = MapboxMapDesign.Streets,
                    ),
                    MapDesignOptions(
                        label = "Outdoors",
                        design = MapboxMapDesign.Outdoors,
                    ),
                    MapDesignOptions(
                        label = "Light",
                        design = MapboxMapDesign.Light,
                    ),
                    MapDesignOptions(
                        label = "Dark",
                        design = MapboxMapDesign.Dark,
                    ),
                    MapDesignOptions(
                        label = "Satellite",
                        design = MapboxMapDesign.Satellite,
                    ),
                    MapDesignOptions(
                        label = "SatelliteStreets",
                        design = MapboxMapDesign.SatelliteStreets,
                    ),
                    MapDesignOptions(
                        label = "NavigationDay",
                        design = MapboxMapDesign.NavigationDay,
                    ),
                    MapDesignOptions(
                        label = "NavigationNight",
                        design = MapboxMapDesign.NavigationNight,
                    ),
                )
            }
            is ArcGISMapViewState -> {
                _options.value = listOf(
                    MapDesignOptions(
                        label = "Streets",
                        design = ArcGISDesign.Streets,
                    ),
                    MapDesignOptions(
                        label = "Imagery",
                        design = ArcGISDesign.Imagery,
                    ),
                    MapDesignOptions(
                        label = "ImageryStandard",
                        design = ArcGISDesign.ImageryStandard,
                    ),
                    MapDesignOptions(
                        label = "ImageryLabels",
                        design = ArcGISDesign.ImageryLabels,
                    ),
                    MapDesignOptions(
                        label = "LightGray",
                        design = ArcGISDesign.LightGray,
                    ),
                    MapDesignOptions(
                        label = "LightGrayBase",
                        design = ArcGISDesign.LightGrayBase,
                    ),
                    MapDesignOptions(
                        label = "LightGrayLabels",
                        design = ArcGISDesign.LightGrayLabels,
                    ),
                    MapDesignOptions(
                        label = "DarkGray",
                        design = ArcGISDesign.DarkGray,
                    ),
                    MapDesignOptions(
                        label = "DarkGrayBase",
                        design = ArcGISDesign.DarkGrayBase,
                    ),
                    MapDesignOptions(
                        label = "DarkGrayLabels",
                        design = ArcGISDesign.DarkGrayLabels,
                    ),
                    MapDesignOptions(
                        label = "Navigation",
                        design = ArcGISDesign.Navigation,
                    ),
                    MapDesignOptions(
                        label = "NavigationNight",
                        design = ArcGISDesign.NavigationNight,
                    ),
                    MapDesignOptions(
                        label = "StreetsNight",
                        design = ArcGISDesign.StreetsNight,
                    ),
                    MapDesignOptions(
                        label = "StreetsRelief",
                        design = ArcGISDesign.StreetsRelief,
                    ),
                    MapDesignOptions(
                        label = "Topographic",
                        design = ArcGISDesign.Topographic,
                    ),
                    MapDesignOptions(
                        label = "Oceans",
                        design = ArcGISDesign.Oceans,
                    ),
                    MapDesignOptions(
                        label = "OceansBase",
                        design = ArcGISDesign.OceansBase,
                    ),
                    MapDesignOptions(
                        label = "OceansLabels",
                        design = ArcGISDesign.OceansLabels,
                    ),
                    MapDesignOptions(
                        label = "Terrain",
                        design = ArcGISDesign.Terrain,
                    ),
                    MapDesignOptions(
                        label = "TerrainBase",
                        design = ArcGISDesign.TerrainBase,
                    ),
                    MapDesignOptions(
                        label = "TerrainDetail",
                        design = ArcGISDesign.TerrainDetail,
                    ),
                    MapDesignOptions(
                        label = "Community",
                        design = ArcGISDesign.Community,
                    ),
                    MapDesignOptions(
                        label = "ChartedTerritory",
                        design = ArcGISDesign.ChartedTerritory,
                    ),
                    MapDesignOptions(
                        label = "ColoredPencil",
                        design = ArcGISDesign.ColoredPencil,
                    ),
                    MapDesignOptions(
                        label = "Nova",
                        design = ArcGISDesign.Nova,
                    ),
                    MapDesignOptions(
                        label = "ModernAntique",
                        design = ArcGISDesign.ModernAntique,
                    ),
                    MapDesignOptions(
                        label = "Midcentury",
                        design = ArcGISDesign.Midcentury,
                    ),
                    MapDesignOptions(
                        label = "Newspaper",
                        design = ArcGISDesign.Newspaper,
                    ),
                    MapDesignOptions(
                        label = "HillshadeLight",
                        design = ArcGISDesign.HillshadeLight,
                    ),
                    MapDesignOptions(
                        label = "HillshadeDark",
                        design = ArcGISDesign.HillshadeDark,
                    ),
                    MapDesignOptions(
                        label = "StreetsReliefBase",
                        design = ArcGISDesign.StreetsReliefBase,
                    ),
                    MapDesignOptions(
                        label = "TopographicBase",
                        design = ArcGISDesign.TopographicBase,
                    ),
                    MapDesignOptions(
                        label = "ChartedTerritoryBase",
                        design = ArcGISDesign.ChartedTerritoryBase,
                    ),
                    MapDesignOptions(
                        label = "ModernAntiqueBase",
                        design = ArcGISDesign.ModernAntiqueBase,
                    ),
                    MapDesignOptions(
                        label = "HumanGeography",
                        design = ArcGISDesign.HumanGeography,
                    ),
                    MapDesignOptions(
                        label = "HumanGeographyBase",
                        design = ArcGISDesign.HumanGeographyBase,
                    ),
                    MapDesignOptions(
                        label = "HumanGeographyDetail",
                        design = ArcGISDesign.HumanGeographyDetail,
                    ),
                    MapDesignOptions(
                        label = "HumanGeographyLabels",
                        design = ArcGISDesign.HumanGeographyLabels,
                    ),
                    MapDesignOptions(
                        label = "HumanGeographyDark",
                        design = ArcGISDesign.HumanGeographyDark,
                    ),
                    MapDesignOptions(
                        label = "HumanGeographyDarkBase",
                        design = ArcGISDesign.HumanGeographyDarkBase,
                    ),
                    MapDesignOptions(
                        label = "HumanGeographyDarkDetail",
                        design = ArcGISDesign.HumanGeographyDarkDetail,
                    ),
                    MapDesignOptions(
                        label = "HumanGeographyDarkLabels",
                        design = ArcGISDesign.HumanGeographyDarkLabels,
                    ),
                    MapDesignOptions(
                        label = "Outdoor",
                        design = ArcGISDesign.Outdoor,
                    ),
                    MapDesignOptions(
                        label = "OsmStandard",
                        design = ArcGISDesign.OsmStandard,
                    ),
                    MapDesignOptions(
                        label = "OsmStandardRelief",
                        design = ArcGISDesign.OsmStandardRelief,
                    ),
                    MapDesignOptions(
                        label = "OsmStandardReliefBase",
                        design = ArcGISDesign.OsmStandardReliefBase,
                    ),
                    MapDesignOptions(
                        label = "OsmStreets",
                        design = ArcGISDesign.OsmStreets,
                    ),
                    MapDesignOptions(
                        label = "OsmStreetsRelief",
                        design = ArcGISDesign.OsmStreetsRelief,
                    ),
                    MapDesignOptions(
                        label = "OsmLightGray",
                        design = ArcGISDesign.OsmLightGray,
                    ),
                    MapDesignOptions(
                        label = "OsmLightGrayBase",
                        design = ArcGISDesign.OsmLightGrayBase,
                    ),
                    MapDesignOptions(
                        label = "OsmLightGrayLabels",
                        design = ArcGISDesign.OsmLightGrayLabels,
                    ),
                    MapDesignOptions(
                        label = "OsmDarkGray",
                        design = ArcGISDesign.OsmDarkGray,
                    ),
                    MapDesignOptions(
                        label = "OsmDarkGrayBase",
                        design = ArcGISDesign.OsmDarkGrayBase,
                    ),
                    MapDesignOptions(
                        label = "OsmDarkGrayLabels",
                        design = ArcGISDesign.OsmDarkGrayLabels,
                    ),
                    MapDesignOptions(
                        label = "OsmStreetsReliefBase",
                        design = ArcGISDesign.OsmStreetsReliefBase,
                    ),
                    MapDesignOptions(
                        label = "OsmBlueprint",
                        design = ArcGISDesign.OsmBlueprint,
                    ),
                    MapDesignOptions(
                        label = "OsmHybrid",
                        design = ArcGISDesign.OsmHybrid,
                    ),
                    MapDesignOptions(
                        label = "OsmHybridDetail",
                        design = ArcGISDesign.OsmHybridDetail,
                    ),
                    MapDesignOptions(
                        label = "OsmNavigation",
                        design = ArcGISDesign.OsmNavigation,
                    ),
                    MapDesignOptions(
                        label = "OsmNavigationDark",
                        design = ArcGISDesign.OsmNavigationDark,
                    ),
                )
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
}
