package com.mapconductor.mapbox

import com.mapbox.maps.Style
import com.mapconductor.core.map.MapDesignTypeInterface
import com.mapconductor.mapbox.MapboxMapDesign.Companion.MAPBOX_URL

typealias MapboxDesignType = MapDesignTypeInterface<String>

sealed class MapboxMapDesign(
    override val id: String,
) : MapboxDesignType {
    object Standard : MapboxMapDesign("standard")

    object StandardSatellite : MapboxMapDesign("standard-satellite")

    object Streets : MapboxMapDesign("streets-v12")

    object Outdoors : MapboxMapDesign("outdoors-v12")

    object Light : MapboxMapDesign("light-v11")

    object Dark : MapboxMapDesign("dark-v11")

    object Satellite : MapboxMapDesign("satellite-v9")

    object SatelliteStreets : MapboxMapDesign("satellite-streets-v12")

    object NavigationDay : MapboxMapDesign("navigation-day-v1")

    object NavigationNight : MapboxMapDesign("navigation-night-v1")

    class Custom(
        layerId: String,
    ) : MapboxMapDesign(layerId)

    override fun getValue(): String = "${MAPBOX_URL}/${this.id}"

    companion object {
        val MAPBOX_URL = "mapbox://styles/mapbox"

        fun Create(layerId: String): MapboxMapDesign =
            when (layerId) {
                Standard.id -> Standard
                StandardSatellite.id -> StandardSatellite
                Streets.id -> Streets
                Outdoors.id -> Outdoors
                Light.id -> Light
                Dark.id -> Dark
                Satellite.id -> Satellite
                SatelliteStreets.id -> SatelliteStreets
                NavigationDay.id -> NavigationDay
                NavigationNight.id -> NavigationNight
                else -> Custom(layerId)
            }
    }
}

fun Style.toMapDesignType(): MapboxDesignType =
    when (this.styleURI) {
        "$MAPBOX_URL/${MapboxMapDesign.Standard.id}" -> MapboxMapDesign.Standard
        "$MAPBOX_URL/${MapboxMapDesign.StandardSatellite.id}" -> MapboxMapDesign.StandardSatellite
        "$MAPBOX_URL/${MapboxMapDesign.Streets.id}" -> MapboxMapDesign.Streets
        "$MAPBOX_URL/${MapboxMapDesign.Outdoors.id}" -> MapboxMapDesign.Outdoors
        "$MAPBOX_URL/${MapboxMapDesign.Light.id}" -> MapboxMapDesign.Light
        "$MAPBOX_URL/${MapboxMapDesign.Dark.id}" -> MapboxMapDesign.Dark
        "$MAPBOX_URL/${MapboxMapDesign.Satellite.id}" -> MapboxMapDesign.Satellite
        "$MAPBOX_URL/${MapboxMapDesign.SatelliteStreets.id}" -> MapboxMapDesign.SatelliteStreets
        "$MAPBOX_URL/${MapboxMapDesign.NavigationDay.id}" -> MapboxMapDesign.NavigationDay
        "$MAPBOX_URL/${MapboxMapDesign.NavigationNight.id}" -> MapboxMapDesign.NavigationNight
        else ->
            MapboxMapDesign.Custom(
                layerId = this.styleURI.replaceFirst("${MAPBOX_URL}/", ""),
            )
    }
