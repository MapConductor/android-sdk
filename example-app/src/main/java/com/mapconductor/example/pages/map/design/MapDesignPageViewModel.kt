package com.mapconductor.example.pages.map.design

import androidx.lifecycle.ViewModel
import com.mapconductor.arcgis.map.ArcGISDesign
import com.mapconductor.arcgis.map.ArcGISMapViewState
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.map.MapDesignType
import com.mapconductor.core.map.MapViewState
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapDesign
import com.mapconductor.here.HereViewState
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.MapboxViewState
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapDesignOption(
    val label: String,
    val design: MapDesignType<*>,
)

interface MapDesignPageViewModel {
    val initCameraPosition: MapCameraPositionImpl
    val mapViewState: StateFlow<MapViewState<*>?>

    val mapDesignOptions: StateFlow<List<MapDesignOption>>

    fun onMapViewChanged(state: MapViewState<*>)
}

class MapDesignPageViewModelImpl :
    ViewModel(),
    MapDesignPageViewModel {
    override val initCameraPosition =
        MapCameraPositionImpl(
            position =
                GeoPointImpl.fromLatLong(
                    latitude = 21.382314,
                    longitude = -157.933097,
                ),
            zoom = 12.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    private val _mapViewState = MutableStateFlow<MapViewState<*>?>(null)
    override val mapViewState: StateFlow<MapViewState<*>?> = _mapViewState.asStateFlow()

    private val _mapDesignOptions: MutableStateFlow<List<MapDesignOption>> = MutableStateFlow(emptyList())
    override val mapDesignOptions: StateFlow<List<MapDesignOption>> = _mapDesignOptions.asStateFlow()

    override fun onMapViewChanged(state: MapViewState<*>) {
        this._mapViewState.value = state
        when (state) {
            is GoogleMapViewState -> {
                _mapDesignOptions.value = googleMapDesigns
            }
            is HereViewState -> {
                _mapDesignOptions.value = hereMapDesigns
            }
            is MapboxViewState -> {
                _mapDesignOptions.value = mapboxMapDesigns
            }
            is ArcGISMapViewState -> {
                _mapDesignOptions.value = arcGISMapDesigns
            }
            is MapLibreViewState -> {
                _mapDesignOptions.value = mapLibreDesigns
            }
        }
    }

    private val googleMapDesigns =
        listOf(
            MapDesignOption(label = "Normal", design = GoogleMapDesign.Normal),
            MapDesignOption(label = "Satellite", design = GoogleMapDesign.Satellite),
            MapDesignOption(label = "Hybrid", design = GoogleMapDesign.Hybrid),
            MapDesignOption(label = "Terrain", design = GoogleMapDesign.Terrain),
            MapDesignOption(label = "None", design = GoogleMapDesign.None),
        )

    private val hereMapDesigns =
        listOf(
            MapDesignOption(label = "NormalDay", design = HereMapDesign.NormalDay),
            MapDesignOption(label = "NormalNigh", design = HereMapDesign.NormalNight),
            MapDesignOption(label = "Satellite", design = HereMapDesign.Satellite),
            MapDesignOption(label = "HybridDay", design = HereMapDesign.HybridDay),
            MapDesignOption(label = "HybridNight", design = HereMapDesign.HybridNight),
            MapDesignOption(label = "LiteDay", design = HereMapDesign.LiteDay),
            MapDesignOption(label = "LiteNight", design = HereMapDesign.LiteNight),
            MapDesignOption(label = "LiteHybridDay", design = HereMapDesign.LiteHybridDay),
            MapDesignOption(label = "LiteHybridNight", design = HereMapDesign.LiteHybridNight),
            MapDesignOption(label = "LogisticsDay", design = HereMapDesign.LogisticsDay),
            MapDesignOption(label = "LogisticsNight", design = HereMapDesign.LogisticsNight),
            MapDesignOption(label = "LogisticsHybridDay", design = HereMapDesign.LogisticsHybridDay),
            MapDesignOption(label = "RoadNetworkDay", design = HereMapDesign.RoadNetworkDay),
            MapDesignOption(label = "RoadNetworkNight", design = HereMapDesign.RoadNetworkNight),
        )

    private val mapboxMapDesigns =
        listOf(
            MapDesignOption(label = "Standard", design = MapboxMapDesign.Standard),
            MapDesignOption(label = "StandardSatellite", design = MapboxMapDesign.StandardSatellite),
            MapDesignOption(label = "Streets", design = MapboxMapDesign.Streets),
            MapDesignOption(label = "Outdoors", design = MapboxMapDesign.Outdoors),
            MapDesignOption(label = "Light", design = MapboxMapDesign.Light),
            MapDesignOption(label = "Dark", design = MapboxMapDesign.Dark),
            MapDesignOption(label = "Satellite", design = MapboxMapDesign.Satellite),
            MapDesignOption(label = "SatelliteStreets", design = MapboxMapDesign.SatelliteStreets),
            MapDesignOption(label = "NavigationDay", design = MapboxMapDesign.NavigationDay),
            MapDesignOption(label = "NavigationNight", design = MapboxMapDesign.NavigationNight),
        )

    private val arcGISMapDesigns =
        listOf(
            MapDesignOption(label = "Streets", design = ArcGISDesign.Companion.Streets),
            MapDesignOption(label = "Imagery", design = ArcGISDesign.Companion.Imagery),
            MapDesignOption(label = "ImageryStandard", design = ArcGISDesign.Companion.ImageryStandard),
            MapDesignOption(label = "ImageryLabels", design = ArcGISDesign.Companion.ImageryLabels),
            MapDesignOption(label = "LightGray", design = ArcGISDesign.Companion.LightGray),
            MapDesignOption(label = "LightGrayBase", design = ArcGISDesign.Companion.LightGrayBase),
            MapDesignOption(label = "LightGrayLabels", design = ArcGISDesign.Companion.LightGrayLabels),
            MapDesignOption(label = "DarkGray", design = ArcGISDesign.Companion.DarkGray),
            MapDesignOption(label = "DarkGrayBase", design = ArcGISDesign.Companion.DarkGrayBase),
            MapDesignOption(label = "DarkGrayLabels", design = ArcGISDesign.Companion.DarkGrayLabels),
            MapDesignOption(label = "Navigation", design = ArcGISDesign.Companion.Navigation),
            MapDesignOption(label = "NavigationNight", design = ArcGISDesign.Companion.NavigationNight),
            MapDesignOption(label = "StreetsNight", design = ArcGISDesign.Companion.StreetsNight),
            MapDesignOption(label = "StreetsRelief", design = ArcGISDesign.Companion.StreetsRelief),
            MapDesignOption(label = "Topographic", design = ArcGISDesign.Companion.Topographic),
            MapDesignOption(label = "Oceans", design = ArcGISDesign.Companion.Oceans),
            MapDesignOption(label = "OceansBase", design = ArcGISDesign.Companion.OceansBase),
            MapDesignOption(label = "OceansLabels", design = ArcGISDesign.Companion.OceansLabels),
            MapDesignOption(label = "Terrain", design = ArcGISDesign.Companion.Terrain),
            MapDesignOption(label = "TerrainBase", design = ArcGISDesign.Companion.TerrainBase),
            MapDesignOption(label = "TerrainDetail", design = ArcGISDesign.Companion.TerrainDetail),
            MapDesignOption(label = "Community", design = ArcGISDesign.Companion.Community),
            MapDesignOption(label = "ChartedTerritory", design = ArcGISDesign.Companion.ChartedTerritory),
            MapDesignOption(label = "ColoredPencil", design = ArcGISDesign.Companion.ColoredPencil),
            MapDesignOption(label = "Nova", design = ArcGISDesign.Companion.Nova),
            MapDesignOption(label = "ModernAntique", design = ArcGISDesign.Companion.ModernAntique),
            MapDesignOption(label = "Midcentury", design = ArcGISDesign.Companion.Midcentury),
            MapDesignOption(label = "Newspaper", design = ArcGISDesign.Companion.Newspaper),
            MapDesignOption(label = "HillshadeLight", design = ArcGISDesign.Companion.HillshadeLight),
            MapDesignOption(label = "HillshadeDark", design = ArcGISDesign.Companion.HillshadeDark),
            MapDesignOption(label = "StreetsReliefBase", design = ArcGISDesign.Companion.StreetsReliefBase),
            MapDesignOption(label = "TopographicBase", design = ArcGISDesign.Companion.TopographicBase),
            MapDesignOption(label = "ChartedTerritoryBase", design = ArcGISDesign.Companion.ChartedTerritoryBase),
            MapDesignOption(label = "ModernAntiqueBase", design = ArcGISDesign.Companion.ModernAntiqueBase),
            MapDesignOption(label = "HumanGeography", design = ArcGISDesign.Companion.HumanGeography),
            MapDesignOption(label = "HumanGeographyBase", design = ArcGISDesign.Companion.HumanGeographyBase),
            MapDesignOption(label = "HumanGeographyDetail", design = ArcGISDesign.Companion.HumanGeographyDetail),
            MapDesignOption(label = "HumanGeographyLabels", design = ArcGISDesign.Companion.HumanGeographyLabels),
            MapDesignOption(label = "HumanGeographyDark", design = ArcGISDesign.Companion.HumanGeographyDark),
            MapDesignOption(label = "HumanGeographyDarkBase", design = ArcGISDesign.Companion.HumanGeographyDarkBase),
            MapDesignOption(
                label = "HumanGeographyDarkDetail",
                design =
                    ArcGISDesign.Companion.HumanGeographyDarkDetail,
            ),
            MapDesignOption(
                label = "HumanGeographyDarkLabels",
                design =
                    ArcGISDesign.Companion.HumanGeographyDarkLabels,
            ),
            MapDesignOption(label = "Outdoor", design = ArcGISDesign.Companion.Outdoor),
            MapDesignOption(label = "OsmStandard", design = ArcGISDesign.Companion.OsmStandard),
            MapDesignOption(label = "OsmStandardRelief", design = ArcGISDesign.Companion.OsmStandardRelief),
            MapDesignOption(label = "OsmStandardReliefBase", design = ArcGISDesign.Companion.OsmStandardReliefBase),
            MapDesignOption(label = "OsmStreets", design = ArcGISDesign.Companion.OsmStreets),
            MapDesignOption(label = "OsmStreetsRelief", design = ArcGISDesign.Companion.OsmStreetsRelief),
            MapDesignOption(label = "OsmLightGray", design = ArcGISDesign.Companion.OsmLightGray),
            MapDesignOption(label = "OsmLightGrayBase", design = ArcGISDesign.Companion.OsmLightGrayBase),
            MapDesignOption(label = "OsmLightGrayLabels", design = ArcGISDesign.Companion.OsmLightGrayLabels),
            MapDesignOption(label = "OsmDarkGray", design = ArcGISDesign.Companion.OsmDarkGray),
            MapDesignOption(label = "OsmDarkGrayBase", design = ArcGISDesign.Companion.OsmDarkGrayBase),
            MapDesignOption(label = "OsmDarkGrayLabels", design = ArcGISDesign.Companion.OsmDarkGrayLabels),
            MapDesignOption(label = "OsmStreetsReliefBase", design = ArcGISDesign.Companion.OsmStreetsReliefBase),
            MapDesignOption(label = "OsmBlueprint", design = ArcGISDesign.Companion.OsmBlueprint),
            MapDesignOption(label = "OsmHybrid", design = ArcGISDesign.Companion.OsmHybrid),
            MapDesignOption(label = "OsmHybridDetail", design = ArcGISDesign.Companion.OsmHybridDetail),
            MapDesignOption(label = "OsmNavigation", design = ArcGISDesign.Companion.OsmNavigation),
            MapDesignOption(label = "OsmNavigationDark", design = ArcGISDesign.Companion.OsmNavigationDark),
        )

    private val mapLibreDesigns =
        listOf(
            MapDesignOption(label = "DemoTiles", design = MapLibreDesign.DemoTiles),
            MapDesignOption(label = "MapTilerBasicEn", design = MapLibreDesign.MapTilerBasicEn),
            MapDesignOption(label = "MapTilerBasicJa", design = MapLibreDesign.MapTilerBasicJa),
            MapDesignOption(label = "MapTilerTonerEn", design = MapLibreDesign.MapTilerTonerEn),
            MapDesignOption(label = "MapTilerTonerJa", design = MapLibreDesign.MapTilerTonerJa),
            MapDesignOption(label = "OsmBright", design = MapLibreDesign.OsmBright),
            MapDesignOption(label = "OsmBrightEn", design = MapLibreDesign.OsmBrightEn),
            MapDesignOption(label = "OsmBrightJa", design = MapLibreDesign.OsmBrightJa),
            MapDesignOption(label = "OpenMapTiles", design = MapLibreDesign.OpenMapTiles),
            // TODO: check the reason to inspect crashing
//            MapDesignOption(label = "OSM", design = MapLibreDesignType(
//                "OSM",
//                "https://demotiles.maplibre.org/styles/osm-bright-gl-style/style.json",
//            )),
        )
}
