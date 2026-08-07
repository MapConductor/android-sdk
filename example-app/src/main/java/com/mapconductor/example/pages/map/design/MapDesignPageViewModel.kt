package com.mapconductor.example.pages.map.design

import androidx.lifecycle.ViewModel
import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISMapViewStateInterface
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapDesignTypeInterface
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.GoogleMapViewStateInterface
import com.mapconductor.here.HereMapDesign
import com.mapconductor.here.HereViewStateInterface
import com.mapconductor.longdo.LongdoDesign
import com.mapconductor.longdo.LongdoViewStateInterface
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.MapboxViewStateInterface
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreViewStateInterface
import com.mapconductor.maptiler.MapTilerDesign
import com.mapconductor.maptiler.MapTilerViewStateInterface
import com.mapconductor.tomtom.TomTomMapDesign
import com.mapconductor.tomtom.TomTomMapViewStateInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapDesignOption(
    val label: String,
    val design: MapDesignTypeInterface<*>,
)

interface MapDesignPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>

    val mapDesignOptions: StateFlow<List<MapDesignOption>>

    fun onMapViewChanged(state: MapViewStateInterface<*>)
}

class MapDesignPageViewModel :
    ViewModel(),
    MapDesignPageViewModelInterface {
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

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    private val _mapDesignOptions: MutableStateFlow<List<MapDesignOption>> = MutableStateFlow(emptyList())
    override val mapDesignOptions: StateFlow<List<MapDesignOption>> = _mapDesignOptions.asStateFlow()

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        this._mapViewState.value = state
        when (state) {
            is GoogleMapViewStateInterface -> {
                _mapDesignOptions.value = googleMapDesigns
            }
            is HereViewStateInterface -> {
                _mapDesignOptions.value = hereMapDesigns
            }
            is MapboxViewStateInterface -> {
                _mapDesignOptions.value = mapboxMapDesigns
            }
            is ArcGISMapViewStateInterface -> {
                _mapDesignOptions.value = arcGISMapDesigns
            }
            is MapLibreViewStateInterface -> {
                _mapDesignOptions.value = mapLibreDesigns
            }
            is TomTomMapViewStateInterface -> {
                _mapDesignOptions.value = tomTomMapDesigns
            }
            is MapTilerViewStateInterface -> {
                _mapDesignOptions.value = mapTilerDesigns
            }
            is LongdoViewStateInterface -> {
                _mapDesignOptions.value = longdoMapDesigns
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
            MapDesignOption(label = "Streets", design = ArcGISDesign.Streets),
            MapDesignOption(label = "Imagery", design = ArcGISDesign.Imagery),
            MapDesignOption(label = "ImageryStandard", design = ArcGISDesign.ImageryStandard),
            MapDesignOption(label = "ImageryLabels", design = ArcGISDesign.ImageryLabels),
            MapDesignOption(label = "LightGray", design = ArcGISDesign.LightGray),
            MapDesignOption(label = "LightGrayBase", design = ArcGISDesign.LightGrayBase),
            MapDesignOption(label = "LightGrayLabels", design = ArcGISDesign.LightGrayLabels),
            MapDesignOption(label = "DarkGray", design = ArcGISDesign.DarkGray),
            MapDesignOption(label = "DarkGrayBase", design = ArcGISDesign.DarkGrayBase),
            MapDesignOption(label = "DarkGrayLabels", design = ArcGISDesign.DarkGrayLabels),
            MapDesignOption(label = "Navigation", design = ArcGISDesign.Navigation),
            MapDesignOption(label = "NavigationNight", design = ArcGISDesign.NavigationNight),
            MapDesignOption(label = "StreetsNight", design = ArcGISDesign.StreetsNight),
            MapDesignOption(label = "StreetsRelief", design = ArcGISDesign.StreetsRelief),
            MapDesignOption(label = "Topographic", design = ArcGISDesign.Topographic),
            MapDesignOption(label = "Oceans", design = ArcGISDesign.Oceans),
            MapDesignOption(label = "OceansBase", design = ArcGISDesign.OceansBase),
            MapDesignOption(label = "OceansLabels", design = ArcGISDesign.OceansLabels),
            MapDesignOption(label = "Terrain", design = ArcGISDesign.Terrain),
            MapDesignOption(label = "TerrainBase", design = ArcGISDesign.TerrainBase),
            MapDesignOption(label = "TerrainDetail", design = ArcGISDesign.TerrainDetail),
            MapDesignOption(label = "Community", design = ArcGISDesign.Community),
            MapDesignOption(label = "ChartedTerritory", design = ArcGISDesign.ChartedTerritory),
            MapDesignOption(label = "ColoredPencil", design = ArcGISDesign.ColoredPencil),
            MapDesignOption(label = "Nova", design = ArcGISDesign.Nova),
            MapDesignOption(label = "ModernAntique", design = ArcGISDesign.ModernAntique),
            MapDesignOption(label = "Midcentury", design = ArcGISDesign.Midcentury),
            MapDesignOption(label = "Newspaper", design = ArcGISDesign.Newspaper),
            MapDesignOption(label = "HillshadeLight", design = ArcGISDesign.HillshadeLight),
            MapDesignOption(label = "HillshadeDark", design = ArcGISDesign.HillshadeDark),
            MapDesignOption(label = "StreetsReliefBase", design = ArcGISDesign.StreetsReliefBase),
            MapDesignOption(label = "TopographicBase", design = ArcGISDesign.TopographicBase),
            MapDesignOption(label = "ChartedTerritoryBase", design = ArcGISDesign.ChartedTerritoryBase),
            MapDesignOption(label = "ModernAntiqueBase", design = ArcGISDesign.ModernAntiqueBase),
            MapDesignOption(label = "HumanGeography", design = ArcGISDesign.HumanGeography),
            MapDesignOption(label = "HumanGeographyBase", design = ArcGISDesign.HumanGeographyBase),
            MapDesignOption(label = "HumanGeographyDetail", design = ArcGISDesign.HumanGeographyDetail),
            MapDesignOption(label = "HumanGeographyLabels", design = ArcGISDesign.HumanGeographyLabels),
            MapDesignOption(label = "HumanGeographyDark", design = ArcGISDesign.HumanGeographyDark),
            MapDesignOption(label = "HumanGeographyDarkBase", design = ArcGISDesign.HumanGeographyDarkBase),
            MapDesignOption(
                label = "HumanGeographyDarkDetail",
                design =
                    ArcGISDesign.HumanGeographyDarkDetail,
            ),
            MapDesignOption(
                label = "HumanGeographyDarkLabels",
                design =
                    ArcGISDesign.HumanGeographyDarkLabels,
            ),
            MapDesignOption(label = "Outdoor", design = ArcGISDesign.Outdoor),
            MapDesignOption(label = "OsmStandard", design = ArcGISDesign.OsmStandard),
            MapDesignOption(label = "OsmStandardRelief", design = ArcGISDesign.OsmStandardRelief),
            MapDesignOption(label = "OsmStandardReliefBase", design = ArcGISDesign.OsmStandardReliefBase),
            MapDesignOption(label = "OsmStreets", design = ArcGISDesign.OsmStreets),
            MapDesignOption(label = "OsmStreetsRelief", design = ArcGISDesign.OsmStreetsRelief),
            MapDesignOption(label = "OsmLightGray", design = ArcGISDesign.OsmLightGray),
            MapDesignOption(label = "OsmLightGrayBase", design = ArcGISDesign.OsmLightGrayBase),
            MapDesignOption(label = "OsmLightGrayLabels", design = ArcGISDesign.OsmLightGrayLabels),
            MapDesignOption(label = "OsmDarkGray", design = ArcGISDesign.OsmDarkGray),
            MapDesignOption(label = "OsmDarkGrayBase", design = ArcGISDesign.OsmDarkGrayBase),
            MapDesignOption(label = "OsmDarkGrayLabels", design = ArcGISDesign.OsmDarkGrayLabels),
            MapDesignOption(label = "OsmStreetsReliefBase", design = ArcGISDesign.OsmStreetsReliefBase),
            MapDesignOption(label = "OsmBlueprint", design = ArcGISDesign.OsmBlueprint),
            MapDesignOption(label = "OsmHybrid", design = ArcGISDesign.OsmHybrid),
            MapDesignOption(label = "OsmHybridDetail", design = ArcGISDesign.OsmHybridDetail),
            MapDesignOption(label = "OsmNavigation", design = ArcGISDesign.OsmNavigation),
            MapDesignOption(label = "OsmNavigationDark", design = ArcGISDesign.OsmNavigationDark),
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

    private val tomTomMapDesigns =
        listOf(
            MapDesignOption(label = "Standard", design = TomTomMapDesign.Standard),
            MapDesignOption(label = "Driving", design = TomTomMapDesign.Driving),
            MapDesignOption(label = "Satellite", design = TomTomMapDesign.Satellite),
        )

    private val mapTilerDesigns =
        listOf(
            MapDesignOption(label = "Streets", design = MapTilerDesign.Streets),
            MapDesignOption(label = "StreetsDark", design = MapTilerDesign.StreetsDark),
            MapDesignOption(label = "StreetsLight", design = MapTilerDesign.StreetsLight),
            MapDesignOption(label = "Basic", design = MapTilerDesign.Basic),
            MapDesignOption(label = "Bright", design = MapTilerDesign.Bright),
            MapDesignOption(label = "Satellite", design = MapTilerDesign.Satellite),
            MapDesignOption(label = "Outdoor", design = MapTilerDesign.Outdoor),
            MapDesignOption(label = "Winter", design = MapTilerDesign.Winter),
            MapDesignOption(label = "Topo", design = MapTilerDesign.Topo),
            MapDesignOption(label = "Toner", design = MapTilerDesign.Toner),
            MapDesignOption(label = "Dataviz", design = MapTilerDesign.Dataviz),
            MapDesignOption(label = "Backdrop", design = MapTilerDesign.Backdrop),
            MapDesignOption(label = "Ocean", design = MapTilerDesign.Ocean),
            MapDesignOption(label = "Landscape", design = MapTilerDesign.Landscape),
            MapDesignOption(label = "Aquarelle", design = MapTilerDesign.Aquarelle),
            MapDesignOption(label = "OpenStreetMap", design = MapTilerDesign.OpenStreetMap),
        )

    private val longdoMapDesigns =
        listOf(
            MapDesignOption(label = "Normal", design = LongdoDesign.Normal),
            MapDesignOption(label = "Easy", design = LongdoDesign.Easy),
            MapDesignOption(label = "Pastel", design = LongdoDesign.Pastel),
            MapDesignOption(label = "PastelGray", design = LongdoDesign.PastelGray),
            MapDesignOption(label = "Hard", design = LongdoDesign.Hard),
            MapDesignOption(label = "Gray", design = LongdoDesign.Gray),
            MapDesignOption(label = "Light", design = LongdoDesign.Light),
            MapDesignOption(label = "Night", design = LongdoDesign.Night),
            MapDesignOption(label = "Dark", design = LongdoDesign.Dark),
            MapDesignOption(label = "Political", design = LongdoDesign.Political),
            MapDesignOption(label = "Osm", design = LongdoDesign.Osm),
            MapDesignOption(label = "Satellite", design = LongdoDesign.Satellite),
            MapDesignOption(label = "Hybrid", design = LongdoDesign.Hybrid),
        )
}
