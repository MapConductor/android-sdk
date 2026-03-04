package com.mapconductor.arcgis.map

import com.arcgismaps.mapping.BasemapStyle
import com.mapconductor.core.map.MapDesignTypeInterface

interface ArcGISDesignTypeInterface : MapDesignTypeInterface<String> {
    val elevationSources: List<String>
}

data class ArcGISDesign(
    override val id: String,
    override val elevationSources: List<String> = emptyList<String>(),
) : ArcGISDesignTypeInterface {
    override fun getValue(): String = id

    fun withElevationSources(sources: List<String>): ArcGISDesign =
        copy(
            elevationSources = sources,
        )

    companion object {
        val Streets = ArcGISDesign("arc_gis_streets")
        val Imagery = ArcGISDesign("arc_gis_imagery")
        val ImageryStandard = ArcGISDesign("arc_gis_imagery_standard")
        val ImageryLabels = ArcGISDesign("arc_gis_imagery_labels")
        val LightGray = ArcGISDesign("arc_gis_light_gray")
        val LightGrayBase = ArcGISDesign("arc_gis_light_gray_base")
        val LightGrayLabels = ArcGISDesign("arc_gis_light_gray_labels")
        val DarkGray = ArcGISDesign("arc_gis_dark_gray")
        val DarkGrayBase = ArcGISDesign("arc_gis_dark_gray_base")
        val DarkGrayLabels = ArcGISDesign("arc_gis_dark_gray_labels")
        val Navigation = ArcGISDesign("arc_gis_navigation")
        val NavigationNight = ArcGISDesign("arc_gis_navigation_night")
        val StreetsNight = ArcGISDesign("arc_gis_streets_night")
        val StreetsRelief = ArcGISDesign("arc_gis_streets_relief")
        val Topographic = ArcGISDesign("arc_gis_topographic")
        val Oceans = ArcGISDesign("arc_gis_oceans")
        val OceansBase = ArcGISDesign("arc_gis_oceans_base")
        val OceansLabels = ArcGISDesign("arc_gis_oceans_labels")
        val Terrain = ArcGISDesign("arc_gis_terrain")
        val TerrainBase = ArcGISDesign("arc_gis_terrain_base")
        val TerrainDetail = ArcGISDesign("arc_gis_terrain_detail")
        val Community = ArcGISDesign("arc_gis_community")
        val ChartedTerritory = ArcGISDesign("arc_gis_charted_territory")
        val ColoredPencil = ArcGISDesign("arc_gis_colored_pencil")
        val Nova = ArcGISDesign("arc_gis_nova")
        val ModernAntique = ArcGISDesign("arc_gis_modern_antique")
        val Midcentury = ArcGISDesign("arc_gis_midcentury")
        val Newspaper = ArcGISDesign("arc_gis_newspaper")
        val HillshadeLight = ArcGISDesign("arc_gis_hillshade_light")
        val HillshadeDark = ArcGISDesign("arc_gis_hillshade_dark")
        val StreetsReliefBase = ArcGISDesign("arc_gis_streets_relief_base")
        val TopographicBase = ArcGISDesign("arc_gis_topographic_base")
        val ChartedTerritoryBase = ArcGISDesign("arc_gis_charted_territory_base")
        val ModernAntiqueBase = ArcGISDesign("arc_gis_modern_antique_base")
        val HumanGeography = ArcGISDesign("arc_gis_human_geography")
        val HumanGeographyBase = ArcGISDesign("arc_gis_human_geography_base")
        val HumanGeographyDetail = ArcGISDesign("arc_gis_human_geography_detail")
        val HumanGeographyLabels = ArcGISDesign("arc_gis_human_geography_labels")
        val HumanGeographyDark = ArcGISDesign("arc_gis_human_geography_dark")
        val HumanGeographyDarkBase = ArcGISDesign("arc_gis_human_geography_dark_base")
        val HumanGeographyDarkDetail = ArcGISDesign("arc_gis_human_geography_dark_detail")
        val HumanGeographyDarkLabels = ArcGISDesign("arc_gis_human_geography_dark_labels")
        val Outdoor = ArcGISDesign("arc_gis_outdoor")
        val OsmStandard = ArcGISDesign("osm_standard")
        val OsmStandardRelief = ArcGISDesign("osm_standard_relief")
        val OsmStandardReliefBase = ArcGISDesign("osm_standard_relief_base")
        val OsmStreets = ArcGISDesign("osm_streets")
        val OsmStreetsRelief = ArcGISDesign("osm_streets_relief")
        val OsmLightGray = ArcGISDesign("osm_light_gray")
        val OsmLightGrayBase = ArcGISDesign("osm_light_gray_base")
        val OsmLightGrayLabels = ArcGISDesign("osm_light_gray_labels")
        val OsmDarkGray = ArcGISDesign("osm_dark_gray")
        val OsmDarkGrayBase = ArcGISDesign("osm_dark_gray_base")
        val OsmDarkGrayLabels = ArcGISDesign("osm_dark_gray_labels")
        val OsmStreetsReliefBase = ArcGISDesign("osm_streets_relief_base")
        val OsmBlueprint = ArcGISDesign("osm_blueprint")
        val OsmHybrid = ArcGISDesign("osm_hybrid")
        val OsmHybridDetail = ArcGISDesign("osm_hybrid_detail")
        val OsmNavigation = ArcGISDesign("osm_navigation")
        val OsmNavigationDark = ArcGISDesign("osm_navigation_dark")

        fun Create(
            id: String,
            sources: List<String> = emptyList<String>(),
        ): ArcGISDesign =
            when (id) {
                Streets.id -> Streets
                Imagery.id -> Imagery
                ImageryStandard.id -> ImageryStandard
                ImageryLabels.id -> ImageryLabels
                LightGray.id -> LightGray
                LightGrayBase.id -> LightGrayBase
                LightGrayLabels.id -> LightGrayLabels
                DarkGray.id -> DarkGray
                DarkGrayBase.id -> DarkGrayBase
                DarkGrayLabels.id -> DarkGrayLabels
                Navigation.id -> Navigation
                NavigationNight.id -> NavigationNight
                StreetsNight.id -> StreetsNight
                StreetsRelief.id -> StreetsRelief
                Topographic.id -> Topographic
                Oceans.id -> Oceans
                OceansBase.id -> OceansBase
                OceansLabels.id -> OceansLabels
                Terrain.id -> Terrain
                TerrainBase.id -> TerrainBase
                TerrainDetail.id -> TerrainDetail
                Community.id -> Community
                ChartedTerritory.id -> ChartedTerritory
                ColoredPencil.id -> ColoredPencil
                Nova.id -> Nova
                ModernAntique.id -> ModernAntique
                Midcentury.id -> Midcentury
                Newspaper.id -> Newspaper
                HillshadeLight.id -> HillshadeLight
                HillshadeDark.id -> HillshadeDark
                StreetsReliefBase.id -> StreetsReliefBase
                TopographicBase.id -> TopographicBase
                ChartedTerritoryBase.id -> ChartedTerritoryBase
                ModernAntiqueBase.id -> ModernAntiqueBase
                HumanGeography.id -> HumanGeography
                HumanGeographyBase.id -> HumanGeographyBase
                HumanGeographyDetail.id -> HumanGeographyDetail
                HumanGeographyLabels.id -> HumanGeographyLabels
                HumanGeographyDark.id -> HumanGeographyDark
                HumanGeographyDarkBase.id -> HumanGeographyDarkBase
                HumanGeographyDarkDetail.id -> HumanGeographyDarkDetail
                HumanGeographyDarkLabels.id -> HumanGeographyDarkLabels
                Outdoor.id -> Outdoor
                OsmStandard.id -> OsmStandard
                OsmStandardRelief.id -> OsmStandardRelief
                OsmStandardReliefBase.id -> OsmStandardReliefBase
                OsmStreets.id -> OsmStreets
                OsmStreetsRelief.id -> OsmStreetsRelief
                OsmLightGray.id -> OsmLightGray
                OsmLightGrayBase.id -> OsmLightGrayBase
                OsmLightGrayLabels.id -> OsmLightGrayLabels
                OsmDarkGray.id -> OsmDarkGray
                OsmDarkGrayBase.id -> OsmDarkGrayBase
                OsmDarkGrayLabels.id -> OsmDarkGrayLabels
                OsmStreetsReliefBase.id -> OsmStreetsReliefBase
                OsmBlueprint.id -> OsmBlueprint
                OsmHybrid.id -> OsmHybrid
                OsmHybridDetail.id -> OsmHybridDetail
                OsmNavigation.id -> OsmNavigation
                OsmNavigationDark.id -> OsmNavigationDark
                else -> throw Throwable("unknown design id: \"$id\"")
            }

        fun toBasemapStyle(designType: ArcGISDesignTypeInterface): BasemapStyle =
            when (designType.getValue()) {
                Streets.id -> BasemapStyle.ArcGISStreets
                Imagery.id -> BasemapStyle.ArcGISImagery
                ImageryStandard.id -> BasemapStyle.ArcGISImageryStandard
                ImageryLabels.id -> BasemapStyle.ArcGISImageryLabels
                LightGray.id -> BasemapStyle.ArcGISLightGray
                LightGrayBase.id -> BasemapStyle.ArcGISLightGrayBase
                LightGrayLabels.id -> BasemapStyle.ArcGISLightGrayLabels
                DarkGray.id -> BasemapStyle.ArcGISDarkGray
                DarkGrayBase.id -> BasemapStyle.ArcGISDarkGrayBase
                DarkGrayLabels.id -> BasemapStyle.ArcGISDarkGrayLabels
                Navigation.id -> BasemapStyle.ArcGISNavigation
                NavigationNight.id -> BasemapStyle.ArcGISNavigationNight
                StreetsNight.id -> BasemapStyle.ArcGISStreetsNight
                StreetsRelief.id -> BasemapStyle.ArcGISStreetsRelief
                Topographic.id -> BasemapStyle.ArcGISTopographic
                Oceans.id -> BasemapStyle.ArcGISOceans
                OceansBase.id -> BasemapStyle.ArcGISOceansBase
                OceansLabels.id -> BasemapStyle.ArcGISOceansLabels
                Terrain.id -> BasemapStyle.ArcGISTerrain
                TerrainBase.id -> BasemapStyle.ArcGISTerrainBase
                TerrainDetail.id -> BasemapStyle.ArcGISTerrainDetail
                Community.id -> BasemapStyle.ArcGISCommunity
                ChartedTerritory.id -> BasemapStyle.ArcGISChartedTerritory
                ColoredPencil.id -> BasemapStyle.ArcGISColoredPencil
                Nova.id -> BasemapStyle.ArcGISNova
                ModernAntique.id -> BasemapStyle.ArcGISModernAntique
                Midcentury.id -> BasemapStyle.ArcGISMidcentury
                Newspaper.id -> BasemapStyle.ArcGISNewspaper
                HillshadeLight.id -> BasemapStyle.ArcGISHillshadeLight
                HillshadeDark.id -> BasemapStyle.ArcGISHillshadeDark
                StreetsReliefBase.id -> BasemapStyle.ArcGISStreetsReliefBase
                TopographicBase.id -> BasemapStyle.ArcGISTopographicBase
                ChartedTerritoryBase.id -> BasemapStyle.ArcGISChartedTerritoryBase
                ModernAntiqueBase.id -> BasemapStyle.ArcGISModernAntiqueBase
                HumanGeography.id -> BasemapStyle.ArcGISHumanGeography
                HumanGeographyBase.id -> BasemapStyle.ArcGISHumanGeographyBase
                HumanGeographyDetail.id -> BasemapStyle.ArcGISHumanGeographyDetail
                HumanGeographyLabels.id -> BasemapStyle.ArcGISHumanGeographyLabels
                HumanGeographyDark.id -> BasemapStyle.ArcGISHumanGeographyDark
                HumanGeographyDarkBase.id -> BasemapStyle.ArcGISHumanGeographyDarkBase
                HumanGeographyDarkDetail.id -> BasemapStyle.ArcGISHumanGeographyDarkDetail
                HumanGeographyDarkLabels.id -> BasemapStyle.ArcGISHumanGeographyDarkLabels
                Outdoor.id -> BasemapStyle.ArcGISOutdoor
                OsmStandard.id -> BasemapStyle.OsmStandard
                OsmStandardRelief.id -> BasemapStyle.OsmStandardRelief
                OsmStandardReliefBase.id -> BasemapStyle.OsmStandardReliefBase
                OsmStreets.id -> BasemapStyle.OsmStreets
                OsmStreetsRelief.id -> BasemapStyle.OsmStreetsRelief
                OsmLightGray.id -> BasemapStyle.OsmLightGray
                OsmLightGrayBase.id -> BasemapStyle.OsmLightGrayBase
                OsmLightGrayLabels.id -> BasemapStyle.OsmLightGrayLabels
                OsmDarkGray.id -> BasemapStyle.OsmDarkGray
                OsmDarkGrayBase.id -> BasemapStyle.OsmDarkGrayBase
                OsmDarkGrayLabels.id -> BasemapStyle.OsmDarkGrayLabels
                OsmStreetsReliefBase.id -> BasemapStyle.OsmStreetsReliefBase
                OsmBlueprint.id -> BasemapStyle.OsmBlueprint
                OsmHybrid.id -> BasemapStyle.OsmHybrid
                OsmHybridDetail.id -> BasemapStyle.OsmHybridDetail
                OsmNavigation.id -> BasemapStyle.OsmNavigation
                OsmNavigationDark.id -> BasemapStyle.OsmNavigationDark
                else -> throw Throwable("unknown design id: \"$designType.id\"")
            }
    }
}
