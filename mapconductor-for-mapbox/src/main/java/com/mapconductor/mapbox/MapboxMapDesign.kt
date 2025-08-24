package com.mapconductor.mapbox

import com.mapconductor.core.map.MapDesignType

typealias MapboxDesignType = MapDesignType<String>

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

        fun all(): List<Pair<String, MapboxMapDesign>> = listOf(
            "Standard" to Standard,
            "StandardSatellite" to StandardSatellite,
            "Streets" to Streets,
            "Outdoors" to Outdoors,
            "Light" to Light,
            "Dark" to Dark,
            "Satellite" to Satellite,
            "SatelliteStreets" to SatelliteStreets,
            "NavigationDay" to NavigationDay,
            "NavigationNight" to NavigationNight,
        )

        // カスタムスタイルも併せてリスト化したい場合はこっちを使うらしい
        fun allWithCustom(
            customs: List<Pair<String, String>>
        ): List<Pair<String, MapboxMapDesign>> = all() + customs.map {
            (label, styleId) -> label to Custom(styleId)
        }
    }
}
